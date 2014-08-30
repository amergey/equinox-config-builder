package com.sarod.equinox.config.builder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.sarod.equinox.config.builder.utils.IOUtils;

public class BundleInfoLoader {

	private static final String MF_ATTRIBUTE_BUNDLE_VERSION = "Bundle-Version";
	private static final String MF_ATTRIBUTE_FRAGMENT_HOST = "Fragment-Host";
	private static final String MF_ATTRIBUTE_BUNDLE_SYMBOLIC_NAME = "Bundle-SymbolicName";

	private final Logger logger = Logger.getLogger(getClass().getName());

	public BundleInfoLoader() {

	}

	/**
	 * Load BundleInfo from a jar inputStream.
	 * 
	 * 
	 * @param jarInputStream
	 * @param pluginFileName
	 * @return the {@link BundleInfo} or null if jar does not contain a MANIFEST
	 *         file or if jar is not an osgi bundle or fragment (MANIFEST does
	 *         not contain a {@value #MF_ATTRIBUTE_BUNDLE_SYMBOLIC_NAME}
	 *         attribute)
	 * @throws ConfigBuildingException
	 */
	public BundleInfo loadBundleInfo(InputStream jarInputStream, String pluginFileName) throws ConfigBuildingException {
		if (jarInputStream == null) {
			throw new NullPointerException("jarInputStreamStream must be not null");
		}
		if (pluginFileName == null) {
			throw new NullPointerException("pluginFileName must be not null");
		}
		try {
			Manifest manifest = loadManifest(jarInputStream);
			if (manifest == null) {
				logger.log(Level.FINE, "No manifest in jar");
				return null;
			}
			String symbolicName = symbolicName(manifest);
			if (symbolicName == null) {
				logger.log(Level.FINE, "Not a bundle: No symbolic name in manifest");
				return null;
			}

			String bundleVersion = manifest.getMainAttributes().getValue(MF_ATTRIBUTE_BUNDLE_VERSION);

			String fragmentHost = manifest.getMainAttributes().getValue(MF_ATTRIBUTE_FRAGMENT_HOST);
			if (fragmentHost == null) {
				return BundleInfo.bundle(pluginFileName, symbolicName, bundleVersion);
			} else {
				String[] fragmentHostParts = fragmentHost.split(";");
				String fragmentHostName = fragmentHostParts[0];
				return BundleInfo.fragment(pluginFileName, symbolicName, bundleVersion, fragmentHostName);
			}
		} catch (IOException e) {
			throw new ConfigBuildingException("Error loading manifest information for " + pluginFileName, e);
		}

	}

	/**
	 * Loads a list of BundleInfo for each bundles/fragments found in a directory
	 * @param directory
	 * @return
	 */
	public Collection<BundleInfo> loadBundleInfos(File directory) {
		List<BundleInfo> bundleInfos = new ArrayList<BundleInfo>();
		for (File pluginFile : directory.listFiles()) {

			String pluginFileName = pluginFile.getName();
			if (!pluginFileName.endsWith(".jar")) {
				// Skip non jars
				logger.log(Level.FINE, "Skipping non jar: " + pluginFileName);
			} else {
				BundleInfo bundleInfo = loadBundleInfo(pluginFile);
				if (bundleInfo == null) {
					logger.log(Level.FINE, "Skipping non bundle jar: " + pluginFile);
				} else {
					logger.log(Level.FINE, "Adding : " + bundleInfo);
					bundleInfos.add(bundleInfo);
				}
			}

		}
		// Sort bundle infos in alphabetical order to make config.ini more
		// readable by humans
		Collections.sort(bundleInfos);
		return bundleInfos;
	}


	/**
	 * Load BundleInfo from a jar file.
	 * 
	 * 
	 * @param jarInputStream
	 * @param pluginFileName
	 * @return the {@link BundleInfo} or null if jar does not contain a MANIFEST
	 *         file or if jar is not an osgi bundle or fragment (MANIFEST does
	 *         not contain a {@value #MF_ATTRIBUTE_BUNDLE_SYMBOLIC_NAME}
	 *         attribute)
	 * @throws ConfigBuildingException
	 */
	public BundleInfo loadBundleInfo(File jarFile) throws ConfigBuildingException {
		if (jarFile == null) {
			throw new NullPointerException("jarFile must be not null");
		}
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
		String symbolicName = manifest.getMainAttributes().getValue(MF_ATTRIBUTE_BUNDLE_SYMBOLIC_NAME);
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
