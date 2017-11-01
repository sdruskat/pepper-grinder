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
package net.sdruskat.peppergrinder.rcp.pepper;

import org.corpus_tools.pepper.common.Pepper;
import org.corpus_tools.pepper.connectors.PepperConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class that starts and initializes a Pepper instance.
 *
 * @author Stephan Druskat <[mail@sdruskat.net](mailto:mail@sdruskat.net)>
 * 
 */
public class GrinderPepperStarter {
	
	private final static Logger log = LoggerFactory.getLogger(GrinderPepperStarter.class);
	
	private PepperConnector pepper = null;
	
	/**
	 * Configures and sets a new Pepper instance, 
	 * i.e., an instance of {@link GrinderPepperOSGiConnector}.
	 * 
	 */
	public void startPepper() {
		GrinderPepperOSGiConnector pepper = null;
		GrinderPepperConfiguration pepperProps = null;
		
		pepperProps = new GrinderPepperConfiguration();
		log.info("Loading Pepper properties via the object " + pepperProps + ".");
		pepperProps.load();
		
		pepper = new GrinderPepperOSGiConnector();
		pepper.setConfiguration(pepperProps);
		
		setPepper(pepper);
	}
	
	/**
	 * Configures and sets a new Pepper instance, 
	 * i.e., an instance of {@link GrinderPepperOSGiConnector}
	 * and bridges it to the OSGi platform.
	 */
	public void startPepperAndBridgeOSGi() {
		GrinderPepperOSGiConnector pepper = null;
		GrinderPepperConfiguration pepperProps = null;
		
		pepperProps = new GrinderPepperConfiguration();
		log.info("Loading Pepper properties via the object " + pepperProps + ".");
		pepperProps.load();
		
		pepper = new GrinderPepperOSGiConnector();
		pepper.setConfiguration(pepperProps);
		
		pepper.addSharedPackage("org.corpus_tools.salt", "3");
	    pepper.addSharedPackage("org.corpus_tools.salt.common", "3");
	    pepper.addSharedPackage("org.corpus_tools.salt.core", "3");
	    pepper.addSharedPackage("org.corpus_tools.salt.graph", "3");
	    pepper.addSharedPackage("org.corpus_tools.salt.util", "3");             
	    pepper.init();
	    
		setPepper(pepper);
	}
	
	/**
	 * @return the Pepper instance (as a {@link PepperConnector} object).
	 */
	public PepperConnector getPepper() {
		return pepper;
	}

	/**
	 * @param pepper The {@link Pepper} to set
	 */
	public void setPepper(PepperConnector pepper) {
		this.pepper = pepper;
		if (!getPepper().isInitialized()) {
			getPepper().init();
		}
	}

	/**
	 * Passes the call to an instance of {@link GrinderPepperOSGiConnector},
	 * which in turn instantiates a {@link GrinderMavenAccessor}, passing
	 * itself as argument. Must be called to be able to resolve Maven
	 * dependencies for modules, etc.
	 *
	 */
	public void initMavenAccessor() {
		((GrinderPepperOSGiConnector) getPepper()).initializeMavenAccessor();
	}

}
