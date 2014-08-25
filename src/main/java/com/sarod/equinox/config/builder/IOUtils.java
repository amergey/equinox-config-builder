package com.sarod.equinox.config.builder;

import java.io.Closeable;
import java.util.logging.Level;
import java.util.logging.Logger;

class IOUtils {

	private static final Logger LOGGER = Logger.getLogger(IOUtils.class.getName());

	private IOUtils() {
		throw new AssertionError("Utility class do not instantiate");
	}

	static void closeQuietly(Closeable closeable) {
		if (closeable == null) {
			return;
		}
		try {
			closeable.close();
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "error closing closeable.", e);
		}
	}

}
