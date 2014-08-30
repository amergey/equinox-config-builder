package com.sarod.equinox.config.builder;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;

public class ConfigBuilderTest {

	private File targetUnzipped;

	@Before
	public void setup() throws IOException {
		
		// unzip test resources
		InputStream inputStream = getClass().getResourceAsStream("eclipsedir-for-tests.zip");

		ZipInputStream zipStream = new ZipInputStream(inputStream);
		targetUnzipped = new File("eclipsedir-unzipped");
		ZipEntry zipEntry = null;
		while ((zipEntry = zipStream.getNextEntry()) != null) {
			if (!zipEntry.isDirectory()) {
				final File file = new File(targetUnzipped, zipEntry.getName());
				Files.createParentDirs(file);
				Files.write(ByteStreams.toByteArray(zipStream), file);
			}
		}
	}
	
	@Test
	public void testConfigBuilder() throws IOException {
		
		Map<String, Integer> bundleStartLevels = new HashMap<String, Integer>();
		bundleStartLevels.put("bundle1", 1);
		ConfigBuilder builder = new ConfigBuilder(targetUnzipped, 4, bundleStartLevels);
		builder.buildConfigFile();
		
		File configFile = new File(targetUnzipped, "configuration/config.ini");
		assertTrue(configFile.exists());
		
		String content = Files.toString(configFile, Charsets.UTF_8);
		assertEquals("#Product Runtime Configuration File\n" + 
				"osgi.bundles.defaultStartLevel=4\n" + 
				"osgi.bundles=com.sarod.test.bundle@start,\\\n" + 
				"com.sarod.test.fragment,\\\n" + 
				"org.eclipse.equinox.servletbridge.extensionbundle", content);
	}
}
