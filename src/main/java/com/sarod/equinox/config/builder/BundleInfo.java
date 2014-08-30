package com.sarod.equinox.config.builder;

import static com.sarod.equinox.config.builder.utils.Preconditions.*;

public final class BundleInfo implements Comparable<BundleInfo> {

	private final String fileName;
	private final String bundleName;
	private final String bundleVersion;

	private final String fragmentHostName;

	private BundleInfo(String fileName, String bundleName, String bundleVersion, String fragmentHostName) {
		this.fileName = checkNotNull(fileName);
		this.bundleName = checkNotNull(bundleName);
		this.bundleVersion = bundleVersion;
		this.fragmentHostName = fragmentHostName;
	}

	public static BundleInfo fragment(String fileName, String bundleName, String bundleVersion, String fragmentHostName) {
		return new BundleInfo(fileName, bundleName, bundleVersion, fragmentHostName);
	}

	public static BundleInfo bundle(String fileName, String bundleName, String bundleVersion) {
		return new BundleInfo(fileName, bundleName, bundleVersion, null);
	}

	public String getFileName() {
		return fileName;
	}

	public String getBundleName() {
		return bundleName;
	}

	public String getBundleVersion() {
		return bundleVersion;
	}

	public boolean isFragment() {
		return fragmentHostName != null;
	}

	/**
	 * Retunr the name of the host when {@link #isFragment()}
	 * 
	 * @return
	 */
	public String getHostName() {
		checkState(isFragment(), "not a fragment");
		return fragmentHostName;
	}

	@Override
	public String toString() {
		if (!isFragment()) {
			return "Bundle: [fileName=" + getFileName() + ", bundleName=" + getBundleName() + ", bundleVersion=" + getBundleVersion() + "]";
		} else {
			return "Fragment: [fileName=" + getFileName() + ", bundleName=" + getBundleName() + ", bundleVersion=" + getBundleVersion()
					+ ", hostName=" + getHostName() + "]";
		}
	}

	public int compareTo(BundleInfo anotherBi) {
		return bundleName.compareTo(anotherBi.getBundleName());
	}

}