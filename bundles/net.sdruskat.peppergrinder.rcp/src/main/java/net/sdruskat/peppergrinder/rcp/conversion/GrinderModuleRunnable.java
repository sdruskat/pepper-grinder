/**
 * 
 */
package net.sdruskat.peppergrinder.rcp.conversion;

import java.io.File;
import java.util.Properties;

import org.corpus_tools.pepper.common.CorpusDesc;
import org.corpus_tools.pepper.common.MODULE_TYPE;
import org.corpus_tools.pepper.common.StepDesc;
import org.corpus_tools.pepper.connectors.PepperConnector;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.common.util.URI;

/**
 * // TODO Add description
 *
 * @author Stephan Druskat <[mail@sdruskat.net](mailto:mail@sdruskat.net)>
 * 
 */
public class GrinderModuleRunnable extends PepperModuleRunnable {

	private Properties tRACesProperties = null;

	public GrinderModuleRunnable(boolean cancelable, PepperConnector pepperConnector, String corpusDirectoryPath) {
		super(cancelable, pepperConnector, corpusDirectoryPath);
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

	/* (non-Javadoc)
	 * @see net.sdruskat.peppergrinder.rcp.conversion.PepperModuleRunnable#createExporterParams()
	 */
	@Override
	protected StepDesc createExporterParams() {
		StepDesc stepDesc = new StepDesc();
		stepDesc.setCorpusDesc(new CorpusDesc());
		stepDesc.getCorpusDesc().setCorpusPath(URI.createFileURI(Platform.getLocation().toFile().getAbsolutePath() + "/annis-files"));
		stepDesc.setName("ANNISExporter");
		stepDesc.setVersion("2.0.9.SNAPSHOT");
		stepDesc.setModuleType(MODULE_TYPE.EXPORTER);
		return (stepDesc);
	}

}
