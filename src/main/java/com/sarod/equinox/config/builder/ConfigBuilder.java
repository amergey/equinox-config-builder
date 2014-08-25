package com.sarod.equinox.config.builder;

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
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ConfigBuilder {

	private static final String CONFIG_INI_FILE = "config.ini";

	private static final String CONFIGURATION_DIR = "configuration";

	protected final static Logger LOGGER = Logger.getLogger(ConfigBuilder.class.getName());

	public static final int DEFAULT_DEFAULT_START_LEVEL = 4;
	private final File eclipseDirectory;
	private final int defaultStartLevel;
	private final Map<String, Integer> bundleStartLevels;

	private BundleInfoLoader bundleInfoLoader;

	public static Map<String, Integer> startLevelsMapFromPropertyFile(File bundleStartLevelsPropertyFile) {
		Properties bundleStartLevelsProperties = new Properties();
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(bundleStartLevelsPropertyFile);
			bundleStartLevelsProperties.load(fis);
			return startLevelsMapFromProperties(bundleStartLevelsProperties);
		} catch (IOException e) {
			throw new ConfigBuildingException("Error loading bundleStartLevelsPropertyFile: " +bundleStartLevelsPropertyFile, e);
		} finally {
			IOUtils.closeQuietly(fis);
		}
	}

	public static Map<String, Integer> startLevelsMapFromProperties(Properties bundleStartLevelsProperties) {
		Map<String, Integer> bundleStartLevelsMap = new HashMap<String, Integer>();
		for (Map.Entry<Object, Object> entry : bundleStartLevelsProperties.entrySet()) {
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

		this.bundleInfoLoader = new BundleInfoLoader();
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

		List<BundleInfo> bundleInfos = loadBundleInfos(pluginsDirectory);

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
			IOUtils.closeQuietly(out);
		}
		LOGGER.log(Level.INFO, "Generating configuration/config.ini: Done " + bundleInfos.size() + " plugins configured.");
	}

	private List<BundleInfo> loadBundleInfos(File pluginsDirectory) {
		List<BundleInfo> bundleInfos = new ArrayList<BundleInfo>();
		for (File pluginFile : pluginsDirectory.listFiles()) {

			String pluginFileName = pluginFile.getName();
			if (!pluginFileName.endsWith(".jar")) {
				// Skip non jars
				LOGGER.log(Level.FINE, "Skipping non jar: " + pluginFileName);
			} else {
				BundleInfo bundleInfo = bundleInfoLoader.loadBundleInfo(pluginFile);
				if (bundleInfo == null) {
					LOGGER.log(Level.FINE, "Skipping non bundle jar: " + pluginFile);
				} else if (shouldExcludeBundle(bundleInfo)) {
					LOGGER.log(Level.FINE, "Skipping excluded bundle : " + bundleInfo);
				} else {
					LOGGER.log(Level.FINE, "Adding : " + bundleInfo);
					bundleInfos.add(bundleInfo);
				}
			}

		}
		// Sort bundle infos in alphabetical order to make config.ini more
		// readable by humans
		Collections.sort(bundleInfos);
		return bundleInfos;
	}

	private boolean shouldExcludeBundle(BundleInfo bundleInfo) {
		// Exclude osgi framework
		return bundleInfo.getBundleName().equals("org.eclipse.osgi");
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

	/**
	 * Build configuration/config.ini file from the list of plugins in an
	 * "eclipse" directory.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		if (args.length < 1) {
			System.out.println("usage:");
			System.out.println("\tequinox-config-builder <eclipseDirectory> [<defaultStartLevel> [<bundleStartLevelsPropertyFile>]]");
			System.out
					.println("\teclipseDirectory: the eclipse directory that should contains a plugins subdirectory and where configuration/config.ini will be generated.");
			System.out.println("\tdefaultStartLevel: the value to use for osgi.bundles.defaultStartLevel. When not specified defaults to "
					+ DEFAULT_DEFAULT_START_LEVEL);
			System.out
					.println("\tbundleStartLevelsPropertyFile: a property file to specify start level for bundles that should not use defaultStartLevel.");
			System.out
					.println("\t\t The file should use bundle symbolic name as key and start level as value e.g. org.eclipse.equinox.common=2");
			return;
		}

		File eclipseDirectory = new File(args[0]);
		System.out.println("equinox-config-builder");
		System.out.println("eclipseDirectory: " + eclipseDirectory);
		int defaultStartLevel = 4;
		if (args.length >= 2) {
			defaultStartLevel = Integer.parseInt(args[1]);
		}
		System.out.println("defaultStartLevel: " + defaultStartLevel);

		Map<String, Integer> bundleStartLevels;
		if (args.length >= 3) {
			File bundleStartLevelsPropertyFile = new File(args[2]);
			System.out.println("bundleStartLevelsPropertyFile: " + bundleStartLevelsPropertyFile);

			bundleStartLevels = startLevelsMapFromPropertyFile(bundleStartLevelsPropertyFile);
		} else {
			bundleStartLevels = Collections.emptyMap();
		}

		ConfigBuilder configBuilder = new ConfigBuilder(eclipseDirectory, defaultStartLevel, bundleStartLevels);
		configBuilder.buildConfigFile();
	}
}
