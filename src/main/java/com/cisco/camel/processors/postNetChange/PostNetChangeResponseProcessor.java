package com.cisco.camel.processors.postNetChange;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cisco.camel.config.PropertyHolder;
import com.google.gson.Gson;

/**
 * This class performs the processing of the payload received from SAP for
 * net change service generates a final response to be sent to CCW workflow.
 * @author arnanda
 *
 */

@Component("mapNetChangeResponse")
public class PostNetChangeResponseProcessor implements Processor {
	private static final Logger logger = LoggerFactory.getLogger(PostNetChangeResponseProcessor.class);

	@Autowired
	private PropertyHolder propertyHolder;
	/**
	 * The method retrieves the response received from SAP, processes the payload
	 * received and generates a target response to be sent to CCW.
	 * 
	 * @param - Exchange object
	 *
	 */

	@Override
	public void process(Exchange exchange) throws Exception {

		logger.info("Post Net Change response payload construction begins");

		HashMap<Object, Object> responsePayload = exchange.getIn().getBody(HashMap.class);
		HashMap<Object, Object> OrderResponse = new HashMap<Object, Object>();
		HashMap<Object, Object> OrderResponseTarget = new HashMap<Object, Object>();
		HashMap<Object, Object> OrderHeader = new HashMap<Object, Object>();
		ArrayList<HashMap<Object, Object>> orderLines = new ArrayList<HashMap<Object, Object>>();
		HashMap<Object, Object> majorLine = new HashMap<Object, Object>();
		ArrayList<HashMap<Object, Object>> minorLines = new ArrayList<HashMap<Object, Object>>();
		HashMap<Object, Object> minorAndMajorLine = new HashMap<Object, Object>();
		if (null != responsePayload && responsePayload instanceof HashMap && responsePayload.containsKey("d")) {
			
			HashMap<Object, Object> orderResponse = new HashMap<Object,Object>();
			
			HashMap<Object, Object> responseBodyMap = (HashMap<Object, Object>) responsePayload.get("d");
			HashMap<Object, Object> minorLinesResult = (HashMap<Object, Object>) responseBodyMap.get("minorLines");
			ArrayList<HashMap<Object, Object>> linesDataSource = (ArrayList<HashMap<Object, Object>>) minorLinesResult.get("results");
			OrderHeader.put("remainingDaysOfBillingCycle", responseBodyMap.get("remainingDaysOfBillingCycle"));
			OrderHeader.put("nextChargeCloseDate", responseBodyMap.get("nextChargeCloseDate"));
			OrderHeader.put("currentChargeCloseDate", responseBodyMap.get("currentChargeCloseDate"));
			OrderHeader.put("totalCredits", responseBodyMap.get("totalCredits"));
			OrderHeader.put("totalProratedCharge", responseBodyMap.get("totalProratedCharge"));
			OrderHeader.put("referenceId", responseBodyMap.get("referenceId"));
		
			majorLine.put("partName", responseBodyMap.get("partName"));
			majorLine.put("ccwOrderLineId", responseBodyMap.get("ccwOrderLineId"));
			majorLine.put("subscriptionId", responseBodyMap.get("subscriptionId"));
			majorLine.put("subscriptionRefId", responseBodyMap.get("subscriptionRefId"));
			majorLine.put("totalCredits", responseBodyMap.get("totalCredits"));
			majorLine.put("totalProratedCharge", responseBodyMap.get("totalProratedCharge"));
			majorLine.put("remainingDaysOfBillingCycle", responseBodyMap.get("remainingDaysOfBillingCycle"));
			majorLine.put("nextChargeCloseDate", responseBodyMap.get("nextChargeCloseDate"));
			majorLine.put("lineId", responseBodyMap.get("lineId"));
			majorLine.put("currentChargeCloseDate", responseBodyMap.get("currentChargeCloseDate"));
			
			for (HashMap<Object, Object> minorLineSource : linesDataSource) {
				HashMap<Object, Object> minorLineTarget = new HashMap<Object, Object>();
				minorLineTarget.put("partName",minorLineSource.get("partName"));
				minorLineTarget.put("ccwOrderLineId",minorLineSource.get("ccwOrderLineId"));
				minorLineTarget.put("proratedCharge",minorLineSource.get("proratedCharge"));
				minorLineTarget.put("credit",minorLineSource.get("credit"));
				minorLineTarget.put("lineId",minorLineSource.get("lineId"));
				minorLineTarget.put("scale",minorLineSource.get("scale"));
				
				minorLines.add(minorLineTarget);
			}
			
			minorAndMajorLine.put("majorLine",majorLine);
			minorAndMajorLine.put("minorLines",minorLines);
			orderLines.add(minorAndMajorLine);
			
			OrderResponse.put("processResult","SUCCESS");
			OrderResponse.put("orderHeader", OrderHeader);
			OrderResponse.put("orderLines", orderLines);
			OrderResponseTarget.put("orderResponse", OrderResponse);
			Gson gson = new Gson();
			logger.info("Final Order Response:"+gson.toJson(OrderResponseTarget));
	
			exchange.getMessage().setBody(OrderResponseTarget);
			logger.info("Post Net Change response payload construction ends");

	   }
	}		
}
