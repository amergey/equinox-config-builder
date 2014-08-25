package com.sarod.equinox.config.builder;

public class ConfigBuildingException extends RuntimeException {
	
	private static final long serialVersionUID = -8282696415618079032L;

	public ConfigBuildingException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConfigBuildingException(String message) {
		super(message);
	}

}
