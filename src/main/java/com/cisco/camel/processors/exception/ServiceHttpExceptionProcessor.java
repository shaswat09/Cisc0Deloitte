package com.cisco.camel.processors.exception;

import java.util.HashMap;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;

import com.cisco.camel.bean.Trace;
import com.cisco.camel.config.PropertyHolder;

/**
 * This class is used to process Rest template http invocation failures response.
 * @author aarkumar
 *
 */
@Component("serviceHttpExceptionProcessor")
public class ServiceHttpExceptionProcessor implements Processor {

	private static final Logger logger = LoggerFactory.getLogger(ServiceHttpExceptionProcessor.class);

	@Autowired
	private PropertyHolder propertyHolder;
	
	@Autowired
	private ExceptionUtility exceptionUtility;

	@SuppressWarnings("deprecation")
	@Override
	public void process(Exchange exchange) throws Exception {

		HashMap<Object, Object> netChangeResponse = new HashMap<Object, Object>();
		HashMap<Object, Object> description = new HashMap<Object, Object>();
		HttpStatusCodeException cause = exchange.getProperty(Exchange.EXCEPTION_CAUGHT,
				HttpStatusCodeException.class);
		exchange.getOut().setHeader(Exchange.HTTP_RESPONSE_CODE, String.valueOf(cause.getStatusCode().value()));
		netChangeResponse.put("code", String.valueOf(cause.getStatusCode().value()));
		String status = null;
		if (cause.getStatusCode().value() >= 400 && cause.getStatusCode().value() < 500) {
			netChangeResponse.put("status", propertyHolder.getDataFailure());
			status = propertyHolder.getDataFailure();
		} else {
			netChangeResponse.put("status", propertyHolder.getSystemFailure());
			status = propertyHolder.getSystemFailure();
		}
		description.put("message", cause.getLocalizedMessage());
		description.put("detail", cause.getMessage());
		netChangeResponse.put("description", description);
		logger.error("Rest Template HTTP requester error details: code:" + cause.getStatusCode() + "Message:" + cause.getLocalizedMessage()
				+ "Response Body:" + cause.getMessage());
		Trace trace = null;
		if (null != exchange.getProperties().get("trace")) {
			trace = (Trace) exchange.getProperties().get("trace");
		}
		if (null != exchange.getProperties().get("transactionId")) {
			trace.setBusinessId(exchange.getProperties().get("transactionId").toString());
		}
		exceptionUtility.writeExceptionToKafka(cause, String.valueOf(cause.getStatusCode()), status, trace, null);
		exchange.getMessage().setBody(netChangeResponse);
	}
}
