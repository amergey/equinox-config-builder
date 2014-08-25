package com.sarod.equinox.config.builder.ant;

public class BundleStartLevel {

	private String bundleName;

	private int startLevel = -1;

	public BundleStartLevel() {

	}

	public int getStartLevel() {
		return startLevel;
	}

	public void setStartLevel(int bundleStartLevel) {
		this.startLevel = bundleStartLevel;
	}

	public String getBundleName() {
		return bundleName;
	}

	public void setBundleName(String bundleName) {
		this.bundleName = bundleName;
	}

}
