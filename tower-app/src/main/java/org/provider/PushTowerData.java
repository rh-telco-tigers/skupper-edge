package org.provider;

import javax.enterprise.context.ApplicationScoped;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class PushTowerData extends RouteBuilder {

	@ConfigProperty(name = "tower") 
	String message;
	
	@Override
	public void configure() throws Exception {
		// produces messages to kafka
		from("timer:foo?period=5s")
		.setHeader(Exchange.HTTP_METHOD, constant(org.apache.camel.component.http.HttpMethods.POST))
		.setHeader(Exchange.CONTENT_TYPE, constant("application/vnd.kafka.json.v2+json"))
		.setBody(simple("{\"records\": [ { \"key\": \"key-1\", \"value\": \""+message+"\" } ] }"))
		.log("${body}")
		.to("http://localhost/topics/devices")
		;


	}

}