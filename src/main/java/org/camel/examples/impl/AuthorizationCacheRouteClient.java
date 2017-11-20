package org.camel.examples.impl;

import java.util.HashMap;
import java.util.Map;

import javax.cache.Caching;
import javax.cache.spi.CachingProvider;

import org.apache.camel.CamelAuthorizationException;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.threescale.ThreeScaleConstants;
import org.apache.camel.component.threescale.security.ThreeScaleAuthPolicy;
import org.apache.camel.impl.DefaultCamelContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthorizationCacheRouteClient {

	private static final Logger LOG = LoggerFactory.getLogger(AuthorizationCacheRouteClient.class);

	private static final String API_KEY = "0bcf317e88e71915442d10c37986f040";
	private static final String SERVICE_ID = "2555417749097";
	private static final String SERVICE_TOKEN = "0f4fe5d82dc61c57f7f02b602bd9b42e670748097c3bb8ea4f644fa4c3be19a2";

	public static void main(String args[]) throws Exception {

		Map<String, Object> headerMap = new HashMap<String, Object>();
		headerMap.put(ThreeScaleConstants.THREE_SCALE_SERVICE_ID, SERVICE_ID);
		headerMap.put(ThreeScaleConstants.THREE_SCALE_SERVICE_TOKEN, SERVICE_TOKEN);
		headerMap.put(ThreeScaleConstants.THREE_SCALE_API_KEY, API_KEY);

		CachingProvider cachingProvider = Caching.getCachingProvider("com.hazelcast.cache.HazelcastCachingProvider");
		final ThreeScaleAuthPolicy threeScalePolicy = new ThreeScaleAuthPolicy("su1.3scale.net", 443, cachingProvider);

		CamelContext context = new DefaultCamelContext();

		context.addRoutes(new RouteBuilder() {
			public void configure() {

				onException(CamelAuthorizationException.class).log("Exception Occurred");

				from("direct:threeScale")
					.policy(threeScalePolicy)
					.choice()
						.when(header(ThreeScaleConstants.THREE_SCALE_CACHE_USED).isEqualTo(Boolean.TRUE))
							.log("log:3scale Cache Used")
							.to("seda:asyncInvoke3scale")
						.otherwise()
							.log("log:3Scale Invoked");
				
				from("seda:asyncInvoke3scale")
					.to("threescale-report:saasAdmin?serverHost=su1.3scale.net&serverPort=443");

			}
		});

		ProducerTemplate template = context.createProducerTemplate();

		context.start();

		template.sendBodyAndHeaders("direct:threeScale", "The Payload", headerMap);
		template.sendBodyAndHeaders("direct:threeScale", "The Payload", headerMap);
		template.sendBodyAndHeaders("direct:threeScale", "The Payload", headerMap);

		Thread.sleep(2000);

		LOG.info("AuthorizationCacheRouteClient Stopping");
		context.stop();
		
		cachingProvider.close();

	}

}
