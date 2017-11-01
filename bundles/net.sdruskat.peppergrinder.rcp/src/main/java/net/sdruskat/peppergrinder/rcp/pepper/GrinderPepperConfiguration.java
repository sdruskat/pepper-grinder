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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Splitter;

/**
 * Represents a configuration setup for Pepper suitable for Pepper Grinder.
 *
 * @author Stephan Druskat <[mail@sdruskat.net](mailto:mail@sdruskat.net)>
 * 
 */
public class GrinderPepperConfiguration extends PepperConfiguration {

	private static final Logger log = LoggerFactory.getLogger(GrinderPepperConfiguration.class);

	private static final long serialVersionUID = 1L;
	/** Folder containing configuration files for Pepper */
	public static final String FOLDER_PEPPER_CONF = "configuration";

	/**
	 * Loads the "pepper.properties" file via the
	 * {@link Properties#load(java.io.InputStream)} mechanism ultimately at the
	 * end of the inheritance chain.
	 * 
	 */
	public void load() {
		File grinderHome = findGrinderHome();
		File propFile = new File(grinderHome.getAbsolutePath() + "/" + GrinderPepperConfiguration.FOLDER_PEPPER_CONF
				+ "/" + PepperStarterConfiguration.FILE_PEPPER_PROP + "/");
		try {
			load(propFile);
		}
		catch (Exception e) {
			log.error("Could not load Pepper configuration for Pepper Grinder!");
		}
	}

	/**
	 * Gets the "Pepper Grinder home" folder, i.e., the folder where the Pepper
	 * Grinder executable is located.
	 *
	 * @return grinderHome The grinderHome File
	 */
	private File findGrinderHome() {
		File grinderHome = null;
		URL grinderHomeURL = null;
		grinderHomeURL = Platform.getInstallLocation().getURL();
		grinderHome = new File(grinderHomeURL.getFile());
		return grinderHome;
	}

	/**
	 * Returns the path for the OSGi plugins folder.
	 * 
	 * @return plugIn path
	 */
	public String getPlugInPath() {
		return (findGrinderHome().getAbsolutePath() + "/"
				+ this.getProperty(PepperStarterConfiguration.PROP_PLUGIN_PATH));
	}

	/**
	 * Returns the dropin paths for OSGi bundles. FIXME: These are not used in
	 * the context of Pepper Grinder.
	 *
	 * @return List<String> the dropin paths
	 */
	public List<String> getDropInPaths() {
		String rawList = this.getProperty(PepperStarterConfiguration.PROP_DROPIN_PATHS);
		if (rawList != null) {
			Iterator<String> it = Splitter.on(',').trimResults().omitEmptyStrings().split(rawList).iterator();
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
					log.warn("Cannot create folder " + tmpFolder + ".");
				}
			}
		}
		else {
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
