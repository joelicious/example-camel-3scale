package org.camel.examples.impl;

import java.util.HashMap;
import java.util.Map;

public class SimpleCache {

	private static Map<String, Boolean> simpleCache = new HashMap<String, Boolean>();

	public boolean hasAPIKeyBeenCachedAndValidated(String apiKey) {
		if (simpleCache.containsKey(apiKey)) {
			return simpleCache.get(apiKey).booleanValue();
		}
		return false;
	}
	
	public void updateValidity(String apiKey, String validity) {
		if (validity.equalsIgnoreCase("valid")) {
			simpleCache.put(apiKey, Boolean.TRUE);
		}
	}

	public void tokenIsValid(String apiKey) {
		simpleCache.put(apiKey, Boolean.TRUE);
	}

	public void tokenIsNotValid(String apiKey) {
		simpleCache.put(apiKey, Boolean.FALSE);
	}

}
