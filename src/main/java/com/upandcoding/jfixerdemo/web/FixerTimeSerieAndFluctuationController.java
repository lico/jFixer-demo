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
import com.upandcoding.fixer.endpoint.FluctuationEndpoint;
import com.upandcoding.fixer.endpoint.TimeSeriesEndpoint;
import com.upandcoding.fixer.endpoint.field.EndpointField;
import com.upandcoding.fixer.model.Currency;
import com.upandcoding.fixer.model.ExchangeRate;
import com.upandcoding.fixer.model.Fluctuation;
import com.upandcoding.jfixerdemo.simulator.SimulatorUtils;

@Controller
public class FixerTimeSerieAndFluctuationController {

	private static final Logger log = LoggerFactory.getLogger(FixerTimeSerieAndFluctuationController.class);

	private static final String TIME_SERIES = "timeSeries";
	private static final String FLUCTUATION = "fluctuations";

	@Value("${fixer.url}")
	String baseUrl;

	@Value("${fixer.simulator.url}")
	String baseSimulatorUrl;

	@Value("${fixer.key}")
	String defaultAccessKey;

	@Value("${fixer.currency}")
	String defaultBaseCurrency;

	@RequestMapping(value = "/jFixer/fluctuations", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public ModelAndView endpointFuctuation(HttpServletRequest request,
			@RequestParam(value = "start_date", required = false) String startDate,
			@RequestParam(value = "end_date", required = false) String endDate,
			@RequestParam(value = "access_key", required = false) String accessKey,
			@RequestParam(value = "base", required = false) String baseCurrency,
			@RequestParam(value = "symbols", required = false) String symbols,
			@RequestParam(value = "debugMode", required = false) String debug) throws Exception {

		return endpointTimeSeriesOrFluctuation(request, FLUCTUATION, "views/fluctuationsView", accessKey, baseCurrency,
				symbols, startDate, endDate, debug);
	}

	@RequestMapping(value = "/jFixer/timeSeries", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public ModelAndView endpointTimeSeries(HttpServletRequest request,
			@RequestParam(value = "start_date", required = false) String startDate,
			@RequestParam(value = "end_date", required = false) String endDate,
			@RequestParam(value = "access_key", required = false) String accessKey,
			@RequestParam(value = "base", required = false) String baseCurrency,
			@RequestParam(value = "symbols", required = false) String symbols,
			@RequestParam(value = "debugMode", required = false) String debug)
			throws JsonParseException, FixerException, IOException {

		return endpointTimeSeriesOrFluctuation(request, TIME_SERIES, "views/timeSeriesView", accessKey, baseCurrency,
				symbols, startDate, endDate, debug);
	}

	public ModelAndView endpointTimeSeriesOrFluctuation(HttpServletRequest request, String endpointName,
			String viewName, String accessKey, String baseCurrency, String symbols, String startDate, String endDate,
			String debug) throws JsonParseException, FixerException, IOException {

		WebUtils.displayParameters(request,endpointName);

		String baseServiceUrl = baseUrl;
		boolean debugMode = false;
		if ("on".equalsIgnoreCase(debug) || SimulatorUtils.simulatorKey.equalsIgnoreCase(accessKey)) {
			debugMode = true;
			baseServiceUrl = baseSimulatorUrl;
		}
		if (StringUtils.isBlank(accessKey)) {
			accessKey = defaultAccessKey;
		}
		if (StringUtils.isBlank(baseCurrency) || "_DEFAULT".equalsIgnoreCase(baseCurrency)) {
			baseCurrency = defaultBaseCurrency;
		}
		if (StringUtils.isBlank(symbols)) {
			symbols = "";
		}
		if (StringUtils.isBlank(startDate)) {
			LocalDate today = LocalDate.now();
			startDate = WebUtils.formatter.format(today);
		}
		if (StringUtils.isBlank(endDate)) {
			LocalDate today = LocalDate.now();
			endDate = WebUtils.formatter.format(today);
		}

		log.debug("");
		log.debug("BaseURL: {}", baseUrl);
		log.debug("BaseCurrency: {}", baseCurrency);
		log.debug("AccesKey: {}", accessKey);
		log.debug("Symbols: {}", symbols);
		log.debug("StartDate: {}", startDate);
		log.debug("EndDate: {}", endDate);
		log.debug("debugMode: {}", debugMode);

		ModelAndView view = new ModelAndView(viewName);
		view.addObject("access_key", accessKey);
		view.addObject("base", baseCurrency);
		view.addObject("symbols", symbols);
		view.addObject("start_date", startDate);
		view.addObject("end_date", endDate);
		view.addObject("debugMode", debugMode);
		view.addObject("endpoint", endpointName);

		view.addObject("pageTitle", "jFixer - " + endpointName);

		// Exchange Rates
		List<ExchangeRate> rates = null;
		List<Fluctuation> fluctuations = null;
		FixerApiLoader loader = new FixerApiLoader(baseServiceUrl, accessKey, baseCurrency);

		try {
			if (TIME_SERIES.equalsIgnoreCase(endpointName)) {
				rates = loader.getTimeSeries(startDate, endDate, symbols, baseCurrency);
				view.addObject("rates", rates);
			} else if (FLUCTUATION.equalsIgnoreCase(endpointName)) {
				fluctuations = loader.getFluctuations(startDate, endDate, symbols);
				view.addObject("fluctuations", fluctuations);
			}
			view.addObject("error", "");
		} catch (FixerException e) {
			log.debug("Error: {}", e.getLocalizedMessage());
			view.addObject("error", e.getLocalizedMessage());
		}

		Endpoint endpoint = null;
		if (TIME_SERIES.equalsIgnoreCase(endpointName)) {
			endpoint = new TimeSeriesEndpoint();
		} else if (FLUCTUATION.equalsIgnoreCase(endpointName)) {
			endpoint = new FluctuationEndpoint();
		}
		endpoint.setBaseUrl(baseServiceUrl);
		Set<EndpointField> params = new HashSet<>();
		if (StringUtils.isNotBlank(accessKey)) {
			EndpointField param = new EndpointField("access_key", accessKey);
			params.add(param);
		}
		if (StringUtils.isNotBlank(baseCurrency)) {
			EndpointField param = new EndpointField("base", baseCurrency);
			params.add(param);
		}
		if (StringUtils.isNotBlank(startDate)) {
			EndpointField param = new EndpointField("start_date", startDate);
			params.add(param);
		}
		if (StringUtils.isNotBlank(startDate)) {
			EndpointField param = new EndpointField("end_date", endDate);
			params.add(param);
		}
		if (StringUtils.isNotBlank(symbols)) {
			EndpointField param = new EndpointField("symbols", symbols);
			params.add(param);
		}
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
		log.debug("Date: {}", startDate);
		log.debug("debugMode: {}", debugMode);

		return view;
	}

}
