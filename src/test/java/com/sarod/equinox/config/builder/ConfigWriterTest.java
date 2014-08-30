package com.sarod.equinox.config.builder;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class ConfigWriterTest {

	private BundleInfo bundle2;
	private BundleInfo bundle1;
	private BundleInfo bundle3;
	private BundleInfo fragment1;
	private ConfigWriter writer;

	@Before
	public void setup() {
		writer = new ConfigWriter();
		bundle1 = BundleInfo.bundle("bundle1.jar", "bundle1", "1.0.0");
		bundle2 = BundleInfo.bundle("bundle2.jar", "bundle2", "1.0.0");
		bundle3 = BundleInfo.bundle("bundle3.jar", "bundle3", "1.0.0");

		fragment1 = BundleInfo.fragment("fragment1.jar", "fragment1", "1.0.0", "bundle1");
	}

	@Test
	public void testBasic() {
		String result = writer.buildConfigContent(new ConfigDescriptor(4, Arrays.asList(bundle1, bundle2, fragment1)));
		String expectedResult = "#Product Runtime Configuration File\n" + 
				"osgi.bundles.defaultStartLevel=4\n" + 
				"osgi.bundles=bundle1@start,\\\n" + 
				"bundle2@start,\\\n" + 
				"fragment1,\\\n" + 
				"org.eclipse.equinox.servletbridge.extensionbundle";

		assertEquals(expectedResult, result);
	}

	@Test
	public void testBundleStartLevels() {
		
		List<BundleInfo> bundles = Arrays.asList(bundle1, bundle2, bundle3, fragment1);
		Map<String, Integer> bundleStartLevels = new HashMap<String, Integer>();
		bundleStartLevels.put(bundle1.getBundleName(), 1);
		bundleStartLevels.put(bundle2.getBundleName(), 5);

		// Fragment start level should be ignored
		bundleStartLevels.put(fragment1.getBundleName(), 5);
		String result = writer.buildConfigContent(new ConfigDescriptor(4, 
				bundles, bundleStartLevels));
		String expectedResult = "#Product Runtime Configuration File\n" + 
				"osgi.bundles.defaultStartLevel=4\n" + 
				"osgi.bundles=bundle1@1:start,\\\n" + 
				"bundle2@5:start,\\\n" + 
				"bundle3@start,\\\n" + 
				"fragment1,\\\n" + 
				"org.eclipse.equinox.servletbridge.extensionbundle";
		assertEquals(expectedResult, result);
	}
}
