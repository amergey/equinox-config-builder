package com.sarod.equinox.config.builder;

import static org.junit.Assert.*;

import org.junit.Test;

public class BuilderInfoLoaderTest {

	@Test
	public void testLoadBundleBundleInfo() {

		BundleInfoLoader infoLoader = new BundleInfoLoader();

		String fileName = "bundle.jar";
		BundleInfo info = infoLoader.loadBundleInfo(getClass().getResourceAsStream(fileName),
				fileName);
		assertEquals("com.sarod.test.bundle", info.getBundleName());
		assertEquals(fileName, info.getFileName());
		assertEquals(false, info.isFragment());
	}
	
	@Test
	public void testLoadFragmentBundleInfo() {

		BundleInfoLoader infoLoader = new BundleInfoLoader();

		String fileName = "fragment.jar";
		BundleInfo info = infoLoader.loadBundleInfo(getClass().getResourceAsStream(fileName),
				fileName);
		assertEquals("com.sarod.test.fragment", info.getBundleName());
		assertEquals(fileName, info.getFileName());
		assertEquals(true, info.isFragment());
	}
	
	@Test
	public void testLoadNonOsgiBundleInfo() {

		BundleInfoLoader infoLoader = new BundleInfoLoader();

		String fileName = "non-osgi.jar";
		BundleInfo info = infoLoader.loadBundleInfo(getClass().getResourceAsStream(fileName),
				fileName);
		assertNull(info);
	}
}
