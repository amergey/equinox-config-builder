package com.sarod.equinox.config.builder.ant;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.apache.tools.ant.taskdefs.Ant;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.sarod.equinox.config.builder.FileUtils;

public class EquinoxConfigBuilderTaskTest {

	private Project project;
	private BuildEvent buildFinishedEvent;
	private File targetUnzipped;
	private File eclipseDir;

	
	@Before
	public void setUp() throws IOException {
		
		
		targetUnzipped = Files.createTempDir();
		
		FileUtils.unzip(getClass().getResourceAsStream("ant-test.zip"), targetUnzipped);

		eclipseDir = new File(targetUnzipped, "eclipse");
		// FIXME we should use BuildFileRule when it's available in ant release


		
		project = new Project();
		project.init();
		File antFile = new File(targetUnzipped, "build.xml");
//		project.setProperty("ant.processid", ProcessUtil.getProcessId("<Process>"));
		project.setProperty("ant.threadname", Thread.currentThread().getName());
		project.setProperty("eclipse.dir", eclipseDir.getAbsolutePath());
		project.setUserProperty("ant.file", antFile.getAbsolutePath());
		
		ProjectHelper.configureProject(project, antFile);

		
		
		
	}

	@Test
	public void testAnt() throws IOException {
		project.executeTarget("generate-config");
		File configFile = new File(eclipseDir, "configuration/config.ini");
		System.out.println(targetUnzipped);
		assertTrue(configFile.exists());

		String content = Files.toString(configFile, Charsets.UTF_8);
		assertEquals("#Product Runtime Configuration File\n" + 
				"osgi.bundles.defaultStartLevel=4\n" + 
				"osgi.bundles=com.sarod.test.bundle@start,\\\n" + 
				"com.sarod.test.bundle2@2:start,\\\n" + 
				"com.sarod.test.fragment,\\\n" + 
				"org.eclipse.rap.servletbridge.extensionbundle", content);
	}
}
