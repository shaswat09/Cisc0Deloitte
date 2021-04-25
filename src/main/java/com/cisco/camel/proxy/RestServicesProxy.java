package com.cisco.camel.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.cisco.camel.config.PropertyHolder;

import java.util.HashMap;
import java.util.Map;

@Service
public class RestServicesProxy {

	private static final Logger logger = LoggerFactory.getLogger(RestServicesProxy.class);

	@Autowired
	private RestTemplate netChangeRestTemplate;

	@Autowired
	private PropertyHolder propertyHolder;

	public HashMap<Object, Object> invokeNetChangeService(final HashMap<Object, Object> requestPayload, String token) {
		logger.info("Entry in method invokeNetChangeService");
		HttpHeaders headers = new HttpHeaders();
		headers.set("Content-Type", "application/json");
		headers.set("Accept", "application/json");
		headers.set("Authorization", "");
		headers.set("X-CSRF-Token", token);
		HttpEntity entity = new HttpEntity<>(requestPayload, headers);
		ResponseEntity response = netChangeRestTemplate.exchange(propertyHolder.getServiceEndpoint(),HttpMethod.POST,entity,Map.class);
		logger.info("NetChange API Response status code: {}", response.getStatusCodeValue());
		logger.info("NetChange Response Body : {}", response.getBody());
		HashMap<Object, Object> finalResponse = (HashMap<Object, Object>) response.getBody();
		logger.info("Exit from method invokeNetChangeService");
		return finalResponse;
	}
}
