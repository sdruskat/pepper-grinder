/*******************************************************************************
 * Copyright (c) 2016, 2017 Stephan Druskat
 * Exploitation rights for this version belong exclusively to Universität Hamburg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     Stephan Druskat - initial API and implementation
 *******************************************************************************/
/**
 * 
 */
package net.sdruskat.peppergrinder.rcp.conversion;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CancellationException;

import org.corpus_tools.pepper.common.MODULE_TYPE;
import org.corpus_tools.pepper.common.PepperModuleDesc;
import org.corpus_tools.pepper.connectors.PepperConnector;
import org.corpus_tools.pepper.modules.PepperModuleProperties;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.widgets.Display;

import net.sdruskat.peppergrinder.rcp.LogManager;
import net.sdruskat.peppergrinder.rcp.pepper.GrinderPepperStarter;
import net.sdruskat.peppergrinder.rcp.util.ZipCompressor;

/**
 * A class that configures and runs a conversion process in Pepper.
 *
 * @author Stephan Druskat <[mail@sdruskat.net](mailto:mail@sdruskat.net)>
 * 
 */
public class ConversionRunner {

	private static LogManager log = LogManager.INSTANCE;

	protected PepperConnector pepper;

	protected PepperModuleProperties pepperModuleProperties;

	private final String corpusDirectoryPath;

	private List<PepperModuleDesc> pepperModuleDescriptions;

	private List<PepperModuleDesc> importerModules;

	/**
	 * Constructor setting the directory path of the corpus to import in the
	 * conversion process and initializes this instance of
	 * {@link ConversionRunner}.
	 * 
	 * @param corpusDirectoryPath
	 */
	public ConversionRunner(String corpusDirectoryPath) {
		this.corpusDirectoryPath = corpusDirectoryPath;
		init();
	}

	/**
	 * Initializes the runner by creating and starting a new
	 * Pepper instance and getting the available 
	 * properties and modules.
	 */
	private void init() {
		GrinderPepperStarter pepperStarter = new GrinderPepperStarter();
		pepperStarter.startPepper();
		setPepper(pepperStarter.getPepper());
		pepperModuleProperties = new PepperModuleProperties();
		importerModules = getPepperModules(MODULE_TYPE.IMPORTER);
		/*
		 * Not relevant in the current setup, as only the pre-packaged
		 * TraCESImporter is available in addition to the basic modules.
		 */
		// if (importerModules.size() < 4) { // Only the three basic modules are
		// available
		// log.info("More modules may be available. To install/update modules,
		// run Help > Updates > Update Pepper.");
		// }
	}

	protected List<PepperModuleDesc> getPepperModules(MODULE_TYPE moduleType) {
		if (pepperModuleDescriptions == null) {
			pepperModuleDescriptions = new ArrayList<>();
		}
		if (!pepperModuleDescriptions.isEmpty()) {
			return pepperModuleDescriptions;
		}
		else {
			LoadPepperModuleRunnable moduleRunnable = new LoadPepperModuleRunnable(getPepper());
			try {
				new ProgressMonitorDialog(Display.getDefault().getActiveShell()).run(false, true, moduleRunnable);
			}
			catch (InvocationTargetException | InterruptedException e) {
				log.error("Loading available non-core Pepper modules did not complete successfully!");
				MessageDialog.openError(
						Display.getCurrent() != null ? Display.getCurrent().getActiveShell()
								: Display.getDefault().getActiveShell(),
						"Pepper modules not loaded!",
						"The available Pepper modules could not be loaded! Only the core modules will be available!");
			}
			// Compile list of module description for use in pages
			if (getPepper() != null) {
				Collection<PepperModuleDesc> allModuleDescs = getPepper().getRegisteredModules();
				if (allModuleDescs != null) {
					for (PepperModuleDesc desc : allModuleDescs) {
						if (desc.getModuleType() == moduleType) {
							pepperModuleDescriptions.add(desc);
						}
					}
				}
			}
			/*
			 * To use later in properly implemented Grinder
			 */
			// if (pepperModuleDescriptions.isEmpty()) {
			// new MessageDialog(Display.getDefault().getActiveShell(), "Error",
			// null, "Did not find any Pepper module of type " +
			// wizardMode.name() + "!", MessageDialog.ERROR, new String[] {
			// IDialogConstants.OK_LABEL }, 0).open();
			// }
			// Sort list of module descriptions
			Collections.sort(pepperModuleDescriptions, new Comparator<PepperModuleDesc>() {
				@Override
				public int compare(PepperModuleDesc o1, PepperModuleDesc o2) {
					return o1.getName().compareTo(o2.getName());
				}
			});
			return pepperModuleDescriptions;
		}
	}

	/**
	 * @return the pepper
	 */
	public final PepperConnector getPepper() {
		return pepper;
	}

	/**
	 * @param pepper
	 *            the pepper to set
	 */
	public final void setPepper(PepperConnector pepper) {
		this.pepper = pepper;
	}

	/**
	 * Runs the Pepper conversion process. 
	 * 
	 * @return The outcome as a boolean value.
	 */
	public boolean run() {
		try {
			PepperModuleRunnable moduleRunnable = createModuleRunnable(/* project, */true);
			new ProgressMonitorDialog(Display.getDefault().getActiveShell()).run(false, true, moduleRunnable);

			boolean outcome = moduleRunnable.get().booleanValue();
			String name = ((TraCESToANNISModuleRunnable) moduleRunnable).getName();
			if (outcome) {
				List<String> lines = Files.readAllLines(Paths.get("./ANNIS-OUTPUT/resolver_vis_map.annis"));
				List<String> newLines = new ArrayList<>();
				for (String l : lines) {
					l = l.replace("$PLACEHOLDER-FOR-REAL-NAME$", name);
					newLines.add(l);
				}
				StringBuilder lineBuilder = new StringBuilder();
				for (String newLine : newLines) {
					lineBuilder.append(newLine + "\n");
				}
				try {
					Files.write(Paths.get("./ANNIS-OUTPUT/resolver_vis_map.annis"), lineBuilder.toString().getBytes());
				}
				catch (IOException e) {
					e.printStackTrace();
				}
				ZipCompressor.createZipFile(new File("./ANNIS-OUTPUT"), "./ANNIS-OUTPUT/" + name + ".zip");
			}
			return outcome;

		}
		catch (CancellationException X) {
			return false;
		}
		catch (Exception X) {
			X.printStackTrace();
			return false;
		}

	}

	private PepperModuleRunnable createModuleRunnable(boolean cancelable) {
		return new TraCESToANNISModuleRunnable(cancelable, getPepper(), this.corpusDirectoryPath);
	}

}
