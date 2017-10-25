package org.camel.examples.impl;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;

public class SimpleRouteClient {

	private SimpleRouteClient() {

	}

	public static void main(String args[]) throws Exception {
		CamelContext context = new DefaultCamelContext();

		
		context.addRoutes(new RouteBuilder() {
            public void configure() {
                from("direct:simple")
                	.log("The is body ${body}")
                	.to("threescale-authrep:test?serverHost=su1.3scale.net&serverPort=443&serviceId=2555417749097&serviceToken=0f4fe5d82dc61c57f7f02b602bd9b42e670748097c3bb8ea4f644fa4c3be19a2&userKey=0bcf317e88e71915442d10c37986f040");
            }
        });

		ProducerTemplate template = context.createProducerTemplate();

		System.out.println("Starting Camel Context");
		context.start();

		template.sendBody("direct:simple", "Hello");

		Thread.sleep(1000);
		
		System.out.println("Stopping Camel Context");
		context.stop();
	}

}
