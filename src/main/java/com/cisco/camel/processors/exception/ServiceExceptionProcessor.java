package com.cisco.camel.processors.exception;

import java.util.HashMap;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.http.base.HttpOperationFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cisco.camel.bean.Trace;
import com.cisco.camel.config.PropertyHolder;

/**
 * This class is used to process http invocation failures response.
 * @author arnanda
 *
 */
@Component("serviceExceptionProcessor")
public class ServiceExceptionProcessor implements Processor {

	private static final Logger logger = LoggerFactory.getLogger(ServiceExceptionProcessor.class);

	@Autowired
	private PropertyHolder propertyHolder;
	
	@Autowired
	private ExceptionUtility exceptionUtility;

	@Override
	public void process(Exchange exchange) throws Exception {

		HashMap<Object, Object> netChangeResponse = new HashMap<Object, Object>();
		HashMap<Object, Object> description = new HashMap<Object, Object>();
		HttpOperationFailedException cause = exchange.getProperty(Exchange.EXCEPTION_CAUGHT,
				HttpOperationFailedException.class);
		exchange.getOut().setHeader(Exchange.HTTP_RESPONSE_CODE, String.valueOf(cause.getStatusCode()));
		netChangeResponse.put("code", String.valueOf(cause.getStatusCode()));
		String status = null;
		if (cause.getStatusCode() >= 400 && cause.getStatusCode() < 500) {
			netChangeResponse.put("status", propertyHolder.getDataFailure());
			status = propertyHolder.getDataFailure();
		} else {
			netChangeResponse.put("status", propertyHolder.getSystemFailure());
			status = propertyHolder.getSystemFailure();
		}
		description.put("message", cause.getStatusText());
		description.put("detail", cause.getResponseBody());
		netChangeResponse.put("description", description);
		logger.error("HTTP requester error details: code:" + cause.getStatusCode() + "Message:" + cause.getStatusText()
				+ "Response Body:" + cause.getResponseBody());
		
		Trace trace = null;
		if (null != exchange.getProperties().get("trace")) {
			trace = (Trace) exchange.getProperties().get("trace");
		}
		if (null != exchange.getProperties().get("transactionId")) {
			trace.setBusinessId(exchange.getProperties().get("transactionId").toString());
		}

		exceptionUtility.writeExceptionToKafka(cause, String.valueOf(cause.getStatusCode()), status, trace, cause.getResponseBody());
		exchange.getMessage().setBody(netChangeResponse);
	}
}
