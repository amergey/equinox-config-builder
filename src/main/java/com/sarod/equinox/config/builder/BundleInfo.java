package com.sarod.equinox.config.builder;


public final class BundleInfo implements Comparable<BundleInfo> {

	private final String fileName;
	private final boolean fragment;
	private final String bundleName;

	public BundleInfo(String fileName, String bundleName, boolean fragment) {
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