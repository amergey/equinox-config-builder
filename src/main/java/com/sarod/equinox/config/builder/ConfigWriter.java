package com.sarod.equinox.config.builder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sarod.equinox.config.builder.utils.IOUtils;

public class ConfigWriter {

	public ConfigWriter() {
	}

	public String buildConfigContent(ConfigDescriptor descriptor) {
		List<BundleInfo> bundleInfos = new ArrayList<BundleInfo>(descriptor.getBundleInfos());
		// Sort bundle info to make the config file more readable
		Collections.sort(bundleInfos);

		StringBuilder configBuilder = new StringBuilder();
		configBuilder.append("#Product Runtime Configuration File\n");
		configBuilder.append("osgi.bundles.defaultStartLevel=4\n");
		configBuilder.append("osgi.bundles=");
		for (BundleInfo bundleInfo : bundleInfos) {
			configBuilder.append(bundleInfo.getBundleName());
			if (!bundleInfo.isFragment()) {
				Integer startLevel = descriptor.getBundleStartLevels().get(bundleInfo.getBundleName());
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

	public void writeConfig(ConfigDescriptor descriptor, File targetConfigFile) {
		byte[] configContent;
		try {
			configContent = buildConfigContent(descriptor).getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			// UTF-8 is always present this should not happen
			throw new AssertionError(e);
		}
		targetConfigFile.getParentFile().mkdirs();
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(targetConfigFile);
			out.write(configContent);
			out.flush();
		} catch (IOException e) {
			throw new ConfigBuildingException("Error writing config file: " + targetConfigFile, e);
		} finally {
			IOUtils.closeQuietly(out);
		}
	}

}
