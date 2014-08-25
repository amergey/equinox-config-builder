package com.sarod.equinox.config.builder.ant;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import com.sarod.equinox.config.builder.ConfigBuilder;

public class EquinoxConfigBuilderTask extends Task {

	private File eclipseDirectory;

	private File bundlesStartLevelPropertyFile;

	private int defaultStartLevel = ConfigBuilder.DEFAULT_DEFAULT_START_LEVEL;

	private List<BundleStartLevel> bundleStartLevels = new ArrayList<BundleStartLevel>();

	public File getEclipseDirectory() {
		return eclipseDirectory;
	}

	public void setEclipseDirectory(File eclipseDirectory) {
		this.eclipseDirectory = eclipseDirectory;
	}

	public int getDefaultStartLevel() {
		return defaultStartLevel;
	}

	public void setDefaultStartLevel(int defaultStartLevel) {
		this.defaultStartLevel = defaultStartLevel;
	}

	public BundleStartLevel createBundleStartLevel() {
		BundleStartLevel bundleStartLevel = new BundleStartLevel();
		bundleStartLevels.add(bundleStartLevel);
		return bundleStartLevel;
	}

	public File getBundlesStartLevelPropertyFile() {
		return bundlesStartLevelPropertyFile;
	}

	public void setBundlesStartLevelPropertyFile(File bundlesStartLevelPropertyFile) {
		this.bundlesStartLevelPropertyFile = bundlesStartLevelPropertyFile;
	}

	@Override
	public void execute() throws BuildException {
		ConfigBuilder builder = new ConfigBuilder(eclipseDirectory, defaultStartLevel, buildBundleStartLevelMap());
		builder.buildConfigFile();
	}

	private Map<String, Integer> buildBundleStartLevelMap() {
		Map<String, Integer> startLevels = new HashMap<String, Integer>();
		
		// Load from property file first
		if (bundlesStartLevelPropertyFile != null) {
			startLevels.putAll(ConfigBuilder.startLevelsMapFromPropertyFile(bundlesStartLevelPropertyFile));
		}
		
		// Override/complements with child elements
		for (BundleStartLevel bundleStartLevel : bundleStartLevels) {
			if (bundleStartLevel.getBundleName() != null) {
				if (bundleStartLevel.getStartLevel() == -1) {
					// override to -1 means reset to default
					startLevels.remove(bundleStartLevel.getBundleName());
				} else {
					startLevels.put(bundleStartLevel.getBundleName(), bundleStartLevel.getStartLevel());
				}
			}			
		}
		
		return startLevels;
	}
}
