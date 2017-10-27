package org.camel.examples.impl;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.SimpleRegistry;
import org.apache.camel.language.simple.SimpleLanguage;

import threescale.v3.api.ParameterMap;

public class SimpleRouteClient {

	public static void main(String args[]) throws Exception {
		
		SimpleCache cacheService = new SimpleCache();
		
		PayloadProcessor payloadProcessor = new PayloadProcessor();
		payloadProcessor.setSimpleCache(cacheService);
		
		ParameterMap parameterMap = new ParameterMap();
		parameterMap.add("service_id", "2555417749097");
		parameterMap.add("service_token", "0f4fe5d82dc61c57f7f02b602bd9b42e670748097c3bb8ea4f644fa4c3be19a2");

		SimpleRegistry reg = new SimpleRegistry();
		reg.put("parameterMap", parameterMap);
		reg.put("cacheService", cacheService);

		CamelContext context = new DefaultCamelContext(reg);

		context.addRoutes(new RouteBuilder() {
			public void configure() {

				from("direct:simple")
					.log("Payload Received")
					.process(payloadProcessor)
					.setHeader("OPERATION_ALLOWED", SimpleLanguage.simple("TRUE"))
					.choice()
						.when(header("CACHED_AND_VALID").isEqualTo("VALID"))
							.log("Payload in Cache; Send")
							.to("seda:authAndRep")
						.otherwise()
							.log("Payload not in Cache; Auth")
							.to("direct:authorize")
					.end()
					.choice()
						.when(header("OPERATION_ALLOWED").isEqualTo("TRUE"))
							// Do Business Logic on Payload
							.log("BUSINESS LOGIC")
						.otherwise()
							.log("OPERATION NOT ALLOWED")
					.end();

				from("seda:authAndRep")
					.to("threescale-authrep:saasAdmin?serverHost=su1.3scale.net&serverPort=443&parameterMap=#parameterMap")
					.to("bean:cacheService?method=updateValidity(${header.API_KEY}, ${header.THREESCALE_AUTH})");

				from("direct:authorize")
					.to("threescale-authorize:saasAdmin?serverHost=su1.3scale.net&serverPort=443&parameterMap=#parameterMap")
					.to("bean:cacheService?method=updateValidity(${header.API_KEY}, ${header.THREESCALE_AUTH})")
					.choice()
						.when(header("THREESCALE_AUTH").isEqualTo("VALID"))
							.log("API Key Authorized; now Reporting")
							.to("threescale-report:saasAdmin?serverHost=su1.3scale.net&serverPort=443&parameterMap=#parameterMap")
						.otherwise()
							.log("Api Key Not Authorized")
							.setHeader("OPERATION_ALLOWED", SimpleLanguage.simple("FALSE"))
					.end();
				
			}
		});

		ProducerTemplate template = context.createProducerTemplate();

		context.start();

		SimplePayload simplePayload = new SimplePayload();
		simplePayload.setApiKey("0bcf317e88e71915442d10c37986f040");
		simplePayload.setThePayload("The Payload");

		template.sendBody("direct:simple", simplePayload);
		
		simplePayload.setThePayload("Second Payload");
		
		template.sendBody("direct:simple", simplePayload);
		template.sendBody("direct:simple", simplePayload);


		Thread.sleep(5000);

		System.out.println("Simple Client Stopping");
		context.stop();
	}

}
