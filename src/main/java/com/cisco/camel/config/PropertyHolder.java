package com.cisco.camel.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * This class is used to read property file values
 * @author arnanda
 *
 */
@Configuration
public class PropertyHolder {
	public static final Logger Logger = LoggerFactory.getLogger(PropertyHolder.class);
	@Value("${netChange.service.endpoint}")
	private String serviceEndpoint;
	
	@Value("${netChange.service.error.code}")
	private String code;

	@Value("${netChange.service.error.status.data-failure}")
	private String dataFailure;

	@Value("${netChange.service.error.status.system-failure}")
	private String systemFailure;
	
	@Value("${netChange.service.success.code}") 
	private String successCode;
	
    @Value("${netChange.service.success.status}") 
    private String successStatus;
    
    @Value("netChange.service.error.data-failure.code}")
    private String dataFailureCode;
    
    @Value("${netChange.service.token-required}")
	private String tokenRequired;

	@Value("${netChange.service.token-not-required}")
	private String tokenNotRequired;

	@Value("${netChange.service.token.expiry-milliseconds}")
	private long tokenExpiry;
	
	@Value("${netChange.service.token.endpoint}")
	private String tokenEndpoint;
	
	@Value("${sap.brim.clear.session.path}")
	private String brimClearSessionURL;
	
	
	@Value("${netChange.service.source.system}")
	private String sourceSystem;

	@Value("${netChange.service.target.system}")
	private String targetSystem;

	@Value("${netChange.service.bslAPIName}")
	private String bslAPIName;

	@Value("${netChange.service.businessObject}")
	private String businessObject;

	@Value("${netChange.service.env}")
	private String env;
	
	@Value("${sap.brim.authorization}")
	private String brimAuthorization;
	
	
	public String getBrimClearSessionURL() {
		return brimClearSessionURL;
	}

	public void setBrimClearSessionURL(String brimClearSessionURL) {
		this.brimClearSessionURL = brimClearSessionURL;
	}

	public String getBrimAuthorization() {
		return brimAuthorization;
	}

	public void setBrimAuthorization(String brimAuthorization) {
		this.brimAuthorization = brimAuthorization;
	}

	public String getSourceSystem() {
		return sourceSystem;
	}

	public void setSourceSystem(String sourceSystem) {
		this.sourceSystem = sourceSystem;
	}

	public String getTargetSystem() {
		return targetSystem;
	}

	public void setTargetSystem(String targetSystem) {
		this.targetSystem = targetSystem;
	}

	public String getBslAPIName() {
		return bslAPIName;
	}

	public void setBslAPIName(String bslAPIName) {
		this.bslAPIName = bslAPIName;
	}

	public String getBusinessObject() {
		return businessObject;
	}

	public void setBusinessObject(String businessObject) {
		this.businessObject = businessObject;
	}

	public String getEnv() {
		return env;
	}

	public void setEnv(String env) {
		this.env = env;
	}

	


	public String getTokenEndpoint() {
		return tokenEndpoint;
	}

	public void setTokenEndpoint(String tokenEndpoint) {
		this.tokenEndpoint = tokenEndpoint;
	}

	public String getTokenRequired() {
		return tokenRequired;
	}

	public void setTokenRequired(String tokenRequired) {
		this.tokenRequired = tokenRequired;
	}

	public String getTokenNotRequired() {
		return tokenNotRequired;
	}

	public void setTokenNotRequired(String tokenNotRequired) {
		this.tokenNotRequired = tokenNotRequired;
	}

	public long getTokenExpiry() {
		return tokenExpiry;
	}

	public void setTokenExpiry(long tokenExpiry) {
		this.tokenExpiry = tokenExpiry;
	}

	public String getDataFailureCode() {
		return dataFailureCode;
	}

	public void setDataFailureCode(String dataFailureCode) {
		this.dataFailureCode = dataFailureCode;
	}

	public String getServiceEndpoint() {
		
		Logger.info("serviceEndpoint -> "+serviceEndpoint);
		return serviceEndpoint;
	}

	public void setServiceEndpoint(String serviceEndpoint) {
		this.serviceEndpoint = serviceEndpoint;
	}

	
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getDataFailure() {
		return dataFailure;
	}

	public void setDataFailure(String dataFailure) {
		this.dataFailure = dataFailure;
	}

	public String getSystemFailure() {
		return systemFailure;
	}

	public void setSystemFailure(String systemFailure) {
		this.systemFailure = systemFailure;
	}

	public String getSuccessCode() {
		return successCode;
	}

	public void setSuccessCode(String successCode) {
		this.successCode = successCode;
	}

	public String getSuccessStatus() {
		return successStatus;
	}

	public void setSuccessStatus(String successStatus) {
		this.successStatus = successStatus;
	}

}