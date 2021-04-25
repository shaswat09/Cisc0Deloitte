package com.cisco.camel.processors.exception;

import java.util.HashMap;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cisco.camel.bean.Trace;
import com.cisco.camel.config.PropertyHolder;

/**
 * This is a exception handler class to process and prepare exception response
 * for Kafka Exceptions in the service.
 * 
 * @author bharath
 *
 */
@Component("kafkaExceptionProcessor")
public class KafkaExceptionProcessor implements Processor {

	private static final Logger logger = LoggerFactory.getLogger(KafkaExceptionProcessor.class);

	@Autowired
	private PropertyHolder propertyHolder;

	@Autowired
	private ExceptionUtility exceptionUtility;

	@SuppressWarnings("deprecation")
	@Override
	public void process(Exchange exchange) throws Exception {

		HashMap<Object, Object> finalResponse = new HashMap<Object, Object>();
		HashMap<Object, Object> subscriptionErrorResponse = new HashMap<Object, Object>();
		HashMap<Object, Object> description = new HashMap<Object, Object>();
		Exception cause = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
		exchange.getOut().setHeader(Exchange.HTTP_RESPONSE_CODE, propertyHolder.getCode());
		subscriptionErrorResponse.put("code", propertyHolder.getCode());
		subscriptionErrorResponse.put("status", propertyHolder.getSystemFailure());
		description.put("message", cause.getMessage() != null ? cause.getMessage() : cause.getClass().getSimpleName());
		subscriptionErrorResponse.put("Description", description);
		finalResponse.put("bslResponse", subscriptionErrorResponse);
		Trace trace = null;
		if (null != exchange.getProperties().get("trace")) {
			trace = (Trace) exchange.getProperties().get("trace");
		}
		if (null != exchange.getProperties().get("transactionId")) {
			trace.setBusinessId(exchange.getProperties().get("transactionId").toString());
		}

		exceptionUtility.writeExceptionToKafka(cause, propertyHolder.getCode(), propertyHolder.getSystemFailure(),
				trace, null);
		exchange.getMessage().setBody(finalResponse);
	}

}
