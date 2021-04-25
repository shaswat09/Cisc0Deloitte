package com.cisco.camel.processors.token;

import java.sql.Timestamp;
import java.util.HashMap;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cisco.camel.bean.Token;
import com.cisco.camel.config.PropertyHolder;

@Component("tokenCacheResponseProcessor")
public class TokenCacheResponseProcessor implements Processor {

	private static final Logger logger = LoggerFactory.getLogger(TokenCacheRequestProcessor.class);

	@Autowired
	private Token tokenObj;

	@Override
	public void process(Exchange exchange) throws Exception {
		logger.info("Processing the response received from the token service");
		Message message = exchange.getIn();
		String token = message.getHeader("x-csrf-token", String.class);
		String setCookie = message.getHeader("set-cookie", String.class);
		Timestamp currentTime = new Timestamp(System.currentTimeMillis());
		tokenObj.setTimeStamp(currentTime);
		tokenObj.setToken(token);
		String cookieVal = generateCookie(setCookie);
		tokenObj.setCookie(cookieVal);
		tokenObj.setTokenPresent(Boolean.TRUE);
		exchange.setProperty("token", token);
		exchange.setProperty("Cookie", cookieVal);
		logger.info("Token and Cookie received and added in exchange");
	}

	public String generateCookie(String setCookie) {
		String cookie = "";
		if (setCookie != null) {
			System.out.println("Set Cookie Value" + setCookie);
			// setCookie = setCookie.substring(1, setCookie.length() - 1);
			String userContext = "";
			String sessionId = "";
			String[] splited = setCookie.split(";");
			for (int i = 0; i < splited.length; i++) {
				System.out.println(splited[i]);
				if (splited[i].contains("sap-usercontext"))
					userContext = splited[i];
				else if (splited[i].contains("SAP_SESSIONID")) {
					sessionId = splited[i];
					sessionId = sessionId.substring(sessionId.indexOf("S"), sessionId.length()) + ";";
				}

			}
			cookie = sessionId + " " + userContext;
			logger.info("cookie =" + cookie);
			return cookie;
		} else {
			logger.info("set-cookie is not received from the SAP");
			return null;
		}
	}
}
