package org.camel.examples.impl;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultCamelContext;

public class SimpleRouteClient {

	public static void main(String args[]) throws Exception {
		CamelContext context = new DefaultCamelContext();

		ProducerTemplate template = context.createProducerTemplate();

		context.start();

		template.sendBody("direct:simple", null, null);

		context.stop();
	}

}
