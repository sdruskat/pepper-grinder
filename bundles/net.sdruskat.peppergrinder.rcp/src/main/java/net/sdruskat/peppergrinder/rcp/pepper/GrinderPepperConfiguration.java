package net.sdruskat.peppergrinder.rcp.pepper;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
import org.corpus_tools.pepper.cli.PepperStarterConfiguration;
import org.corpus_tools.pepper.common.PepperConfiguration;
import org.corpus_tools.pepper.common.PepperUtil;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Constants;

import com.google.common.base.Splitter;

public class GrinderPepperConfiguration extends PepperConfiguration {

	
	/**
	 * Defines a static logger variable so that it references the {@link org.apache.logging.log4j.Logger} instance named "AtomicPepperConfiguration".
	 */
//	private static final Logger log = LogManager.getLogger(AtomicPepperConfiguration.class);
	
	/** Folder containing configuration files for Pepper */
	public static final String FOLDER_PEPPER_CONF = "configuration";
	

	/**
	 * Loads the "pepper.properties" file via the 
	 * {@link Properties#load(java.io.InputStream)} mechanism ultimately
	 * at the end of the inheritance chain.
	 * 
	 */
	public void load() {
		File atomicHome = findAtomicHome();
		File propFile = new File(atomicHome.getAbsolutePath() + "/" + GrinderPepperConfiguration.FOLDER_PEPPER_CONF + "/" + PepperStarterConfiguration.FILE_PEPPER_PROP + "/");
		try {
			load(propFile);
		}
		catch (Exception e) {
//			log.error("Could not load Pepper configuration for Atomic!", e);
		}
	}

	/**
	 * Gets the "Atomic home" folder, i.e., the folder where the Atomic executable is located.
	 *
	 * @return pepperHome The pepperHome File
	 */
	private File findAtomicHome() {
		File atomicHome = null;
		URL atomicHomeURL = null;
		atomicHomeURL = Platform.getInstallLocation().getURL();
		atomicHome = new File(atomicHomeURL.getFile());
		return atomicHome;
	}

	/**
	 * Returns the path for the OSGi plugins folder.
	 * 
	 * @return plugIn path
	 */
	public String getPlugInPath() {
		return (findAtomicHome().getAbsolutePath() + "/" + this.getProperty(PepperStarterConfiguration.PROP_PLUGIN_PATH));
	}

	/**
	 * Returns the dropin paths for OSGi bundles.
	 * FIXME: These are not used in the contenxt of
	 * Atomic.
	 *
	 * @return List<String> the dropin paths
	 */
	public List<String> getDropInPaths() {
			String rawList = this.getProperty(PepperStarterConfiguration.PROP_DROPIN_PATHS);
			if (rawList != null) {
				Iterator<String> it
					= Splitter.on(',').trimResults().omitEmptyStrings().split(rawList).iterator();
				List<String> result = new ArrayList<>();
				while (it.hasNext()) {
					result.add(it.next());
				}
				return result;
			}
			return null;
	}
	
	/**
	 * Returns a temporary path, where the entire system and all modules can
	 * store temp files. If no temp folder is given by configuration file, the
	 * default temporary folder given by the operating system is used.
	 * 
	 * @return path, where to store temporary files
	 */
	public File getTempPath() {
		String tmpFolderStr = getProperty(PROP_TEMP_FOLDER);
		File tmpFolder = null;
		if (tmpFolderStr != null) {
			tmpFolderStr = tmpFolderStr + "/pepper/";
			tmpFolder = new File(tmpFolderStr);
			if (!tmpFolder.exists()) {
				if (!tmpFolder.mkdirs()) {
//					log.warn("Cannot create folder {}. ", tmpFolder);
				}
			}
		} else {
			tmpFolder = PepperUtil.getTempFile();
		}
		return (tmpFolder);
	}

	/**
	 * Returns the content of property {@link #PROP_OSGI_SHAREDPACKAGES}.
	 * 
	 * @return plugIn path
	 */
	public String getSharedPackages() {
		return (this.getProperty("pepper." + Constants.FRAMEWORK_SYSTEMPACKAGES));
	}
}
