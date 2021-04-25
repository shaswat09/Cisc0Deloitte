package com.cisco.camel.routes;

import java.util.concurrent.TimeoutException;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.http.common.HttpOperationFailedException;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.http.conn.HttpHostConnectException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;

import com.cisco.camel.config.PropertyHolder;

/**
 * This class contains exception handling and routing definition for Net Change
 * 
 * 
 * @author arnanda
 *
 */

@Component
public class NetChangeRoutes extends RouteBuilder {

	public static final Logger Logger = LoggerFactory.getLogger(NetChangeRoutes.class);

	@Autowired
	private PropertyHolder propertyHolder;

	/**
	 * This methods configures camel components and routes.
	 */

	@Override
	public void configure() throws Exception {

		Logger.info("post-net-change-service configure method starts");
		// ================================ Exception Handling starts================================//
	
		
		onException(HttpOperationFailedException.class)
		.maximumRedeliveries(2)
		.process("serviceExceptionProcessor")
		.handled(true)
		.log("HttpOperationFailedException Handled")
		.end();
		
		onException(TimeoutException.class)
		.maximumRedeliveries(2)
		.process("kafkaExceptionProcessor")
		.handled(true)
		.end();

		onException(HttpStatusCodeException.class)
       .process("serviceHttpExceptionProcessor")
		.handled(true)
		.log("HttpStatusCodeException Handled")
		.end();

		onException(ArrayIndexOutOfBoundsException.class)
		.process("exceptionHandlerProcessor")
		.handled(true)
		.log("ArrayIndexOutOfBoundsException Handled")
		.end();

		
		onException(NullPointerException.class)
		  .process("exceptionHandlerProcessor")
		  .handled(true)
          .log("Null Pointer Exeption Handled")
	      .end();
	 	  
		onException(HttpHostConnectException.class)
        .log("HttpHostConnectException.class")
        .maximumRedeliveries(2)
        .process("exceptionHandlerProcessor")         
        .log("HttpHostConnect Exception Handled")            
        .end();      
        
        onException(RestClientException.class)
        .log("RestClientException.class")
        .maximumRedeliveries(2)
        .process("exceptionHandlerProcessor")         
        .log("RestClientException Exception Handled")
        .end();      

			   
		onException(Exception.class)
		.process("exceptionHandlerProcessor")
		.handled(true)
		.log("Exception Caught")
		.end();
		
		// ================================ Exception Handling ends =========================================//
		

		getContext().setAllowUseOriginalMessage(true);
		getContext().setStreamCaching(true);

		
		
		// ===================================Route Definition===========================================//
		
		from("direct:post-net-change-service")
		.routeId("post-net-change-service-logic")
		.log("net-change-service Start")
		.process("mapNetChangeRequest")
		.log("${body}")
		.marshal().json()
		.log("${body}")
  	    .to("direct:get-token")
  	    .log("Generated SAP Token:: " + "${exchangeProperty.token}")
		.log("Generated SAP Cookie:: " + "${exchangeProperty.Cookie}")
		//.removeHeaders("*")
		.process(new Processor() {
			@Override
			public void process(Exchange exchange) throws Exception {
				Message message = exchange.getUnitOfWork().getOriginalInMessage();
				String originalRequest = message.getBody(String.class);
				exchange.getMessage().setBody(originalRequest);
			}
		})
		.unmarshal().json()
		.process("mapNetChangeRequest")
		.marshal().json()
		.log("${body}")
		.to("direct:net-change-send")
		.process("mapNetChangeResponse")
		.log("${body}")
		.marshal().json(JsonLibrary.Jackson)
		.log("post-net-change-service Response - >  ${body}")
		.unmarshal().json(JsonLibrary.Jackson)
		.log("post-net-change-service End")
		.end();
		
		from("direct:clear-sap-session")
		.routeId("get-clear-sap-session-logic")
		.choice()
		   .when(exchangeProperty("Cookie").isNotNull())
		   	.log("clear sap session Route Start")
		   	.setHeader(Exchange.HTTP_METHOD, constant("GET"))
		   	.removeHeader(Exchange.HTTP_URI)
		   	.removeHeader(Exchange.HTTP_PATH)
		   	.setHeader("Cookie", exchangeProperty("Cookie"))
		   	.setBody(simple(null))
		   	.to(propertyHolder.getBrimClearSessionURL())
		   	.process("clearTokenCacheProcessor")
		   	.log("clear sap session Route End")
		.end();

		
		from("direct:get-token")
		.routeId("get-token-logic")
		.log("get token Route Start")
		.process("tokenCacheRequestProcessor")
		.choice()
			.when(exchangeProperty("isTokenRequired").contains(propertyHolder.getTokenNotRequired()))
			.log("Token is valid")
			.when(exchangeProperty("isTokenRequired").contains(propertyHolder.getTokenRequired()))
			.to("direct:clear-sap-session")
			.log("Token is getting generated")
			.setHeader("Exchange.HTTP_METHOD", constant("GET"))
				
		    .setHeader("Authorization", constant(propertyHolder.getBrimAuthorization()))
		    .setHeader("X-CSRF-Token", constant("Fetch"))
		  //.setHeader("x-sap-security-session", constant("disabled"))
		    .removeHeader(Exchange.HTTP_URI)
		    .removeHeader(Exchange.HTTP_PATH)
		    .to("log:DEBUG?showBody=true&showHeaders=true")
		    .to(propertyHolder.getTokenEndpoint())
		    .to("log:DEBUG?showBody=true&showHeaders=true")
		    .process("tokenCacheResponseProcessor")
		    .log("Generated Token:: " + "${exchangeProperty.token}")
		    .log("Generated Cookie:: " + "${exchangeProperty.Cookie}")
		.end();
		
		
		

		from("direct:net-change-send")
		.routeId("net-change-send-logic")
		.log("net change send Start")		
		.removeHeader(Exchange.HTTP_URI).removeHeader(Exchange.HTTP_PATH)
		.removeHeader("set-cookie")
		.setHeader(Exchange.HTTP_METHOD, constant("POST"))
		.setHeader("Cookie", exchangeProperty("Cookie"))	
		.setHeader("Accept-Encoding", constant("gzip"))
		.setHeader("Content-Type", constant("application/json"))
		.setHeader("X-CSRF-Token", exchangeProperty("token"))
		.setHeader("Accept", constant("application/json"))
		//.setHeader("x-sap-security-session", constant("disabled"))
		.setHeader("Authorization", constant(propertyHolder.getBrimAuthorization()))
		.log("____________________________________________________________________________")
		.log("$simple{in.headers}")
		.to("log:DEBUG?showBody=true&showHeaders=true")
		.to(propertyHolder.getServiceEndpoint())	
		.log("${body}")
		.setBody(simple("${body}"))
		.unmarshal().json()
		.log("${body}")
		.log("net change Send End")
		.end();
		
		
		}
}
