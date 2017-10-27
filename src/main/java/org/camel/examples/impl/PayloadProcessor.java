package org.camel.examples.impl;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.language.simple.SimpleLanguage;

import threescale.v3.api.ParameterMap;

public class PayloadProcessor implements Processor {

	private SimpleCache simpleCache;

	public void process(Exchange exchange) {

		SimplePayload payload = exchange.getIn().getBody(SimplePayload.class);
		String apiKey = payload.getApiKey();

		ParameterMap parameterMap = (ParameterMap) exchange.getContext().getRegistry().lookupByName("parameterMap");
		if (null == parameterMap) {
			System.out.println("Parameter Map not available");
		} else {
			parameterMap.add("user_key", apiKey);
		}

		exchange.getIn().setHeader("API_KEY", apiKey);

		boolean cachedAndValid = simpleCache.hasAPIKeyBeenCachedAndValidated(apiKey);

		if (true == cachedAndValid) {
			exchange.getIn().setHeader("CACHED_AND_VALID", SimpleLanguage.simple("VALID", String.class));
		} else {
			exchange.getIn().setHeader("CACHED_AND_VALID", SimpleLanguage.simple("NOT_VALID", String.class));
		}

	}

	public void setSimpleCache(SimpleCache simpleCache) {
		this.simpleCache = simpleCache;
	}

}
