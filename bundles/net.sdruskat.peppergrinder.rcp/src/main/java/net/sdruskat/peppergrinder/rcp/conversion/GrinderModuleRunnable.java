/**
 * 
 */
package net.sdruskat.peppergrinder.rcp.conversion;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Properties;

import org.corpus_tools.pepper.common.CorpusDesc;
import org.corpus_tools.pepper.common.MODULE_TYPE;
import org.corpus_tools.pepper.common.StepDesc;
import org.corpus_tools.pepper.connectors.PepperConnector;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.common.util.URI;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

/**
 * // TODO Add description
 *
 * @author Stephan Druskat <[mail@sdruskat.net](mailto:mail@sdruskat.net)>
 * 
 */
public class GrinderModuleRunnable extends PepperModuleRunnable {

	private Properties tRACesProperties;
	private Properties orderRelationAdderProps;
	private Properties annisExporterProps;
	private String name;

	public GrinderModuleRunnable(boolean cancelable, PepperConnector pepperConnector, String corpusDirectoryPath) {
		super(cancelable, pepperConnector, corpusDirectoryPath);
		orderRelationAdderProps = new Properties();
		orderRelationAdderProps.put("segmentation-layers", "{TR,FIDED}");
		annisExporterProps = new Properties();
		annisExporterProps.put("clobber.visualisation", false);
		name = null;
		File dir = new File(corpusDirectoryPath);

        File[] file = dir.listFiles(new FilenameFilter() { 
                 public boolean accept(File dir, String filename)
                      { return filename.endsWith(".json"); }
        } );
        if (file.length > 1) {
        	MessageDialog.openInformation(Display.getCurrent().getActiveShell(), "Only one corpus per directory!", "Only one corpus per directory!");
        }
        else if (file.length == 1) {
        	File corpusFile = file[0];
        	System.out.println("FILE " + corpusFile.getName());
        	String[] split = corpusFile.getName().split("\\.");
        	System.out.println("ARRAY " + Arrays.toString(split));
        	if (split.length > 1) {
    			name = split[0];
    		}
        }
        else {
        	MessageDialog.openInformation(Display.getCurrent().getActiveShell(), "No corpus found!", "Couldn't find a corpus in directory " + corpusDirectoryPath + "!");
        }
        System.out.println("NAME " + name);
//		String[] split = corpusDirectoryPath.split(".");
//		if (split.length > 1) {
//			name = split[split.length - 2];
//		}
		annisExporterProps.put("corpusName", name );
	}

	/* (non-Javadoc)
	 * @see net.sdruskat.peppergrinder.rcp.conversion.PepperModuleRunnable#createImporterParams()
	 */
	@Override
	protected StepDesc createImporterParams() {
		StepDesc stepDesc = new StepDesc();
		stepDesc.setName("TraCESImporter");
		stepDesc.setVersion("1.0.0.SNAPSHOT");
		stepDesc.setCorpusDesc(new CorpusDesc().setCorpusPath(URI.createFileURI(new File(this.corpusDirectoryPath).getAbsolutePath())));
		stepDesc.setModuleType(MODULE_TYPE.IMPORTER);
//		stepDesc.setProps(tRACesProperties);
		return (stepDesc);
	}
	
	@Override
	protected StepDesc createManipulatorParams() {
		StepDesc stepDesc = new StepDesc();
		stepDesc.setName("OrderRelationAdder");
		stepDesc.setVersion("1.0.2.SNAPSHOT");
		stepDesc.setModuleType(MODULE_TYPE.MANIPULATOR);
		stepDesc.setProps(orderRelationAdderProps);
		return (stepDesc);
	}

	/* (non-Javadoc)
	 * @see net.sdruskat.peppergrinder.rcp.conversion.PepperModuleRunnable#createExporterParams()
	 */
	@Override
	protected StepDesc createExporterParams() {
		StepDesc stepDesc = new StepDesc();
		stepDesc.setCorpusDesc(new CorpusDesc());
		stepDesc.getCorpusDesc().setCorpusPath(URI.createFileURI(new File("").getAbsolutePath() + "/ANNIS-OUTPUT"));
		stepDesc.setName("ANNISExporter");
		stepDesc.setVersion("2.0.9.SNAPSHOT");
		stepDesc.setModuleType(MODULE_TYPE.EXPORTER);
		stepDesc.setProps(annisExporterProps);
		return (stepDesc);
	}

	/**
	 * @return the name
	 */
	public final String getName() {
		return name;
	}

}
