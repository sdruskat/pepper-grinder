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

import javax.inject.Inject;

import org.corpus_tools.pepper.common.MODULE_TYPE;
import org.corpus_tools.pepper.common.PepperModuleDesc;
import org.corpus_tools.pepper.connectors.PepperConnector;
import org.corpus_tools.pepper.modules.PepperModuleProperties;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

import net.sdruskat.peppergrinder.rcp.pepper.GrinderPepperStarter;
import net.sdruskat.peppergrinder.rcp.util.ZipCompressor;

/**
 * // TODO Add description
 *
 * @author Stephan Druskat <[mail@sdruskat.net](mailto:mail@sdruskat.net)>
 * 
 */
public class ConversionRunner {
	
//	public enum MODULE_TYPE {
//		IMPORTER, MANIPULATOR, EXPORTER
//	};
	
	protected PepperConnector pepper;
	
	protected PepperModuleProperties pepperModuleProperties;

	private final String corpusDirectoryPath;

	private List pepperModuleDescriptions;

	private List<PepperModuleDesc> importerModules;

	public ConversionRunner(String corpusDirectoryPath) {
		this.corpusDirectoryPath = corpusDirectoryPath;
		init();
	}

	private void init() {
		GrinderPepperStarter pepperStarter = new GrinderPepperStarter();
		pepperStarter.startPepper();
		setPepper(pepperStarter.getPepper());
		pepperModuleProperties = new PepperModuleProperties();
		// FIXME TODO Check whether previous selections are remembered.
//		readDialogSettings();
		importerModules = getPepperModules(MODULE_TYPE.IMPORTER);
		if (importerModules.size() < 4) { // Only the three basic modules are available
			System.err.println("More modules may be available. To install/update modules, run Help > Updates > Update Pepper.");
		}
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
//				log.error("Loading available non-core Pepper modules did not complete successfully!", e);
				MessageDialog.openError(Display.getCurrent() != null ? Display.getCurrent().getActiveShell() : Display.getDefault().getActiveShell(), "Pepper modules not loaded!", "The available Pepper modules could not be loaded! Only the core modules will be available!");
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
			if (pepperModuleDescriptions.isEmpty()) {
//				new MessageDialog(Display.getDefault()., "Error", null, "Did not find any Pepper module of type " + wizardMode.name() + "!", MessageDialog.ERROR, new String[] { IDialogConstants.OK_LABEL }, 0).open();
			}
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
	 * @param pepper the pepper to set
	 */
	public final void setPepper(PepperConnector pepper) {
		this.pepper = pepper;
	}

	public boolean run() {
		try {
				PepperModuleRunnable moduleRunnable = createModuleRunnable(/*project, */true);
//				progressService.run(false, true, moduleRunnable);
				new ProgressMonitorDialog(Display.getDefault().getActiveShell()).run(false, true, moduleRunnable);

				boolean outcome = moduleRunnable.get().booleanValue();
				String name = ((GrinderModuleRunnable) moduleRunnable).getName();
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
						Files.write(Paths.get("./ANNIS-OUTPUT/resolver_vis_map.annis"),
								lineBuilder.toString().getBytes());
					}
					catch (IOException e) {
						e.printStackTrace();
					}
					ZipCompressor zipper = new ZipCompressor();
					File zip = new File("./ANNIS-OUTPUT-ZIP/" + name + ".zip");
					zipper.createZip("./ANNIS-OUTPUT/", zip.getAbsolutePath());
//					zipper.zipDirectory(new File("./ANNIS-OUTPUT"), "./ANNIS-CORPUS/" + name + ".zip");
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
		return new GrinderModuleRunnable(cancelable, getPepper(), this.corpusDirectoryPath);
	}

}
