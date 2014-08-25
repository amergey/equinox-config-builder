package com.sarod.equinox.config.builder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class BundleInfoLoader {

	private static final String FRAGMENT_HOST = "Fragment-Host";
	private static final String BUNDLE_SYMBOLIC_NAME = "Bundle-SymbolicName";

	private final Logger logger = Logger.getLogger(getClass().getName());

	public BundleInfoLoader() {

	}

	/**
	 * Load BundleInfo from a jar file
	 * @param jarStream
	 * @param pluginFileName
	 * @return
	 * @throws ConfigBuildingException
	 */
	public BundleInfo loadBundleInfo(InputStream jarStream, String pluginFileName) throws ConfigBuildingException {
		try {
			Manifest manifest = loadManifest(jarStream);
			if (manifest == null) {
				logger.log(Level.FINE, "No manifest in jar");
				return null;
			}
			String symbolicName = symbolicName(manifest);
			if (symbolicName == null) {
				logger.log(Level.FINE, "Not a bundle: No symbolic name in manifest");
				return null;
			}

			boolean fragment = manifest.getMainAttributes().getValue(FRAGMENT_HOST) != null;
			BundleInfo bundleInfo = new BundleInfo(pluginFileName, symbolicName, fragment);
			return bundleInfo;
		} catch (IOException e) {
			throw new ConfigBuildingException("Error loading manifest information for " + pluginFileName, e);
		}

	}

	public BundleInfo loadBundleInfo(File jarFile) throws ConfigBuildingException {
		String pluginFileName = jarFile.getName();

		FileInputStream fis = null;
		try {
			fis = new FileInputStream(jarFile);
			return loadBundleInfo(fis, pluginFileName);
		} catch (FileNotFoundException e) {
			throw new ConfigBuildingException("Error loading manifest information for " + jarFile, e);			
		} finally {
			IOUtils.closeQuietly(fis);
		}

	}

	private Manifest loadManifest(InputStream jarStream) throws IOException {
		ZipInputStream zis = null;
		try {
			zis = new ZipInputStream(jarStream);

			ZipEntry entry;
			while ((entry = zis.getNextEntry()) != null) {
				if (entry.getName().equals("META-INF/MANIFEST.MF")) {
					return new Manifest(zis);
				}
			}
			return null;
		} finally {
			IOUtils.closeQuietly(zis);
		}
	}

	private String symbolicName(Manifest manifest) {
		String symbolicName = manifest.getMainAttributes().getValue(BUNDLE_SYMBOLIC_NAME);
		if (symbolicName == null) {
			return null;
		}

		// Remove the configuration part (;singleton:=true...)
		int semColonIndex = symbolicName.indexOf(";");
		if (semColonIndex != -1) {
			symbolicName = symbolicName.substring(0, semColonIndex);
		}
		symbolicName = symbolicName.trim();
		return symbolicName;
	}

}
