package de.shop.util;

import static de.shop.util.TestConstants.TEST_CLASSES;
import static de.shop.util.TestConstants.WEB_PROJEKT;

import java.io.File;
import java.nio.file.Paths;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.importer.ExplodedImporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.PomEquippedResolveStage;

public enum ArchiveBuilder {
	INSTANCE;
	
	static final String TEST_WAR = WEB_PROJEKT + ".war";
	
	private static final String CLASSES_DIR = "target/classes";
	private static final String WEBINF_DIR = "src/main/webapp/WEB-INF";
	private static final String JBOSS_DEPLOYMENT_STRUCTURE_XML = "src/test/resources/jboss-deployment-structure.xml";
	
	private final WebArchive archive = ShrinkWrap.create(WebArchive.class, TEST_WAR);
	private final WebArchive archiveMitTestklassen = ShrinkWrap.create(WebArchive.class, TEST_WAR);

	/**
	 */
	private ArchiveBuilder() {
		addWebInf();		
		addJars();
		addKlassen();

		archiveMitTestklassen.merge(archive);
		addTestKlassen();
		
//		final Path arquillianPath = Paths.get("target/arquillian");
//		try {
//			Files.createDirectories(arquillianPath);
//		}
//		catch (IOException e) {
//			throw new RuntimeException(e);
//		}
//		final File warFile = Paths.get(arquillianPath.toString(), TEST_WAR).toFile();
//		archiveResourceTests.as(ZipExporter.class).exportTo(warFile, true); 
	}

	private void addWebInf() {
		final File webInfDir = new File(WEBINF_DIR);
		final File[] files = webInfDir.listFiles();
		for (File f : files) {
			if (f.isFile()) {
				archive.addAsWebInfResource(f);
			}
		}

		// Gibt es jboss-deployment-structure.xml fuer die Erweiterung des Classpath?
		final File jbossDeploymentStructureXml = Paths.get(JBOSS_DEPLOYMENT_STRUCTURE_XML).toFile();
		if (jbossDeploymentStructureXml.exists()) {
			archive.addAsWebInfResource(jbossDeploymentStructureXml);
		}
		// TODO MANIFEST.MF fuer Web-Archiv kann nicht mit ShrinkWrap gesetzt werden
//		else {
//			archive.addAsManifestResource(new StringAsset("Manifest-Version: 1.0\n"
//			                              + "Dependencies: org.jboss.as.controller-client,org.jboss.dmr"),
//					                      "MANIFEST.MF");
//			
//			archive.setManifest(new StringAsset("Manifest-Version: 1.0\n"
//					            + "Dependencies: org.jboss.as.controller-client,org.jboss.dmr"));
//		}
	}
	
	private void addJars() {
		// http://exitcondition.alrubinger.com/2012/09/13/shrinkwrap-resolver-new-api
		final PomEquippedResolveStage pomResolver = Maven.resolver().offline().loadPomFromFile("pom.xml");
		archive.addAsLibraries(pomResolver.resolve("org.richfaces.ui:richfaces-components-ui")
										  .withTransitivity().asFile())
		       .addAsLibraries(pomResolver.resolve("org.richfaces.core:richfaces-core-impl")
		    		   					  .withTransitivity().asFile());
	}
	
	private void addKlassen() {
		final JavaArchive tmp = ShrinkWrap.create(JavaArchive.class);
		tmp.as(ExplodedImporter.class).importDirectory(CLASSES_DIR);
		archive.merge(tmp, "WEB-INF/classes");
	}

	private void addTestKlassen() {
		for (Class<?> c : TEST_CLASSES) {
			archiveMitTestklassen.addClass(c);
		}
	}

	public static ArchiveBuilder getInstance() {
		return INSTANCE;
	}
	
	public Archive<? extends Archive<?>> getArchive() {
		return archive;
	}

	public Archive<? extends Archive<?>> getArchiveMitTestklassen() {
		return archiveMitTestklassen;
	}
}
