package com.cisco.camel.processors.token;

import java.sql.Timestamp;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cisco.camel.bean.Token;
import com.cisco.camel.config.PropertyHolder;

@Component("tokenCacheRequestProcessor")
public class TokenCacheRequestProcessor implements Processor {

	private static final Logger logger = LoggerFactory.getLogger(TokenCacheRequestProcessor.class);

	@Autowired
	private PropertyHolder propertyHolder;

	@Autowired
	private Token token;

	/**
	 * This method is used to build the request payload for net change brim
	 * service
	 * 
	 */
	@Override
	public void process(Exchange exchange) throws Exception {
		String isTokenRequired;
		
		if (!token.isTokenPresent()) {
			isTokenRequired = propertyHolder.getTokenRequired();
			exchange.setProperty("isTokenRequired", isTokenRequired);
		} else {
			Timestamp oldTime = token.getTimeStamp();
			Timestamp currentTime = new Timestamp(System.currentTimeMillis());
			long milliseconds1 = oldTime.getTime();
			long milliseconds2 = currentTime.getTime();
			long diff = milliseconds2 - milliseconds1;

			if (diff >= propertyHolder.getTokenExpiry()) {
				logger.info("Setting Token required to true" + diff);
				isTokenRequired = propertyHolder.getTokenRequired();
				String existingCookie = token.getCookie();
				exchange.setProperty("Cookie", existingCookie);
				exchange.setProperty("isTokenRequired", isTokenRequired);
			} else {
				isTokenRequired = propertyHolder.getTokenNotRequired();
				String existingToken = token.getToken();
				String existingCookie = token.getCookie();
				exchange.setProperty("isTokenRequired", isTokenRequired);
				exchange.setProperty("token", existingToken);
				exchange.setProperty("Cookie", existingCookie);
			}
		}

	}
}
