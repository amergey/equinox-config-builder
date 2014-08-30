package com.sarod.equinox.config.builder.utils;

/**
 * Simple Preconditions check class ala guava Preconditions. used internally
 * 
 * 
 * @author sarod
 *
 */
public final class Preconditions {

	private Preconditions() {
		throw new AssertionError("No instantiate");
	}


	public static void checkState(boolean expression, Object errorMessage) {
		if (!expression) {
			throw new IllegalStateException(String.valueOf(errorMessage));
		}
	}

	public static <T> T checkNotNull(T reference) {
		if (reference == null) {
			throw new NullPointerException();
		}
		return reference;
	}

	public static <T> T checkNotNull(T reference, Object errorMessage) {
		if (reference == null) {
			throw new NullPointerException(String.valueOf(errorMessage));
		}
		return reference;
	}

}
