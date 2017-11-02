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
import java.io.FilenameFilter;
import java.time.LocalDateTime;
import java.util.Properties;

import org.corpus_tools.pepper.common.CorpusDesc;
import org.corpus_tools.pepper.common.MODULE_TYPE;
import org.corpus_tools.pepper.common.StepDesc;
import org.corpus_tools.pepper.connectors.PepperConnector;
import org.eclipse.emf.common.util.URI;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

/**
 * An implementation of {@link PepperModuleRunnable} that configures a
 * conversion process for converting a corpus in the TraCES format into the
 * ANNIS format.
 *
 * @author Stephan Druskat <[mail@sdruskat.net](mailto:mail@sdruskat.net)>
 * 
 */
public class TraCESToANNISModuleRunnable extends PepperModuleRunnable {

	private Properties orderRelationAdderProps;
	private Properties annisExporterProps;
	private String name;
	private final String dateString;
	private static final String OUTPUT_PATH = "./output";

	private static final String OUTPUT_FORMAT = "annis";

	public TraCESToANNISModuleRunnable(boolean cancelable, PepperConnector pepperConnector, String corpusDirectoryPath) {
		super(cancelable, pepperConnector, corpusDirectoryPath);
		LocalDateTime date = LocalDateTime.now();
		this.dateString = date.getYear() + "-" + date.getMonthValue() + "-" + date.getDayOfMonth() + "-" + date.getHour() + "-" + date.getMinute() + "-" + date.getSecond();
		orderRelationAdderProps = new Properties();
		orderRelationAdderProps.put("segmentation-layers", "{TR,FIDED}");
		annisExporterProps = new Properties();
		annisExporterProps.put("clobber.visualisation", false);
		name = null;
		File dir = new File(corpusDirectoryPath);

		File[] file = dir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String filename) {
				return filename.endsWith(".json");
			}
		});
		if (file.length > 1) {
			MessageDialog.openInformation(Display.getCurrent().getActiveShell(), "Only one corpus per directory!",
					"The chose directory contains more than one TraCES corpus file, or more than one JSON file.\n\n"
					+ "However, only one TraCES corpus file is permitted per directory.");
		}
		else if (file.length == 1) {
			File corpusFile = file[0];
			String[] split = corpusFile.getName().split("\\.");
			if (split.length > 1) {
				name = split[0];
				// Cut off trailing "EA" (TraCES standard name ending)
				if (name.endsWith("EA")) {
					int nameLength = name.length();
					name = name.substring(0, nameLength - 2);
				}
			}
		}
		else {
			MessageDialog.openInformation(Display.getCurrent().getActiveShell(), "No corpus found!",
					"The directory does not seem to contain a TraCES corpus file (JSON)!");
		}
		annisExporterProps.put("corpusName", name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sdruskat.peppergrinder.rcp.conversion.PepperModuleRunnable#
	 * createImporterParams()
	 */
	@Override
	protected StepDesc createImporterParams() {
		StepDesc stepDesc = new StepDesc();
		stepDesc.setName("TraCESImporter");
		stepDesc.setVersion("1.0.0.SNAPSHOT");
		stepDesc.setCorpusDesc(new CorpusDesc()
				.setCorpusPath(URI.createFileURI(new File(this.corpusDirectoryPath).getAbsolutePath())));
		stepDesc.setModuleType(MODULE_TYPE.IMPORTER);
		return (stepDesc);
	}

	/* (non-Javadoc)
	 * @see net.sdruskat.peppergrinder.rcp.conversion.PepperModuleRunnable#createManipulatorParams()
	 */
	@Override
	protected StepDesc createManipulatorParams() {
		StepDesc stepDesc = new StepDesc();
		stepDesc.setName("OrderRelationAdder");
		stepDesc.setVersion("1.0.2.SNAPSHOT");
		stepDesc.setModuleType(MODULE_TYPE.MANIPULATOR);
		stepDesc.setProps(orderRelationAdderProps);
		return (stepDesc);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sdruskat.peppergrinder.rcp.conversion.PepperModuleRunnable#
	 * createExporterParams()
	 */
	@Override
	protected StepDesc createExporterParams() {
		StepDesc stepDesc = new StepDesc();
		stepDesc.setCorpusDesc(new CorpusDesc());
		stepDesc.getCorpusDesc().setCorpusPath(URI.createFileURI(new File("").getAbsolutePath() + getUnprefixedOutputPath() + "/" + name + "/" + getOutputFormat() + "/" + getDateString()));
		stepDesc.setName("ANNISExporter");
		stepDesc.setVersion("2.0.9.SNAPSHOT");
		stepDesc.setModuleType(MODULE_TYPE.EXPORTER);
		stepDesc.setProps(annisExporterProps);
		return (stepDesc);
	}

	String getDateString() {
		return dateString;
	}

	/**
	 * @return The name of the corpus that is currently being converted.
	 */
	public final String getName() {
		return name;
	}

	public String getOutputPath() {
		return OUTPUT_PATH;
	}
	
	private String getUnprefixedOutputPath() {
		return OUTPUT_PATH.substring(1);
	}

	public String getOutputFormat() {
		return OUTPUT_FORMAT;
	}

}
