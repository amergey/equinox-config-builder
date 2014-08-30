package com.sarod.equinox.config.builder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.sarod.equinox.config.builder.utils.Preconditions.*;

/**
 * In memory description of an equinox config This class is immutable.
 * 
 * @author sarod
 *
 */
public final class ConfigDescriptor {

	private final int defaultStartLevel;
	private final Map<String, Integer> bundleStartLevels;
	private final Collection<BundleInfo> bundleInfos;

	public ConfigDescriptor(int defaultStartLevel, Collection<BundleInfo> bundleInfos, Map<String, Integer> bundleStartLevels) {
		checkNotNull(bundleInfos);
		checkNotNull(bundleStartLevels);
		this.defaultStartLevel = defaultStartLevel;
		this.bundleInfos = Collections.unmodifiableList(new ArrayList<BundleInfo>(bundleInfos));
		this.bundleStartLevels = Collections.unmodifiableMap(new HashMap<String, Integer>(bundleStartLevels));
	}

	public ConfigDescriptor(int defaultStartLevel, Collection<BundleInfo> bundleInfos) {
		this(defaultStartLevel, bundleInfos, Collections.<String, Integer> emptyMap());
	}

	public int getDefaultStartLevel() {
		return defaultStartLevel;
	}

	public Map<String, Integer> getBundleStartLevels() {
		return bundleStartLevels;
	}

	public Collection<BundleInfo> getBundleInfos() {
		return bundleInfos;
	}

}
