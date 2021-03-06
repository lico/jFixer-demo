package com.upandcoding.jfixerdemo.web;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.core.JsonParseException;
import com.upandcoding.fixer.FixerApiLoader;
import com.upandcoding.fixer.FixerException;
import com.upandcoding.fixer.endpoint.Endpoint;
import com.upandcoding.fixer.endpoint.HistoricalEndpoint;
import com.upandcoding.fixer.endpoint.LatestEndpoint;
import com.upandcoding.fixer.endpoint.field.EndpointField;
import com.upandcoding.fixer.model.Currency;
import com.upandcoding.fixer.model.ExchangeRate;
import com.upandcoding.jfixerdemo.Common;
import com.upandcoding.jfixerdemo.simulator.SimulatorUtils;

@Controller
public class FixerLatestAndHistoryController {

	private static final Logger log = LoggerFactory.getLogger(FixerLatestAndHistoryController.class);

	@Value("${fixer.url}")
	String baseUrl;
	
	@Value("${fixer.url.https}")
	String baseUrlHttps;

	@Value("${fixer.simulator.url}")
	String baseSimulatorUrl;

	@Value("${fixer.key}")
	String defaultAccessKey;

	@Value("${fixer.currency}")
	String defaultBaseCurrency;

	/**
	 * 
	 * http://localhost:8088/history/2018-07-24?access_key=ABC&base=EUR
	 * 
	 */
	@RequestMapping(value = "/jFixer/history", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public ModelAndView endpointHistory(HttpServletRequest request,
			@RequestParam(value = "access_key", required = false) String accessKey,
			@RequestParam(value = "date", required = false) String date,
			@RequestParam(value = "base", required = false) String baseCurrency,
			@RequestParam(value = "symbols", required = false) String symbols,
			@RequestParam(value = "httpsMode", required = false) String https,
			@RequestParam(value = "debugMode", required = false) String debug) throws Exception {

		return endpointLatestOrHistory(request, Common.HISTORY, "views/historyView", accessKey, baseCurrency, symbols, date,
				debug, https);
	}

	@RequestMapping(value = "/jFixer/latest", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public ModelAndView endpointLatest(HttpServletRequest request,
			@RequestParam(value = "access_key", required = false) String accessKey,
			@RequestParam(value = "base", required = false) String baseCurrency,
			@RequestParam(value = "symbols", required = false) String symbols,
			@RequestParam(value = "httpsMode", required = false) String https,
			@RequestParam(value = "debugMode", required = false) String debug)
			throws JsonParseException, FixerException, IOException {

		return endpointLatestOrHistory(request, Common.LATEST, "views/latestView", accessKey, baseCurrency, symbols, null,
				debug, https);
	}

	public ModelAndView endpointLatestOrHistory(HttpServletRequest request, String endpointName, String viewName,
			String accessKey, String baseCurrency, String symbols, String date, String debug, String https)
			throws JsonParseException, FixerException, IOException {

		WebUtils.displayParameters(request, endpointName);

		// Check Access Key
		if (StringUtils.isBlank(accessKey)) {
			accessKey = defaultAccessKey;
		}
		if (SimulatorUtils.simulatorKey.equalsIgnoreCase(accessKey) || SimulatorUtils.invalidKey.equalsIgnoreCase(accessKey)) {
			debug = Common.DEBUG_ON;
		}

		// Check Base URL (https)
		String fixerUrl = baseUrl;
		boolean httpsMode = false;
		if (Common.HTTPS_ON.equalsIgnoreCase(https)) {
			fixerUrl = baseUrlHttps;
			httpsMode=true;
		}
		
		// Check Debug
		String baseServiceUrl = fixerUrl;
		boolean debugMode = false;
		if (Common.DEBUG_ON.equalsIgnoreCase(debug) || SimulatorUtils.simulatorKey.equalsIgnoreCase(accessKey)) {
			debugMode = true;
			baseServiceUrl = baseSimulatorUrl;
		}

		if (StringUtils.isBlank(baseCurrency) || "_DEFAULT".equalsIgnoreCase(baseCurrency)) {
			baseCurrency = defaultBaseCurrency;
		}
		if (StringUtils.isBlank(symbols)) {
			symbols = "";
		}
		if (StringUtils.isBlank(date)) {
			LocalDate today = LocalDate.now();
			date = WebUtils.formatter.format(today);
		}

		ModelAndView view = new ModelAndView(viewName);
		view.addObject("access_key", accessKey);
		view.addObject("base", baseCurrency);
		view.addObject("symbols", symbols);
		view.addObject("date", date);
		view.addObject("debugMode", debugMode);
		view.addObject("httpsMode", httpsMode);
		view.addObject("endpoint", endpointName);

		view.addObject("pageTitle", "jFixer - " + endpointName);

		// Exchange Rates
		List<ExchangeRate> rates = null;
		FixerApiLoader loader = new FixerApiLoader(baseServiceUrl, accessKey, baseCurrency);

		try {
			if (Common.LATEST.equalsIgnoreCase(endpointName)) {
				if (StringUtils.isBlank(symbols)) {
					rates = loader.getLatest();
				} else {
					rates = loader.getLatest(symbols);
				}
			} else if (Common.HISTORY.equalsIgnoreCase(endpointName)) {
				rates = loader.getHistorical(date, symbols);
			}
			log.debug("EndpointName: {} equals history? {}", endpointName, Common.HISTORY.equalsIgnoreCase(endpointName));
			log.debug("Service URL: {}", loader.getLastCalledUrl());
			view.addObject("rates", rates);
			view.addObject("error", "");
		} catch (FixerException e) {
			log.debug("Error: {}", e.getLocalizedMessage());
			view.addObject("error", e.getLocalizedMessage());
		}

		if (rates != null && !rates.isEmpty()) {
			ExchangeRate rate = rates.get(0);
			log.debug("Timestamp: {}", rate.getTimestamp());
		}

		Endpoint endpoint = null;
		Set<EndpointField> params = new HashSet<>();
		if (StringUtils.isNotBlank(accessKey)) {
			EndpointField param1 = new EndpointField("access_key", accessKey);
			params.add(param1);
		}
		if (StringUtils.isNotBlank(baseCurrency)) {
			EndpointField param2 = new EndpointField("base", baseCurrency);
			params.add(param2);
		}
		if (StringUtils.isNotBlank(symbols)) {
			EndpointField param3 = new EndpointField("symbols", symbols);
			params.add(param3);
		}
		if (Common.LATEST.equalsIgnoreCase(endpointName)) {
			endpoint = new LatestEndpoint();
		} else {
			endpoint = new HistoricalEndpoint();
			if (StringUtils.isNotBlank(date)) {
				EndpointField param4 = new EndpointField("date", date);
				param4.setInUrlParameter(false);
				params.add(param4);
			}

		}
		endpoint.setBaseUrl(baseServiceUrl);
		endpoint.setRequestedEndpointParameters(params);
		String url = endpoint.getRequestUrl();
		view.addObject("serviceUrl", url);
		String response = loader.getJsonResponse();
		view.addObject("WebResponse", response);

		// Supported Currencies
		List<Currency> currencies = new ArrayList<>();
		try {
			currencies = loader.getSupportedSymbols();
		} catch (FixerException e) {
			Currency currency = new Currency("EUR", "Euro");
			currencies.add(currency);
			log.debug("Unable to retrieve currencies: {}", e.getLocalizedMessage());
		}
		view.addObject("currencies", currencies);
		view.addObject("nbCurrencies", currencies.size());
		log.debug("Found {} currencies", currencies.size());

		log.debug("");
		log.debug("BaseURL: {}", baseServiceUrl);
		log.debug("serviceUrl: {}", url);
		log.debug("BaseCurrency: {}", baseCurrency);
		log.debug("AccesKey: {}", accessKey);
		log.debug("Symbols: {}", symbols);
		log.debug("Date: {}", date);
		log.debug("debugMode: {}", debugMode);

		return view;
	}

}
