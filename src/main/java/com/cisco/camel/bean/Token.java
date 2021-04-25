package com.cisco.camel.bean;

import java.sql.Timestamp;

import org.springframework.stereotype.Component;

@Component
public class Token {

	private String token;

	private Timestamp timeStamp;
	
	private String cookie;
	

	public String getCookie() {
		return cookie;
	}

	public void setCookie(String cookie) {
		this.cookie = cookie;
	}

	private boolean tokenPresent;

	public boolean isTokenPresent() {
		return tokenPresent;
	}

	public void setTokenPresent(boolean tokenPresent) {
		this.tokenPresent = tokenPresent;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public Timestamp getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(Timestamp timeStamp) {
		this.timeStamp = timeStamp;
	}
	
	


}
