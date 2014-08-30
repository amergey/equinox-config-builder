package com.sarod.equinox.config.builder;

import static org.junit.Assert.*;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class BuilderInfoLoaderTest {

	private BundleInfoLoader infoLoader;

	@Before
	public void setup() {
		infoLoader = new BundleInfoLoader();

	}

	@Test
	public void testLoadBundleBundleInfo() {

		String fileName = "bundle.jar";
		BundleInfo info = infoLoader.loadBundleInfo(getClass().getResourceAsStream(fileName), fileName);
		assertEquals("com.sarod.test.bundle", info.getBundleName());
		assertEquals(fileName, info.getFileName());
		assertEquals("1.0.0", info.getBundleVersion());
		assertEquals(false, info.isFragment());
		info.toString();
	}

	@Test
	public void testLoadFragmentBundleInfo() {

		String fileName = "fragment.jar";
		BundleInfo info = infoLoader.loadBundleInfo(getClass().getResourceAsStream(fileName), fileName);
		assertEquals("com.sarod.test.fragment", info.getBundleName());
		assertEquals(fileName, info.getFileName());
		assertEquals(true, info.isFragment());

		assertEquals("com.sarod.test.bundle", info.getHostName());
		info.toString();
	}

	@Test
	public void testLoadNonOsgiBundleInfo() {

		String fileName = "non-osgi.jar";
		BundleInfo info = infoLoader.loadBundleInfo(getClass().getResourceAsStream(fileName), fileName);
		assertNull(info);
	}

	@Test
	public void testLoadNoManifestBundleInfo() {

		String fileName = "no-manifest.zip";
		BundleInfo info = infoLoader.loadBundleInfo(getClass().getResourceAsStream(fileName), fileName);
		assertNull(info);
	}

	@Test
	public void testLoadNotAJarBundleInfo() {

		String fileName = "not-a-jar.txt";
		BundleInfo info = infoLoader.loadBundleInfo(getClass().getResourceAsStream(fileName), fileName);
		assertNull(info);
	}

	@Test
	public void testLoadDiretory() {
		URL url = getClass().getResource("not-a-jar.txt");
		if (url.getProtocol().equals("file")) {
			File directory = new File(url.getFile()).getParentFile();
			Collection<BundleInfo> bundleInfos = infoLoader.loadBundleInfos(directory);
			assertEquals(2, bundleInfos.size());
			
			List<BundleInfo> sorted = new ArrayList<BundleInfo>(bundleInfos);
			
			BundleInfo bundle = sorted.get(0);
			BundleInfo fragment = sorted.get(1);
			assertEquals("bundle.jar", bundle.getFileName());
			assertEquals("com.sarod.test.bundle", bundle.getBundleName());

			assertEquals("fragment.jar", fragment.getFileName());
			assertEquals("com.sarod.test.fragment", fragment.getBundleName());
		} else  {
			// 
			System.out.println("Skipping directory test");
		}
	}
}
