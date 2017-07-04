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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
import org.apache.maven.repository.internal.DefaultArtifactDescriptorReader;
import org.apache.maven.repository.internal.DefaultVersionRangeResolver;
import org.apache.maven.repository.internal.DefaultVersionResolver;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.apache.maven.repository.internal.SnapshotMetadataGeneratorFactory;
import org.apache.maven.repository.internal.VersionsMetadataGeneratorFactory;
import org.codehaus.plexus.util.FileUtils;
import org.eclipse.aether.AbstractRepositoryListener;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositoryEvent;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.artifact.DefaultArtifactType;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.collection.CollectResult;
import org.eclipse.aether.collection.DependencyCollectionException;
import org.eclipse.aether.collection.DependencyGraphTransformer;
import org.eclipse.aether.collection.DependencyManager;
import org.eclipse.aether.collection.DependencySelector;
import org.eclipse.aether.collection.DependencyTraverser;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.impl.ArtifactDescriptorReader;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.impl.MetadataGeneratorFactory;
import org.eclipse.aether.impl.VersionRangeResolver;
import org.eclipse.aether.impl.VersionResolver;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.LocalRepositoryManager;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.VersionRangeRequest;
import org.eclipse.aether.resolution.VersionRangeResolutionException;
import org.eclipse.aether.resolution.VersionRangeResult;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transfer.AbstractTransferListener;
import org.eclipse.aether.transfer.ArtifactNotFoundException;
import org.eclipse.aether.transfer.MetadataNotFoundException;
import org.eclipse.aether.transfer.TransferEvent;
import org.eclipse.aether.transfer.TransferResource;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.eclipse.aether.util.artifact.DefaultArtifactTypeRegistry;
import org.eclipse.aether.util.graph.manager.ClassicDependencyManager;
import org.eclipse.aether.util.graph.selector.AndDependencySelector;
import org.eclipse.aether.util.graph.selector.ExclusionDependencySelector;
import org.eclipse.aether.util.graph.selector.OptionalDependencySelector;
import org.eclipse.aether.util.graph.selector.ScopeDependencySelector;
import org.eclipse.aether.util.graph.transformer.ChainedDependencyGraphTransformer;
import org.eclipse.aether.util.graph.transformer.ConflictResolver;
import org.eclipse.aether.util.graph.transformer.JavaDependencyContextRefiner;
import org.eclipse.aether.util.graph.transformer.JavaScopeDeriver;
import org.eclipse.aether.util.graph.transformer.JavaScopeSelector;
import org.eclipse.aether.util.graph.transformer.NearestVersionSelector;
import org.eclipse.aether.util.graph.transformer.SimpleOptionalitySelector;
import org.eclipse.aether.util.graph.traverser.FatArtifactTraverser;
import org.eclipse.aether.util.repository.SimpleArtifactDescriptorPolicy;
import org.eclipse.aether.util.version.GenericVersionScheme;
import org.eclipse.aether.version.InvalidVersionSpecificationException;
import org.eclipse.aether.version.Version;
import org.eclipse.aether.version.VersionRange;
import org.eclipse.aether.version.VersionScheme;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

import net.sdruskat.peppergrinder.rcp.LogManager;

/**
 * // TODO Add description
 *
 * @author Stephan Druskat <[mail@sdruskat.net](mailto:mail@sdruskat.net)>
 * 
 */
public class GrinderMavenAccessor {

	private static final LogManager log = LogManager.INSTANCE;

	/** contains the path to the blacklist file. */
	private static final String BLACKLIST_PATH = "./configuration/dependencies/blacklist.cfg";
	/** this String contains the artifactId of pepper-framework. */
	public static final String ARTIFACT_ID_PEPPER_FRAMEWORK = "pepper-framework";
	/** this String contains the artifactId of pepper-parentModule. */
	public static final String ARTIFACT_ID_PEPPER_PARENT = "pepper-parentModule";
	/** path to korpling maven repo */
	public static final String KORPLING_MAVEN_REPO = "http://korpling.german.hu-berlin.de/maven2/";
	/** path to maven central */
	public static final String CENTRAL_REPO = "http://central.maven.org/maven2/";
	/** Path to Sonatype Snapshots Maven repo */
	public static final String SONATYPE_SNAPSHOTS_REPO = "https://oss.sonatype.org/content/repositories/snapshots/";

	/** flag which is added to the blacklist entry of a dependency – a FINAL dependency can not be overridden */
	private static enum STATUS {
		OVERRIDABLE, FINAL;
	}

	/** delimiter for artifact strings */
	private static final String DELIMITER = ":";

	private final GrinderPepperOSGiConnector grinderPepperOSGiConnector;

	private RepositorySystem mvnSystem = null;

	/**
	 * this {@link Set} stores all dependencies, that are installed or forbidden. The format of the {@link String}s is GROUP_ID:ARTIFACT_ID:EXTENSION:VERSION, which is also the output format of {@link Dependency#getArtifact()#toString()}.
	 */
	private Set<String> forbiddenFruits = null;

	/** this Map contains all repos already used in this pepper session, key is url, value is repo */
	HashMap<String, RemoteRepository> repos = null;
	/** maven/aether utility used to build Objects of class {@link RemoteRepository}. */
	RemoteRepository.Builder repoBuilder = null;
	/** this map contains already collected pepper parent dependencies (version-String->List<Dependency>) */
	private HashMap<String, List<Dependency>> parentDependencies = null;
	/** path to temporary repository */
	private final String PATH_LOCAL_REPO;

	/**
	 * @param grinderPepperOSGiConnector
	 */
	public GrinderMavenAccessor(GrinderPepperOSGiConnector atomicPepperOSGiConnector) {
		this.grinderPepperOSGiConnector = atomicPepperOSGiConnector;
		{
			DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
			locator.addService(ArtifactDescriptorReader.class, DefaultArtifactDescriptorReader.class);
			locator.addService(VersionResolver.class, DefaultVersionResolver.class);
			locator.addService(VersionRangeResolver.class, DefaultVersionRangeResolver.class);
			locator.addService(MetadataGeneratorFactory.class, SnapshotMetadataGeneratorFactory.class);
			locator.addService(MetadataGeneratorFactory.class, VersionsMetadataGeneratorFactory.class);
			locator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
			locator.addService(TransporterFactory.class, FileTransporterFactory.class);
			locator.addService(TransporterFactory.class, HttpTransporterFactory.class);
			mvnSystem = locator.getService(RepositorySystem.class);
		}
		repoBuilder = new RemoteRepository.Builder("", "default", "");
		repos = new HashMap<>();
		forbiddenFruits = new HashSet<>();
		parentDependencies = new HashMap<>();
		PATH_LOCAL_REPO = atomicPepperOSGiConnector.getGrinderPepperConfiguration().getTempPath().getAbsolutePath().concat("/local-repo/");
		try {
			File lr = new File(PATH_LOCAL_REPO);
			if (lr.exists()) {
				FileUtils.deleteDirectory(lr);
			}
		}
		catch (IOException e) {
			log.warn("Failed to clean local repository.");
		}
		init();
		initDependencies();
	}

	/**
	 * This method tries to read the blacklist file, if it already exists
	 */
	private void init() {
		/* init Maven utils */
		/* read/write dependency blacklist */
		File blacklistFile = new File(BLACKLIST_PATH);
		if (blacklistFile.exists()) {
			try {
				FileReader fR = new FileReader(blacklistFile);
				BufferedReader reader = new BufferedReader(fR);
				String line = reader.readLine();
				while (line != null) {
					forbiddenFruits.add(line);
					line = reader.readLine();
				}
				reader.close();
				fR.close();
			}
			catch (IOException e) {
				log.warn("Could not read blacklist file.");
			}
		}
	}

	/**
	 * this method initializes the dependency blacklist
	 */
	private boolean initDependencies() {
		String frameworkVersion = grinderPepperOSGiConnector.getFrameworkVersion();
		if (frameworkVersion.split("\\.").length > 3) { // I.e., Is a snapshot version
			String[] segments = frameworkVersion.split("\\.");
			frameworkVersion = (segments[0] + "." + segments[1] + "." + segments[2] + "-SNAPSHOT");			
		}
		RemoteRepository repo = getRepo("korpling", SONATYPE_SNAPSHOTS_REPO);
		getRepo("central", CENTRAL_REPO);
//		getRepo("sonatype-snapshots", SONATYPE_SNAPSHOTS_REPO);

		if (forbiddenFruits.isEmpty()) {
			log.info("Configuring update mechanism ...");
			/* maven access utils */
			Artifact pepArt = new DefaultArtifact("org.corpus-tools", ARTIFACT_ID_PEPPER_PARENT, "pom", frameworkVersion);

			DefaultRepositorySystemSession session = getNewSession();

			/* utils for dependency collection */
			CollectRequest collectRequest = new CollectRequest();
			collectRequest.setRoot(new Dependency(pepArt, ""));
			collectRequest.setRepositories(null);
			collectRequest.addRepository(repo);
			collectRequest.addRepository(repos.get(CENTRAL_REPO));
			collectRequest.addRepository(repos.get(SONATYPE_SNAPSHOTS_REPO));
			collectRequest.setRootArtifact(pepArt);
			try {
				CollectResult collectResult = mvnSystem.collectDependencies(session, collectRequest);
				List<Dependency> allDeps = getAllDependencies(collectResult.getRoot(), false);
				parentDependencies.put(frameworkVersion.replace("-SNAPSHOT", ""), allDeps);
				Bundle bundle = null;
				STATUS status = null;
				for (Dependency dependency : allDeps) {
					bundle = grinderPepperOSGiConnector.getBundle(dependency.getArtifact().getGroupId(), dependency.getArtifact().getArtifactId(), null);
					status = bundle == null || bundle.getHeaders().get("Bundle-SymbolicName").contains("singleton:=true") ? STATUS.FINAL : STATUS.OVERRIDABLE;
					forbiddenFruits.add(dependency.getArtifact().toString().concat(DELIMITER).concat(status.toString()).concat(DELIMITER).concat(bundle == null ? "" : bundle.getSymbolicName()));
				}
				write2Blacklist();
				collectResult = null;

			}
			catch (DependencyCollectionException e) {
				log.warn("An error occured initializing the update mechanism. Please check your internet connection.");
				return false;
			}
			session = null;
			pepArt = null;
			collectRequest = null;
		}
		write2Blacklist();
		return true;
	}

	private DefaultRepositorySystemSession getNewSession() {
		DefaultRepositorySystemSession session = new DefaultRepositorySystemSession();
		LocalRepository localRepo = new LocalRepository(PATH_LOCAL_REPO);
		LocalRepositoryManager repoManager = mvnSystem.newLocalRepositoryManager(session, localRepo);
		session.setLocalRepositoryManager(repoManager);
		session.setRepositoryListener(repoListener);
		session.setTransferListener(transferListener);

		DependencyTraverser depTraverser = new FatArtifactTraverser();
		session.setDependencyTraverser(depTraverser);

		DependencyManager depManager = new ClassicDependencyManager();
		session.setDependencyManager(depManager);

		DependencySelector depFilter = new AndDependencySelector(new ScopeDependencySelector("test", "Test"/* , "provided" */ ), new OptionalDependencySelector(), new ExclusionDependencySelector());
		session.setDependencySelector(depFilter);

		DependencyGraphTransformer transformer = new ConflictResolver(new NearestVersionSelector(), new JavaScopeSelector(), new SimpleOptionalitySelector(), new JavaScopeDeriver());
		new ChainedDependencyGraphTransformer(transformer, new JavaDependencyContextRefiner());
		session.setDependencyGraphTransformer(transformer);

		DefaultArtifactTypeRegistry stereotypes = new DefaultArtifactTypeRegistry();
		stereotypes.add(new DefaultArtifactType("pom"));
		stereotypes.add(new DefaultArtifactType("maven-plugin", "jar", "", "java"));
		stereotypes.add(new DefaultArtifactType("jar", "jar", "", "java"));
		stereotypes.add(new DefaultArtifactType("zip", "zip", "", "java"));
		stereotypes.add(new DefaultArtifactType("ejb", "jar", "", "java"));
		stereotypes.add(new DefaultArtifactType("ejb-client", "jar", "client", "java"));
		stereotypes.add(new DefaultArtifactType("test-jar", "jar", "tests", "java"));
		stereotypes.add(new DefaultArtifactType("javadoc", "jar", "javadoc", "java"));
		stereotypes.add(new DefaultArtifactType("java-source", "jar", "sources", "java", false, false));
		stereotypes.add(new DefaultArtifactType("war", "war", "", "java", false, true));
		stereotypes.add(new DefaultArtifactType("ear", "ear", "", "java", false, true));
		stereotypes.add(new DefaultArtifactType("rar", "rar", "", "java", false, true));
		stereotypes.add(new DefaultArtifactType("par", "par", "", "java", false, true));
		session.setArtifactTypeRegistry(stereotypes);

		session.setArtifactDescriptorPolicy(new SimpleArtifactDescriptorPolicy(true, true));

		Properties sysProps = System.getProperties();
		session.setSystemProperties(sysProps);
		session.setConfigProperties(sysProps);

		return session;
	}

	/** This listener does not write the maven transfer output. */
	private final MavenTransferListener transferListener = new MavenTransferListener();

	/** This is used to write the Maven output onto our log. The default {@link ConsoleRepositoryListener} writes it directly to System.out */
	private final MavenRepositoryListener repoListener = new MavenRepositoryListener();

	/**
	 * This method checks the provided pepper plugin for updates and triggers the installation process if a newer version is available
	 */
	public boolean update(String groupId, String artifactId, String repositoryUrl, boolean isSnapshot, boolean ignoreFrameworkVersion, Bundle installedBundle) {
		if (forbiddenFruits.isEmpty() && !initDependencies()) {
			log.warn("Update could not be performed, because the pepper dependencies could not be listed.");
			return false;
		}

//		if (log.isTraceEnabled()) {
//			log.trace("Starting update process for " + groupId + ", " + artifactId + ", " + repositoryUrl + ", isSnapshot=" + isSnapshot + ", ignoreFrameworkVersion=" + ignoreFrameworkVersion + ", installedBundle=" + installedBundle);
//		}
//		else {
//			log.info("Starting update process for " + artifactId);
//		}

		String newLine = System.getProperty("line.separator");

		DefaultRepositorySystemSession session = getNewSession();

		boolean update = true; // MUST be born true!

		/* build repository */

		RemoteRepository repo = getRepo("repo", repositoryUrl);

		/* build artifact */
		Artifact artifact = new DefaultArtifact(groupId, artifactId, "zip", "[0,)");

		try {
			/* version range request */
			VersionRangeRequest rangeRequest = new VersionRangeRequest();
			rangeRequest.addRepository(repo);
			rangeRequest.setArtifact(artifact);
			VersionRangeResult rangeResult = mvnSystem.resolveVersionRange(session, rangeRequest);
			rangeRequest.setArtifact(artifact);

			/* utils needed for request */
			ArtifactRequest artifactRequest = new ArtifactRequest();
			artifactRequest.addRepository(repo);
			ArtifactResult artifactResult = null;

			/* get all pepperModules versions listed in the maven repository */
			List<Version> versions = rangeResult.getVersions();
			Collections.reverse(versions);

			/* utils for version comparison */
			Iterator<Version> itVersions = versions.iterator();
			VersionScheme vScheme = new GenericVersionScheme();
			boolean srcExists = false;
			Version installedVersion = installedBundle == null ? vScheme.parseVersion("0.0.0") : vScheme.parseVersion(installedBundle.getVersion().toString().replace(".SNAPSHOT", "-SNAPSHOT"));
			Version newestVersion = null;

			/* compare, if the listed version really exists in the maven repository */
			File file = null;
			while (!srcExists && itVersions.hasNext() && update) {
				newestVersion = itVersions.next();
				artifact = new DefaultArtifact(groupId, artifactId, "zip", newestVersion.toString());
				if (!(artifact.isSnapshot() && !isSnapshot)) {
					update = newestVersion.compareTo(installedVersion) > 0;
					artifactRequest.setArtifact(artifact);
					try {
						artifactResult = mvnSystem.resolveArtifact(session, artifactRequest);
						artifact = artifactResult.getArtifact();
						srcExists = update && artifact.getFile().exists();
						file = artifact.getFile();

					}
					catch (ArtifactResolutionException e) {
						log.warn("Plugin version " + newestVersion + " could not be found in repository. Checking the next lower version ...");
					}
				}
			}
			update &= file != null;// in case of only snapshots in the maven repository vs. isSnapshot=false
			/* if an update is possible/necessary, perform dependency collection and installation */
			if (update) {
				/* create list of necessary repositories */
				Artifact pom = new DefaultArtifact(artifact.getGroupId(), artifact.getArtifactId(), "pom", artifact.getVersion());
				artifactRequest.setArtifact(pom);
				boolean pomReadingErrors = false;
				try {
					artifactResult = null;
					artifactResult = mvnSystem.resolveArtifact(session, artifactRequest);
				}
				catch (ArtifactResolutionException e1) {
					pomReadingErrors = true;
				}
				List<RemoteRepository> repoList = new ArrayList<>();
				repoList.add(repos.get(CENTRAL_REPO));
//				repoList.add(repos.get(SONATYPE_SNAPSHOTS_REPO));
				repoList.add(repos.get(repositoryUrl));
				if (artifactResult != null && artifactResult.getArtifact().getFile().exists()) {
					try {
						SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
						saxParser.parse(artifactResult.getArtifact().getFile().getAbsolutePath(), new POMReader(repoList));
					}
					catch (SAXException | IOException | ParserConfigurationException e) {
						pomReadingErrors = true;
					}
				}
				if (pomReadingErrors) {
//					log.warn("Could not determine all relevant repositories, update might fail. Trying to continue ...");
					repoList.add(repos.get(KORPLING_MAVEN_REPO));
				}

				/* remove older version */
				if (installedBundle != null) {
					try {
						if (!grinderPepperOSGiConnector.remove(installedBundle.getSymbolicName())) {
//							log.warn("Could not remove older version. Update process aborted.");
							return false;
						}
					}
					catch (BundleException | IOException e) {
//						log.warn("An error occured while trying to remove OSGi bundle " + installedBundle.getSymbolicName() + ". This may cause update problems. Trying to continue ...");
					}
				}

				/* utils for file-collection */
				List<Artifact> installArtifacts = new ArrayList<>();
				installArtifacts.add(artifact);

				/* utils for dependency collection */
				CollectRequest collectRequest = new CollectRequest();
				collectRequest.setRoot(new Dependency(artifact, ""));
				collectRequest.setRepositories(repoList);
				CollectResult collectResult = mvnSystem.collectDependencies(session, collectRequest);
				List<Dependency> allDependencies = getAllDependencies(collectResult.getRoot(), true);

				/*
				 * we have to remove the dependencies of pepperParent from the dependency list, since they are (sometimes) not already on the blacklist
				 */
				String parentVersion = null;
				for (int i = 0; i < allDependencies.size() && parentVersion == null; i++) {
					if (ARTIFACT_ID_PEPPER_FRAMEWORK.equals(allDependencies.get(i).getArtifact().getArtifactId())) {
						parentVersion = allDependencies.get(i).getArtifact().getVersion();
					}
				}
				if (parentVersion == null) {
//					log.warn(artifactId + ": Could not perform update: pepper-parent version could not be determined.");
					return false;
				}
				VersionRange range = isCompatiblePlugin(parentVersion);
				if (!ignoreFrameworkVersion && range != null) {
					log.info((new StringBuilder()).append("No update was performed because of a version incompatibility according to pepper-framework: ").append(newLine).append(artifactId).append(" only supports ").append(range.toString()).append(", but ").append(grinderPepperOSGiConnector.getFrameworkVersion()).append(" is installed!").append(newLine).append("You can make pepper ignore this by using \"update").append(isSnapshot ? " snapshot " : " ").append("iv ").append(artifactId).append("\"").toString());
					return false;
				}
				allDependencies = cleanDependencies(allDependencies, session, parentVersion);
				Bundle bundle = null;
				Dependency dependency = null;
				// in the following we ignore the first dependency (i=0), because it is the module itself
				for (int i = 1; i < allDependencies.size(); i++) {
					dependency = allDependencies.get(i);
					if (!ARTIFACT_ID_PEPPER_FRAMEWORK.equals(dependency.getArtifact().getArtifactId())) {
						artifactRequest = new ArtifactRequest();
						artifactRequest.setArtifact(dependency.getArtifact());
						artifactRequest.setRepositories(repoList);
						try {
							artifactResult = mvnSystem.resolveArtifact(session, artifactRequest);
							installArtifacts.add(artifactResult.getArtifact());
						}
						catch (ArtifactResolutionException e) {
							log.warn("Artifact " + dependency.getArtifact().getArtifactId() + " could not be resolved. Dependency will not be installed.");
							if (!Boolean.parseBoolean(grinderPepperOSGiConnector.getPepperStarterConfiguration().getProperty("pepper.forceUpdate").toString())) {
								log.error("Artifact ".concat(artifact.getArtifactId()).concat(" will not be installed. Resolution of dependency ").concat(dependency.getArtifact().getArtifactId()).concat(" failed and \"force update\" is disabled in pepper.properties."));
								return false;
							}
						}
					}
				}
				artifact = null;
				Artifact installArtifact = null;
				for (int i = installArtifacts.size() - 1; i >= 0; i--) {
					try {
						installArtifact = installArtifacts.get(i);
						log.info("installing: " + installArtifact);
						bundle = grinderPepperOSGiConnector.installAndCopy(installArtifact.getFile().toURI());
						if (i != 0) {// the module itself must not be put on the blacklist
							putOnBlacklist(installArtifact);
						}
						else if (installedBundle != null) {
							grinderPepperOSGiConnector.remove(installedBundle.getSymbolicName());
							log.info("Successfully removed version ".concat(installedBundle.getVersion().toString()).concat(" of ").concat(artifactId));
						}
						if (bundle != null) {
							bundle.start();
						}
					}
					catch (IOException | BundleException e) {
//						if (log.isTraceEnabled()) {
//							log.trace("File could not be installed: " + installArtifact + " (" + installArtifact.getFile() + "); " + e.getClass().getSimpleName());
//						}
//						else {
//							log.warn("File could not be installed: " + installArtifact.getFile());
//						}
					}
				}
				/*
				 * btw: root is not supposed to be stored as forbidden dependency. This makes the removal of a module much less complicated. If a pepper module would be put onto the blacklist and the bundle would be removed, we always had
				 * to make sure, it its entry on the blacklist would be removed. Assuming the entry would remain on the blacklist, the module could be reinstalled, BUT(!) the dependencies would all be dropped and never being installed
				 * again, since the modules node dominates all other nodes in the dependency tree.
				 */
				write2Blacklist();
			}
		}
		catch (VersionRangeResolutionException | InvalidVersionSpecificationException | DependencyCollectionException e) {
			if (e instanceof DependencyCollectionException) {
				Throwable t = e.getCause();
				while (t.getCause() != null) {
					t = t.getCause();
				}
				if (t instanceof ArtifactNotFoundException) {
//					if (log.isDebugEnabled()) {
//						log.debug(t.getMessage(), e);
//					}
//					else {
//						log.warn("Update of " + artifactId + " failed, could not resolve dependencies "/* , e */);// TODO decide
//					}
				}
			}
			update = false;
		}
		return update;
	}

	private VersionRange isCompatiblePlugin(String pluginFrameworkVersion) {
		VersionScheme vScheme = new GenericVersionScheme();
		Version frameworkVersion;
		try {
			frameworkVersion = vScheme.parseVersion(grinderPepperOSGiConnector.getFrameworkVersion().replace(".SNAPSHOT", "-SNAPSHOT"));
			final Version depParentVersion = vScheme.parseVersion(pluginFrameworkVersion);
			int m = 1 + Integer.parseInt(depParentVersion.toString().split("\\.")[0]);
			final Version maxVersion = vScheme.parseVersion(m + ".0.0");
			String rangeString = "[".concat(pluginFrameworkVersion).concat(",").concat(maxVersion.toString()).concat(")");
			VersionRange range = vScheme.parseVersionRange(rangeString);
			if (!range.containsVersion(frameworkVersion)) {
				return range;
			}
		}
		catch (InvalidVersionSpecificationException e) {
			log.error("Could not compare required framework version to running framework. Trying to perform update anyway...");
		}
		return null;
	}

	/**
	 * This method returns all dependencies as list. Elementary dependencies and their daughters are skipped.
	 */
	private List<Dependency> getAllDependencies(DependencyNode startNode, boolean skipFramework) {
		List<Dependency> retVal = new ArrayList<>();
		retVal.add(startNode.getDependency());
		for (DependencyNode node : startNode.getChildren()) {
			boolean isFramework = ARTIFACT_ID_PEPPER_FRAMEWORK.equals(node.getArtifact().getArtifactId());
			boolean isSalt = node.getArtifact().getArtifactId().contains("salt-");
			if ((isFramework && !skipFramework) || (!isFramework && !isSalt)) {
				String blackListLine = getBlackListString(node.getArtifact());
				if (blackListLine != null && blackListLine.split(DELIMITER)[4].equals(STATUS.FINAL.toString())) {// dependency already installed AND singleton
					// do nothing at the Moment (TODO-> maybe implement a version range check, that enables an exchange of singletons)
				}
				else {// dependency not installed yet or not singleton
					if ("provided".equalsIgnoreCase(startNode.getDependency().getScope())) {
						putOnBlacklist(node.getArtifact());
						return Collections.<Dependency> emptyList();
					}
					else {
						retVal.addAll(getAllDependencies(node, skipFramework));
					}
				}
			}
			else if (skipFramework && isFramework) {// we need this for checking compatibility
				retVal.add(node.getDependency());
			}
		}
		return retVal;
	}

	private void putOnBlacklist(Artifact artifact) {
		if (getBlackListString(artifact) == null) {// for safety reasons (future use of this method, etc) we do the check
			Bundle bundle = grinderPepperOSGiConnector.getBundle(artifact.getGroupId(), artifact.getArtifactId(), null);
			STATUS status = bundle == null || !grinderPepperOSGiConnector.isSingleton(bundle) ? STATUS.OVERRIDABLE : STATUS.FINAL;
			forbiddenFruits.add(artifact.toString().concat(DELIMITER).concat(status.toString()).concat(DELIMITER).concat(bundle == null ? "" : bundle.getSymbolicName()));
			log.info("Put dependency on blacklist: ".concat(artifact.toString()));
		}
	}

	private String getBlackListString(Artifact artifact) {
		String as = artifact.toString().substring(0, artifact.toString().lastIndexOf(DELIMITER.charAt(0)));
		for (String artifactString : forbiddenFruits) {
			if (artifactString.startsWith(as)) {
				return artifactString;
			}
		}
		return null;
	}

	/**
	 * writes the old, freshly installed and forbidden dependencies to the blacklist file.
	 */
	private void write2Blacklist() {
		File blacklistFile = new File(BLACKLIST_PATH);
		if (!blacklistFile.exists()) {
			blacklistFile.getParentFile().mkdirs();
		}
		try {
			blacklistFile.createNewFile();
			PrintWriter fW = new PrintWriter(blacklistFile);
			BufferedWriter bW = new BufferedWriter(fW);
			for (String s : forbiddenFruits) {
				bW.write(s);
				bW.newLine();
			}
			bW.close();
			fW.close();
		}
		catch (IOException e) {
			log.warn("Could not write blacklist file.");
		}

	}

	/**
	 * @return the Blacklist of already installed or forbidden dependencies
	 */
	public String getBlacklist() {
		String lineSeparator = System.getProperty("line.separator");
		String indent = "\t";
		StringBuilder retVal = (new StringBuilder()).append(lineSeparator);
		retVal.append(indent).append("installed dependencies:").append(lineSeparator).append(lineSeparator);
		for (String s : forbiddenFruits) {
			retVal.append(indent).append(s).append(lineSeparator);
		}
		return retVal.toString();
	}

	/**
	 * This method cleans the dependencies, i.e. dependencies inherited from pepperParent as direct dependencies are removed.
	 * 
	 * @param dependencies
	 * @param session
	 * @param parentVersion
	 * @return
	 */
	private List<Dependency> cleanDependencies(List<Dependency> dependencies, RepositorySystemSession session, String parentVersion) {
		try {
			final List<Dependency> parentDeps;
			List<Dependency> checkList = parentDependencies.get(parentVersion.replace("-SNAPSHOT", ""));
			if (checkList == null) {
				CollectRequest collectRequest = new CollectRequest();
				collectRequest.setRoot(new Dependency(new DefaultArtifact("org.corpus-tools", ARTIFACT_ID_PEPPER_PARENT, "pom", parentVersion), ""));
				collectRequest.addRepository(repos.get(CENTRAL_REPO));
//				collectRequest.addRepository(repos.get(KORPLING_MAVEN_REPO));
				collectRequest.addRepository(repos.get(SONATYPE_SNAPSHOTS_REPO));
				CollectResult collectResult;
				collectResult = mvnSystem.collectDependencies(session, collectRequest);
				parentDeps = getAllDependencies(collectResult.getRoot(), false);
				parentDependencies.put(parentVersion.replace("-SNAPSHOT", ""), parentDeps);
			}
			else {
				parentDeps = checkList;
			}
			Iterator<Dependency> itDeps = parentDeps.iterator();
			Dependency next = itDeps.next();
			while (!ARTIFACT_ID_PEPPER_FRAMEWORK.equals(next.getArtifact().getArtifactId()) && itDeps.hasNext()) {
				next = itDeps.next();
			}
			itDeps = null;
			int j = 0;
			List<Dependency> newDeps = new ArrayList<>();
			STATUS status = null;
			for (int i = 0; i < dependencies.size(); i++) {
				j = 0;
				next = dependencies.get(i);
				status = getDependencyStatus(next.toString());
				while (j < parentDeps.size() && !(next.getArtifact().getArtifactId().equals(parentDeps.get(j).getArtifact().getArtifactId()) || STATUS.OVERRIDABLE.equals(status))) {
					j++;
				}
				if (j == parentDeps.size() || STATUS.OVERRIDABLE.equals(status)) {
					newDeps.add(next);
				}
				else {
					forbiddenFruits.add(next.getArtifact().toString() + DELIMITER + STATUS.FINAL);
					log.info("The following dependency was put on blacklist, because it equals a parent dependency: " + next.getArtifact().toString());
				}
			}
			return newDeps;
		}
		catch (DependencyCollectionException e) {
			log.warn("Could not collect dependencies for parent. No dependencies will be installed.");
		}
		ArrayList<Dependency> retVal = new ArrayList<>();
		return retVal;
	}

	private STATUS getDependencyStatus(String dependencyString) {
		dependencyString = dependencyString.substring(0, dependencyString.lastIndexOf(':'));
		for (String fruit : forbiddenFruits) {
			if (fruit.startsWith(dependencyString)) {
				if (fruit.split(DELIMITER)[4].equals(STATUS.FINAL.toString())) {
					return STATUS.FINAL;
				}
				return STATUS.OVERRIDABLE;
			}
		}
		return null;
	}

	/**
	 * This method builds a RemoteRepository for diverse maven/aether purposes.
	 * 
	 * @param id
	 * @param url
	 * @return
	 */
	private RemoteRepository getRepo(String id, String url) {
		RemoteRepository repo = repos.get(url);
		if (repo == null) {
			repoBuilder.setId(id);
			repoBuilder.setUrl(url);
			repo = repoBuilder.build();
			repos.put(url, repo);
		}
		return repo;
	}

	/**
	 * This method starts invokes the computation of the dependency tree. If no version is provided, the highest version in the specified maven repository is used. If no repository is provided, maven central and the korpling maven
	 * repository are used for trial.
	 */
	public String printDependencies(String groupId, String artifactId, String version, String repositoryUrl) {
		/* repositories */
		RemoteRepository repo = null;
		if (repositoryUrl == null) {
			repo = getRepo("korpling", KORPLING_MAVEN_REPO);
		}
		else {
			repo = getRepo("repository", repositoryUrl);
		}
		/* version range resolution and dependency collection */
		DefaultRepositorySystemSession session = getNewSession();
		Artifact artifact = new DefaultArtifact(groupId, artifactId, "pom", version == null ? "[0,)" : version);
		if (version == null) {
			VersionRangeRequest request = new VersionRangeRequest();
			request.setArtifact(artifact);
			if (repositoryUrl == null) {
				request.addRepository(repos.get(CENTRAL_REPO));
				request.addRepository(repos.get(SONATYPE_SNAPSHOTS_REPO));
			}
			request.addRepository(repo);
			try {
				VersionRangeResult rangeResult = mvnSystem.resolveVersionRange(session, request);
				version = rangeResult.getHighestVersion().toString();
				artifact.setVersion(version);
			}
			catch (VersionRangeResolutionException e) {
//				log.error("Could not determine newest version.");
				return null;
			}
		}
		CollectRequest collectRequest = new CollectRequest();
		collectRequest.setRoot(new Dependency(artifact, ""));
		if (repositoryUrl == null) {
			collectRequest.addRepository(repos.get(CENTRAL_REPO));
		}
		collectRequest.addRepository(repo);
		CollectResult collectResult;
		try {
			collectResult = mvnSystem.collectDependencies(session, collectRequest);
			return getDependencyPrint(collectResult.getRoot(), 0);
		}
		catch (DependencyCollectionException e) {
//			log.error("Could not print dependencies for ".concat(artifactId).concat("."));
		}
		return null;
	}

	/** this method recursively computes */
	private String getDependencyPrint(DependencyNode startNode, int depth) {
		String d = "";
		for (int i = 0; i < depth; i++) {
			d += " ";
		}
		d += depth == 0 ? "" : "+- ";
		d += startNode.getArtifact().toString().concat(" (").concat(startNode.getDependency().getScope()).concat(")");
		for (DependencyNode node : startNode.getChildren()) {
			d += System.lineSeparator().concat(getDependencyPrint(node, depth + 1));
		}
		return d;
	}

	/**
	 * This method tries to determine maven project coordinates from a bundle id to invoke {@link #printDependencies(String, String, String, String)}.
	 */
	protected String printDependencies(Bundle bundle) { // NO UCD (unused code)
		String[] coords = null;
		for (String s : forbiddenFruits) {
			coords = s.split(DELIMITER);
			if (bundle != null && coords.length == 6 && coords[5].equals(bundle.getSymbolicName())) {
				return printDependencies(coords[0], coords[1], coords[3].replace(".SNAPSHOT", "-SNAPSHOT"), null);
			}
		}
		// maven coordinates could not be determined, assume, we talk about a pepper plugin:
		return printDependencies(bundle.getSymbolicName().substring(0, bundle.getSymbolicName().lastIndexOf('.')), bundle.getSymbolicName().substring(bundle.getSymbolicName().lastIndexOf('.') + 1), bundle.getVersion().toString(), KORPLING_MAVEN_REPO);
	}

	private class MavenRepositoryListener extends AbstractRepositoryListener {
		private final boolean TRACE = true;

		private MavenRepositoryListener() {
//			TRACE = log.isTraceEnabled();
		}

		@Override
		public void artifactDeployed(RepositoryEvent event) {
			if (TRACE) {
//				log.trace("Deployed " + event.getArtifact() + " to " + event.getRepository());
			}
		}

		@Override
		public void artifactDeploying(RepositoryEvent event) {
			if (TRACE) {
//				log.trace("Deploying " + event.getArtifact() + " to " + event.getRepository());
			}
		}

		@Override
		public void artifactDescriptorInvalid(RepositoryEvent event) {
			if (TRACE) {
//				log.trace("Invalid artifact descriptor for " + event.getArtifact() + ": " + event.getException().getMessage());
			}
		}

		@Override
		public void artifactDescriptorMissing(RepositoryEvent event) {
			if (TRACE) {
//				log.trace("Missing artifact descriptor for " + event.getArtifact());
			}
		}

		@Override
		public void artifactInstalled(RepositoryEvent event) {
			if (TRACE) {
//				log.trace("Installed " + event.getArtifact() + " to " + event.getFile());
			}
		}

		@Override
		public void artifactInstalling(RepositoryEvent event) {
			if (TRACE) {
//				log.trace("Installing " + event.getArtifact() + " to " + event.getFile());
			}
		}

		@Override
		public void artifactResolved(RepositoryEvent event) {
			if (TRACE) {
//				log.trace("Resolved artifact " + event.getArtifact() + " from " + event.getRepository());
			}
		}

		@Override
		public void artifactDownloading(RepositoryEvent event) {
			if (TRACE) {
//				log.trace("Downloading artifact " + event.getArtifact() + " from " + event.getRepository());
			}
		}

		@Override
		public void artifactDownloaded(RepositoryEvent event) {
			if (TRACE) {
//				log.trace("Downloaded artifact " + event.getArtifact() + " from " + event.getRepository());
			}
		}

		@Override
		public void artifactResolving(RepositoryEvent event) {
			if (TRACE) {
//				log.trace("Resolving artifact " + event.getArtifact());
			}
		}

		@Override
		public void metadataDeployed(RepositoryEvent event) {
			if (TRACE) {
//				log.trace("Deployed " + event.getMetadata() + " to " + event.getRepository());
			}
		}

		@Override
		public void metadataDeploying(RepositoryEvent event) {
			if (TRACE) {
//				log.trace("Deploying " + event.getMetadata() + " to " + event.getRepository());
			}
		}

		@Override
		public void metadataInstalled(RepositoryEvent event) {
			if (TRACE) {
//				log.trace("Installed " + event.getMetadata() + " to " + event.getFile());
			}
		}

		@Override
		public void metadataInstalling(RepositoryEvent event) {
			if (TRACE) {
//				log.trace("Installing " + event.getMetadata() + " to " + event.getFile());
			}
		}

		@Override
		public void metadataInvalid(RepositoryEvent event) {
			if (TRACE) {
//				log.trace("Invalid metadata {}", event.getMetadata());
			}
		}

		@Override
		public void metadataResolved(RepositoryEvent event) {
			if (TRACE) {
//				log.trace("Resolved metadata " + event.getMetadata() + " from " + event.getRepository());
			}
		}

		@Override
		public void metadataResolving(RepositoryEvent event) {
			if (TRACE) {
//				log.trace("Resolving metadata " + event.getMetadata() + " from " + event.getRepository());
			}
		}
	}

	private class MavenTransferListener extends AbstractTransferListener {
		private final boolean TRACE = true;

		private MavenTransferListener() {
//			TRACE = log.isTraceEnabled();
		}

		@Override
		public void transferInitiated(TransferEvent event) {
			String message = event.getRequestType() == TransferEvent.RequestType.PUT ? "Uploading" : "Downloading";

			if (TRACE) {
//				log.trace(message + ": " + event.getResource().getRepositoryUrl() + event.getResource().getResourceName());
			}
		}

		@Override
		public void transferProgressed(TransferEvent event) {
		}

		@Override
		public void transferSucceeded(TransferEvent event) {
			if (TRACE) {
				transferCompleted(event);

				TransferResource resource = event.getResource();
				long contentLength = event.getTransferredBytes();
				if (contentLength >= 0) {
					String type = (event.getRequestType() == TransferEvent.RequestType.PUT ? "Uploaded" : "Downloaded");
					String len = contentLength >= 1024 ? toKB(contentLength) + " KB" : contentLength + " B";

					String throughput = "";
					long duration = System.currentTimeMillis() - resource.getTransferStartTime();
					if (duration > 0) {
						long bytes = contentLength - resource.getResumeOffset();
						DecimalFormat format = new DecimalFormat("0.0", new DecimalFormatSymbols(Locale.ENGLISH));
						double kbPerSec = (bytes / 1024.0) / (duration / 1000.0);
						throughput = " at " + format.format(kbPerSec) + " KB/sec";
					}

//					log.trace(type + ": " + resource.getRepositoryUrl() + resource.getResourceName() + " (" + len + throughput + ")");
				}
			}
		}

		@Override
		public void transferFailed(TransferEvent event) {
			if (TRACE) {
				transferCompleted(event);
				if (!(event.getException() instanceof MetadataNotFoundException)) {
//					log.trace("An error occured transfering " + event.getResource(), event.getException());
				}
			}
		}

		private void transferCompleted(TransferEvent event) {
//			log.trace("Transfer completed.");
		}

		@Override
		public void transferCorrupted(TransferEvent event) {
			if (TRACE) {
//				log.trace("Transfer corrupted.", event.getException());
			}
		}

		protected long toKB(long bytes) {
			return (bytes + 1023) / 1024;
		}

	}

	private class POMReader extends DefaultHandler2 {

		private boolean read = true;

		static final String TAG_REPOSITORIES = "repositories";
		static final String TAG_REPOSITORY = "repository";
		static final String TAG_URL = "url";
		static final String TAG_ID = "id";
		static final String ROOT_TAG = "project";

		private String pppparent = null;
		private String ppparent = null;
		private String pparent = null;
		private String parent = null;
		private String currTag = null;
		private StringBuilder chars = null;

		private String url = null;
		private String id = null;

		private List<RemoteRepository> resultsList;

		private POMReader(List<RemoteRepository> targetList) {
			chars = new StringBuilder();
			resultsList = targetList;
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) {
			if (read) {
				localName = qName.substring(qName.lastIndexOf(":") + 1);
				pppparent = ppparent;
				ppparent = pparent;
				pparent = parent;
				parent = currTag;
				currTag = localName;
				chars.delete(0, chars.length());
			}
		}

		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			if (read && ROOT_TAG.equals(ppparent) && TAG_REPOSITORIES.equals(pparent) && TAG_REPOSITORY.equals(parent) && (TAG_ID.equals(currTag) || TAG_URL.equals(currTag))) {
				for (int i = start; i < start + length; i++) {
					chars.append(ch[i]);
				}
			}
		}

		@Override
		public void endElement(java.lang.String uri, String localName, String qName) throws SAXException {
			if (read) {
				localName = qName.substring(qName.lastIndexOf(":") + 1);
				boolean constraint = ROOT_TAG.equals(ppparent) && TAG_REPOSITORIES.equals(pparent) && TAG_REPOSITORY.equals(parent);
				if (constraint && TAG_URL.equals(localName)) {
					url = chars.toString();
				}
				else if (constraint && TAG_ID.equals(localName)) {
					id = chars.toString();
				}
				else if (ROOT_TAG.equals(pparent) && TAG_REPOSITORIES.equals(parent) && TAG_REPOSITORY.equals(localName)) {
					resultsList.add(getRepo(id, url));
				}
				else if (ROOT_TAG.equals(parent) && TAG_REPOSITORIES.equals(localName)) {
					read = false;
				}
				chars.delete(0, chars.length());
				currTag = parent;
				parent = pparent;
				pparent = ppparent;
				ppparent = pppparent;
				pppparent = null;
			}
		}
	}
}
