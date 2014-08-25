package com.sarod.equinox.config.builder;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class ConfigBuilder {

	private static final String CONFIG_INI_FILE = "config.ini";

	private static final String CONFIGURATION_DIR = "configuration";

	private static final class BundleInfo implements Comparable<BundleInfo> {

		private final String fileName;
		private final boolean fragment;
		private final String bundleName;

		public BundleInfo(String fileName, String bundleName, boolean fragment) {
			super();
			this.fileName = fileName;
			this.bundleName = bundleName;
			this.fragment = fragment;
		}

		public String getFileName() {
			return fileName;
		}

		public boolean isFragment() {
			return fragment;
		}

		public String getBundleName() {
			return bundleName;
		}

		@Override
		public String toString() {
			return "BundleInfo [fileName=" + getFileName() + ", bundleName=" + getBundleName() + ", fragment=" + isFragment() + "]";
		}

		public int compareTo(BundleInfo anotherBi) {
			return bundleName.compareTo(anotherBi.getBundleName());
		}

	}

	protected final static Logger LOGGER = Logger.getLogger(ConfigBuilder.class.getName());

	private static final String FRAGMENT_HOST = "Fragment-Host";
	private static final String BUNDLE_SYMBOLIC_NAME = "Bundle-SymbolicName";

	public static final int DEFAULT_DEFAULT_START_LEVEL = 4;
	private final File eclipseDirectory;
	private final int defaultStartLevel;
	private final Map<String, Integer> bundleStartLevels;

	public static Map<String, Integer> startLevelsMapFromProperties(Properties bundleStartLevels) {
		Map<String, Integer> bundleStartLevelsMap = new HashMap<String, Integer>();
		for (Map.Entry<Object, Object> entry : bundleStartLevels.entrySet()) {
			try {
				int startLevel = Integer.parseInt((String) entry.getValue());
				bundleStartLevelsMap.put((String) entry.getKey(), startLevel);
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("Properties contain invalid start level " + entry.getValue() + "for bundle: "
						+ entry.getKey());
			}
		}
		return bundleStartLevelsMap;
	}

	public ConfigBuilder(File eclipseDirectory) {
		this(eclipseDirectory, DEFAULT_DEFAULT_START_LEVEL);
	}

	public ConfigBuilder(File eclipseDirectory, int defaultStartLevel) {
		this(eclipseDirectory, defaultStartLevel, Collections.<String, Integer> emptyMap());
	}

	public ConfigBuilder(File eclipseDirectory, int defaultStartLevel, Map<String, Integer> bundleStartLevels) {
		if (eclipseDirectory == null) {
			throw new NullPointerException("eclipseDirectory should not be null");
		}
		if (!eclipseDirectory.exists() || !eclipseDirectory.isDirectory() || !eclipseDirectory.canRead()) {
			throw new IllegalArgumentException("eclipseDirectory " + eclipseDirectory + " is not a readable directory.");
		}
		this.eclipseDirectory = eclipseDirectory;

		if (!eclipseDirectory.exists() || !eclipseDirectory.isDirectory() || !eclipseDirectory.canRead()) {
			throw new IllegalArgumentException("eclipseDirectory " + eclipseDirectory + " is not a readable directory.");
		}
		if (defaultStartLevel <= 0) {
			throw new IllegalArgumentException("deafultStartLevel should be a stritcly positive integer.");
		}
		this.defaultStartLevel = defaultStartLevel;
		if (bundleStartLevels == null) {
			throw new NullPointerException("bundleStartLevels should not be null");
		}
		// Defensive copy
		this.bundleStartLevels = Collections.unmodifiableMap(new HashMap<String, Integer>(bundleStartLevels));
	}

	public File getEclipseDirectory() {
		return eclipseDirectory;
	}

	public Map<String, Integer> getBundleStartLevels() {
		return bundleStartLevels;
	}

	public int getDefaultStartLevel() {
		return defaultStartLevel;
	}

	public static int getDefaultDefaultStartLevel() {
		return DEFAULT_DEFAULT_START_LEVEL;
	}

	public void buildConfigFile() {
		File pluginsDirectory = new File(eclipseDirectory, "plugins");
		if (!pluginsDirectory.exists() || !pluginsDirectory.canRead()) {
			throw new ConfigBuildingException("Cannot read <eclipseDirectory>/plugins directory: " + pluginsDirectory);
		}

		LOGGER.log(Level.INFO, "Generating configuration/config.ini....");

		List<BundleInfo> bundleInfos = new ArrayList<BundleInfo>();
		for (File pluginFile : pluginsDirectory.listFiles()) {
			BundleInfo bundleInfo = bundleInfo(pluginFile);
			if (bundleInfo != null) {
				bundleInfos.add(bundleInfo);
			}
		}
		// Sort bundle infos in alphabetical order to make config.ini more
		// readable by humans
		Collections.sort(bundleInfos);
		String configContent = buildConfigContent(bundleInfos);

		File configDirectory = new File(eclipseDirectory, CONFIGURATION_DIR);
		configDirectory.mkdirs();
		File configFile = new File(configDirectory, CONFIG_INI_FILE);
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(configFile);
			out.write(configContent.getBytes("UTF-8"));
			out.flush();
		} catch (Exception e) {
			throw new ConfigBuildingException("Error writing <eclipseDirectory>/configuration/config.ini file: " + configFile, e);
		} finally {
			closeQuietly(out);
		}
		LOGGER.log(Level.INFO, "Generating configuration/config.ini: Done " + bundleInfos.size() + " plugins configured.");
	}

	private String buildConfigContent(List<BundleInfo> bundleInfos) {
		StringBuilder configBuilder = new StringBuilder();
		configBuilder.append("#Product Runtime Configuration File\n");
		configBuilder.append("osgi.bundles.defaultStartLevel=4\n");
		configBuilder.append("osgi.bundles=");
		for (BundleInfo bundleInfo : bundleInfos) {
			configBuilder.append(bundleInfo.getBundleName());
			if (!bundleInfo.isFragment()) {
				Integer startLevel = bundleStartLevels.get(bundleInfo.getBundleName());
				configBuilder.append("@");
				if (startLevel != null) {
					configBuilder.append(startLevel).append(":");
				}
				configBuilder.append("start");
			}
			configBuilder.append(",\\\n");
		}
		configBuilder.append("org.eclipse.equinox.servletbridge.extensionbundle");
		return configBuilder.toString();
	}

	private static void closeQuietly(Closeable closeable) {
		if (closeable == null) {
			return;
		}
		try {
			closeable.close();
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "error closing closeable.", e);
		}
	}

	private BundleInfo bundleInfo(File pluginFile) {
		String pluginFileName = pluginFile.getName();
		if (!pluginFileName.endsWith(".jar")) {
			// Skip non jars
			LOGGER.log(Level.FINE, "Skipping non jar: " + pluginFileName);
			return null;
		} else if (pluginFileName.startsWith("org.eclipse.osgi_")) {
			// Skip osgi framework
			LOGGER.log(Level.FINE, "Skipping osgi framework jar: " + pluginFileName);			
			return null;
		} else {
			// find if it's a fragment or bundle
			Manifest manifest = loadManifest(pluginFile);
			if (manifest == null) {
				LOGGER.log(Level.FINE, "Skipping jar wihtout manifest: " + pluginFileName);
				return null;
			}
			String symbolicName = symbolicName(manifest);
			if (symbolicName == null) {
				LOGGER.log(Level.FINE, "Skipping jar wihtout Bundle names: " + pluginFileName);
			}

			boolean fragment = manifest.getMainAttributes().getValue(FRAGMENT_HOST) != null;
			BundleInfo bundleInfo = new BundleInfo(pluginFileName, symbolicName, fragment);
			LOGGER.log(Level.FINE, "Found : " + bundleInfo);
			return bundleInfo;
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

	private Manifest loadManifest(File jarFile) {
		FileInputStream fis = null;
		ZipInputStream zis = null;
		try {
			fis = new FileInputStream(jarFile);
			zis = new ZipInputStream(fis);

			ZipEntry entry;
			while ((entry = zis.getNextEntry()) != null) {
				if (entry.getName().equals("META-INF/MANIFEST.MF")) {
					return new Manifest(zis);
				}
			}
			return null;
		} catch (IOException e) {
			throw new ConfigBuildingException("Error loading manifest information for " + jarFile, e);
		} finally {
			closeQuietly(fis);
			closeQuietly(zis);
		}
	}

	/**
	 * Build configuration/config.ini file from the list of plugins in an
	 * "eclipse" directory.
	 * 
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		if (args.length < 1) {
			System.out.println("usage:");
			System.out.println("\tequinox-config-builder <eclipseDirectory> [<defaultStartLevel> [<bundleStartLevelsPropertyFile>]]");
			System.out.println("\teclipseDirectory: the eclipse directory that should contains a plugins subdirectory and where configuration/config.ini will be generated.");
			System.out.println("\tdefaultStartLevel: the value to use for osgi.bundles.defaultStartLevel. When not specified defaults to "+DEFAULT_DEFAULT_START_LEVEL);
			System.out.println("\tbundleStartLevelsPropertyFile: a property file to specify start level for bundles that should not use defaultStartLevel.");
			System.out.println("\t\t The file should use bundle symbolic name as key and start level as value e.g. org.eclipse.equinox.common=2");
			return;
		}

		File eclipseDirectory = new File(args[0]);
		System.out.println("equinox-config-builder:");
		System.out.println("\teclipseDirectory: " + eclipseDirectory);
		int defaultStartLevel = 4;
		if (args.length >= 2) {
			defaultStartLevel = Integer.parseInt(args[1]);
		}
		System.out.println("\tdefaultStartLevel: " + defaultStartLevel);

		Map<String, Integer> bundleStartLevels;
		if (args.length >= 3) {
			Properties bundleStartLevelsProperties = new Properties();
			File bundleStartLevelsPropertyFile = new File(args[2]);
			System.out.println("\bundleStartLevelsPropertyFile: " + bundleStartLevelsPropertyFile);
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(bundleStartLevelsPropertyFile);
				bundleStartLevelsProperties.load(fis);
			} finally {
				closeQuietly(fis);
			}
			bundleStartLevels = startLevelsMapFromProperties(bundleStartLevelsProperties);
		} else {
			bundleStartLevels = Collections.emptyMap();
		}
		
		ConfigBuilder configBuilder = new ConfigBuilder(eclipseDirectory, defaultStartLevel, bundleStartLevels);
		configBuilder.buildConfigFile();
	}
}
