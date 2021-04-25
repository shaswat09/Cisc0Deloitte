package com.cisco.camel.processors.postNetChange;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cisco.camel.bean.Trace;
import com.cisco.camel.constants.ApplicationConstants;
import com.cisco.camel.proxy.RestServicesProxy;
import com.google.gson.Gson;

import brave.Tracer;

/**
 * This class performs the processing of the input payload to generate the
 * target payload which will be used as a source to SAP service
 * 
 * @author arnanda
 *
 */

@Component("mapNetChangeRequest")
public class PostNetChangeRequestProcessor implements Processor {

	@Autowired
	private RestServicesProxy restServicesProxy;

	@Autowired
	Tracer tracer;

	private static final Logger logger = LoggerFactory.getLogger(PostNetChangeRequestProcessor.class);

	/**
	 * This method Performs the processing of the input message and generates an
	 * output message.
	 * 
	 * @param - Exchange interface of Camel
	 * @return - void
	 * @exception - throws java.lang.Exception
	 */

	@Override
	public void process(Exchange exchange) throws Exception {
		Trace trace = new Trace();
		trace.setTraceId(tracer.currentSpan().context().traceIdString());
		trace.setSpanId(tracer.currentSpan().context().spanIdString());
		exchange.setProperty("trace", trace);
		HashMap<Object, Object> netChangeInputPayload = exchange.getIn().getBody(HashMap.class);

		HashMap<Object, Object> netChangeDataTarget = new HashMap<Object, Object>();
		ArrayList<HashMap<Object, Object>> minorLinesTarget = new ArrayList<HashMap<Object, Object>>();
		HashMap<Object, Object> netChangeRequestPayload = (HashMap<Object, Object>) netChangeInputPayload
				.get("orderRequest");
		HashMap<Object, Object> headerDataSource = (HashMap<Object, Object>) netChangeRequestPayload.get("orderHeader");
		ArrayList<HashMap<Object, Object>> linesDataSource = (ArrayList<HashMap<Object, Object>>) netChangeRequestPayload
				.get("orderLines");
		for (HashMap<Object, Object> lineDataSource : linesDataSource) {
			HashMap<Object, Object> majorLine = (HashMap<Object, Object>) lineDataSource.get("majorLine");
			if (null != majorLine && majorLine instanceof HashMap) {
				netChangeDataTarget.put("subscriptionId",(majorLine.containsKey("subscriptionId"))? majorLine.get("subscriptionId"):"");
				netChangeDataTarget.put("subscriptionRefId",(majorLine.containsKey("subscriptionRefId"))? majorLine.get("subscriptionRefId"):"");
				netChangeDataTarget.put("lineId",(majorLine.containsKey("lineId"))? majorLine.get("lineId"):"");
				netChangeDataTarget.put("ccwOrderLineId",(majorLine.containsKey("ccwOrderLineId"))? majorLine.get("ccwOrderLineId"):"");
				netChangeDataTarget.put("partName",(majorLine.containsKey("partName"))? majorLine.get("partName"):"");
				if (headerDataSource.get("transactionId") != null) {
					exchange.setProperty("transactionId",
							"TransactionId:" + headerDataSource.get("transactionId").toString());
				}
				netChangeDataTarget.put("transactionId", (majorLine.containsKey("transactionId"))? majorLine.get("transactionId"):"");
				netChangeDataTarget.put("actionType", (majorLine.containsKey("actionType"))? majorLine.get("actionType"):"");
				netChangeDataTarget.put("initialTerm", (majorLine.containsKey("initialTerm"))? majorLine.get("initialTerm"):"");
				netChangeDataTarget.put("renewalTerm", (majorLine.containsKey("renewalTerm"))? majorLine.get("renewalTerm"):"");
				netChangeDataTarget.put("requestedStartDate", majorLine.get("requestedStartDate")+"T00:00:00.000");
				netChangeDataTarget.put("billingModel", (majorLine.containsKey("billingModel"))? majorLine.get("billingModel"):"");
				netChangeDataTarget.put("netPriceChangeFlag", (majorLine.containsKey("netPriceChangeFlag"))? majorLine.get("netPriceChangeFlag"):"");

			}
			ArrayList<HashMap<Object, Object>> minorLines = (ArrayList<HashMap<Object, Object>>) lineDataSource
					.get("minorLines");

			for (HashMap<Object, Object> minorLineSource : minorLines) {
				HashMap<Object, Object> minorLine = new HashMap<Object, Object>();
				
				minorLine.put("lineId", (minorLineSource.containsKey("lineId"))? minorLineSource.get("lineId"):"");
				
				minorLine.put("partName",(minorLineSource.containsKey("partName"))? minorLineSource.get("partName"):"");
				minorLine.put("ccwOrderLineId", (minorLineSource.containsKey("ccwOrderLineId"))? minorLineSource.get("ccwOrderLineId"):"");
				minorLine.put("unitNetPrice", minorLineSourceUnitNetPrice(minorLineSource.get("pricingInfo"),minorLineSource.get("actionType").toString()));
				minorLine.put("subscriptionCredit",
						minorLineSourceSubscriptionCredit(minorLineSource.get("pricingInfo"),minorLineSource.get("actionType").toString()));

				minorLine.put("decimalExpansion", (minorLineSource.containsKey("decimalExpansion"))? minorLineSource.get("decimalExpansion").toString():"");
				minorLine.put("actionType", (minorLineSource.containsKey("actionType"))? minorLineSource.get("actionType"):"");
				minorLine.put("quantity", (minorLineSource.containsKey("quantity"))? minorLineSource.get("quantity").toString():"");
				minorLine.put("netPriceChangeFlag", (minorLineSource.containsKey("netPriceChangeFlag"))? minorLineSource.get("netPriceChangeFlag"):"");
				minorLine.put("chargeType", (minorLineSource.containsKey("chargeType"))? minorLineSource.get("chargeType"):"");
				minorLine.put("pricingTerm", (minorLineSource.containsKey("pricingTerm"))? minorLineSource.get("pricingTerm").toString():"");

				minorLinesTarget.add(minorLine);

			}

			netChangeDataTarget.put("minorLines", minorLinesTarget);

		}

		Gson gson = new Gson();

		logger.info("OData request structure acceptable by SAP BRIM:" + gson.toJson(netChangeDataTarget));

		exchange.getMessage().setBody(netChangeDataTarget);

	}

	private String minorLineSourceUnitNetPrice(Object pricingInfo, String b) {
		
		HashMap<Object, Object> pricingInfoMap = (HashMap<Object, Object>) pricingInfo;
		return (pricingInfoMap.get("unitNetPrice") instanceof String? pricingInfoMap.get("unitNetPrice").toString():"");

	}

	private String minorLineSourceSubscriptionCredit(Object pricingInfo, String b) {
		HashMap<Object, Object> pricingInfoMap = (HashMap<Object, Object>) pricingInfo;
		return (pricingInfoMap.get("subscriptionCredit") instanceof String? pricingInfoMap.get("subscriptionCredit").toString():"");

	}
}
