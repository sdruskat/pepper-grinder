/**
 * 
 */
package net.sdruskat.peppergrinder.rcp.pepper;

//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
import org.corpus_tools.pepper.common.Pepper;
import org.corpus_tools.pepper.connectors.PepperConnector;

/**
 * // TODO Add description
 *
 * @author Stephan Druskat <[mail@sdruskat.net](mailto:mail@sdruskat.net)>
 * 
 */
public class GrinderPepperStarter {
	
	private PepperConnector pepper = null;
	
	public void startPepper() {
		GrinderPepperOSGiConnector pepper = null;
		GrinderPepperConfiguration pepperProps = null;
		
		pepperProps = new GrinderPepperConfiguration();
//		log.trace("Loading Pepper properties via the object {}.", pepperProps);
		pepperProps.load();
		
		pepper = new GrinderPepperOSGiConnector();
//		log.trace("Setting the Pepper properties ({}) as configuration in the {} object {}.", pepperProps, AtomicPepperOSGiConnector.class.getName(), pepper);
		pepper.setConfiguration(pepperProps);
		
		setPepper(pepper);
	}
	
	public void startPepperAndBridgeOSGi() { // NO_UCD (unused code)
		GrinderPepperOSGiConnector pepper = null;
		GrinderPepperConfiguration pepperProps = null;
		
		pepperProps = new GrinderPepperConfiguration();
//		log.trace("Loading Pepper properties via the object {}.", pepperProps);
		pepperProps.load();
		
		pepper = new GrinderPepperOSGiConnector();
//		log.trace("Setting the Pepper properties ({}) as configuration in the {} object {}.", pepperProps, AtomicPepperOSGiConnector.class.getName(), pepper);
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
	 * Passes the call to an instance of {@link AtomicPepperOSGiConnector},
	 * which in turn instantiates a {@link AtomicMavenAccessor}, passing
	 * itself as argument. Must be called to be able to resolve Maven
	 * dependencies for modules, etc.
	 *
	 */
	public void initMavenAccessor() {
		((GrinderPepperOSGiConnector) getPepper()).initializeMavenAccessor();
	}

}
