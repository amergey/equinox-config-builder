package com.sarod.equinox.config.builder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sarod.equinox.config.builder.utils.IOUtils;
import com.sarod.equinox.config.builder.utils.Preconditions;

public final class ConfigBuilder {

	private static final String CONFIG_INI_FILE = "config.ini";

	private static final String CONFIGURATION_DIR = "configuration";

	protected final static Logger LOGGER = Logger.getLogger(ConfigBuilder.class.getName());

	public static final int DEFAULT_DEFAULT_START_LEVEL = 4;
	private final File eclipseDirectory;
	private final int defaultStartLevel;
	private final Map<String, Integer> bundleStartLevels;

	private BundleInfoLoader bundleInfoLoader;
	private ConfigWriter configWriter;

	public static Map<String, Integer> startLevelsMapFromPropertyFile(File bundleStartLevelsPropertyFile) {
		Properties bundleStartLevelsProperties = new Properties();
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(bundleStartLevelsPropertyFile);
			bundleStartLevelsProperties.load(fis);
			return startLevelsMapFromProperties(bundleStartLevelsProperties);
		} catch (IOException e) {
			throw new ConfigBuildingException("Error loading bundleStartLevelsPropertyFile: " + bundleStartLevelsPropertyFile, e);
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
		Preconditions.checkNotNull(eclipseDirectory);
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
		this.configWriter = new ConfigWriter();
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
		for (BundleInfo bundleInfo : bundleInfoLoader.loadBundleInfos(pluginsDirectory)) {
			if (shouldExcludeBundle(bundleInfo)) {
				LOGGER.log(Level.FINE, "Excluding {0}", bundleInfo);
			} else {
				LOGGER.log(Level.FINE, "Adding {0}", bundleInfo);
				bundleInfos.add(bundleInfo);
			}
		}

		File configFile = new File(new File(eclipseDirectory, CONFIGURATION_DIR), CONFIG_INI_FILE);
		configWriter.writeConfig(new ConfigDescriptor(defaultStartLevel, bundleInfos, bundleStartLevels), configFile);

		LOGGER.log(Level.INFO, "Generating configuration/config.ini: Done " + bundleInfos.size() + " plugins configured.");
	}

	private boolean shouldExcludeBundle(BundleInfo bundleInfo) {
		// Exclude osgi framework
		return bundleInfo.getBundleName().equals("org.eclipse.osgi");
	}

}
