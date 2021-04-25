package com.cisco.camel.processors.token;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cisco.camel.bean.Token;

@Component("clearTokenCacheProcessor")
public class ClearTokenCacheProcessor implements Processor {

	private static final Logger logger = LoggerFactory.getLogger(ClearTokenCacheProcessor.class);
	
	@Autowired
	private Token token;

	/**
	 * This method is used to build to clear the token and cookie from the cache.
	 * 
	 */
	@Override
	public void process(Exchange exchange) throws Exception {		
		token.setTimeStamp(null);
		token.setTokenPresent(false);
		token.setCookie(null);
		token.setToken(null);
		exchange.setProperty("Cookie", null);
		logger.debug("clear the session cookie and token");
	}
}
