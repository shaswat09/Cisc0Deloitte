package com.cisco.camel.processors.exception;

import java.util.Date;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.cisco.camel.bean.Trace;
import com.cisco.camel.config.PropertyHolder;
import com.cisco.camel.constants.ApplicationConstants;
import com.google.gson.Gson;

import brave.Tracer;

@Component
public class ExceptionUtility {

	private static final Logger logger = LoggerFactory.getLogger(ExceptionUtility.class);

	@Autowired
	private PropertyHolder propertyHolder;

	@Autowired
	private KafkaTemplate<String, String> kafkaTemplate;

	@Value("${netChange.service.exception.topicName}")
	private String TOPIC;

	@Autowired
	Tracer tracer;

	public void writeExceptionToKafka(Exception cause, String code, String status, Trace trace, String description) {

		HashMap<Object, Object> exceptionMessage = new HashMap<Object, Object>();
		exceptionMessage.put("code", code);
		exceptionMessage.put("status", status);
		exceptionMessage.put("corelationId",trace.getTraceId()+ "_" + trace.getSpanId());
		exceptionMessage.put("businessId", trace.getBusinessId()!=null?trace.getBusinessId():ApplicationConstants.UNKNOWN_BUSINESS_ID); 
		exceptionMessage.put("sourceSystem", propertyHolder.getSourceSystem());
		exceptionMessage.put("targetSystem", propertyHolder.getTargetSystem());
		exceptionMessage.put("bslAPIName", propertyHolder.getBslAPIName());
		exceptionMessage.put("businessObject", propertyHolder.getBusinessObject());
		exceptionMessage.put("env", propertyHolder.getEnv());
		//exceptionMessage.put("errorMessage", cause.getMessage() != null ? cause.getMessage() : cause.getClass().getSimpleName());
		exceptionMessage.put("errorMessage", null != description ? description : (cause.getMessage() != null ? cause.getMessage() : cause.getClass().getSimpleName()));
		Date d = new Date(System.currentTimeMillis());
		exceptionMessage.put("errorTimestamp", d);
		Gson gson = new Gson();
		String jsonString = gson.toJson(exceptionMessage);

		kafkaTemplate.send(TOPIC, jsonString);

	}
	
	private String getRootException(Exception ex) {
		StackTraceElement[] elements = ex.getStackTrace();
		String[] message = new String[elements.length];
		for (int iterator = 0; iterator < elements.length; iterator++)
			message[iterator] = "Class Name:" + elements[iterator].getClassName() + " Method Name:"
					+ elements[iterator].getMethodName() + " Line Number:" + elements[iterator].getLineNumber();

		logger.error("Details of exception:" +message[0]);
		return message[0];
	}
}
