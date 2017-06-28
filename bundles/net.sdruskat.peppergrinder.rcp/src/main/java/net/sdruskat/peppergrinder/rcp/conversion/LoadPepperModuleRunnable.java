package net.sdruskat.peppergrinder.rcp.conversion;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.filefilter.SuffixFileFilter;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
import org.corpus_tools.pepper.common.Pepper;
import org.corpus_tools.pepper.connectors.PepperConnector;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkUtil;

import net.sdruskat.peppergrinder.rcp.pepper.GrinderPepperConfiguration;

public class LoadPepperModuleRunnable implements IRunnableWithProgress{

	/** 
	 * Defines a static logger variable so that it references the {@link org.apache.logging.log4j.Logger} instance named "LoadPepperModuleRunnable".
	 */
//	private static final Logger log = LogManager.getLogger(LoadPepperModuleRunnable.class);
	private PepperConnector pepper;
	

	/**
	 * @param pepper The {@link Pepper} instance to use
	 */
	public LoadPepperModuleRunnable(PepperConnector pepper) {
		this.pepper = pepper;
	}


	/* 
	 * @copydoc @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		monitor.beginTask("Loading available Pepper modules ...", IProgressMonitor.UNKNOWN);
//		log.info("Loading Pepper modules.");
		// Load the configuration
		GrinderPepperConfiguration configuration = (GrinderPepperConfiguration) getPepper().getConfiguration();
		configuration.load();
		String path = configuration.getPlugInPath();
		// Find all JAR files in pepper-modules directory
		File[] fileLocations = new File(path).listFiles((FilenameFilter) new SuffixFileFilter(".jar"));
		List<Bundle> moduleBundles = new ArrayList<>();
		if (fileLocations != null) {
			// Install JARs as OSGi bundles
			for (File bundleJar : fileLocations) {
				if (bundleJar.isFile() && bundleJar.canRead()) {
					URI bundleURI = bundleJar.toURI();
					Bundle bundle = null;
					try {
						BundleContext bundleContext = FrameworkUtil.getBundle(this.getClass())
                        .getBundleContext();
						
						bundle = bundleContext.installBundle(bundleURI.toString());
						moduleBundles.add(bundle);
						System.out.println("\n\nINSTALLED " + bundle.getSymbolicName() + "\n\n");
					}
					catch (BundleException e) {
//						log.debug("Could not install bundle {}!", bundleURI.toString());
					}
				}
			}
			// Start bundles
			for (Bundle bundle : moduleBundles) {
				if (bundle.getState() != Bundle.ACTIVE) {
					try {
						bundle.start();
					}
					catch (BundleException e) {
//						log.debug("Could not start bundle {}!", bundle.getSymbolicName());
					}
				}
			}
		}
		
	}


	/**
	 * @return the pepper
	 */
	private PepperConnector getPepper() {
		return pepper;
	}
	
}
