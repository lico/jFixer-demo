package com.upandcoding.jfixerdemo.web;

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

import com.upandcoding.fixer.FixerApiLoader;
import com.upandcoding.fixer.FixerException;
import com.upandcoding.fixer.endpoint.ConvertEndpoint;
import com.upandcoding.fixer.endpoint.Endpoint;
import com.upandcoding.fixer.endpoint.field.EndpointField;
import com.upandcoding.fixer.model.Currency;
import com.upandcoding.fixer.model.ExchangeRate;
import com.upandcoding.jfixerdemo.Common;
import com.upandcoding.jfixerdemo.simulator.SimulatorUtils;

@Controller
public class FixerConvertController {

	private static final Logger log = LoggerFactory.getLogger(FixerConvertController.class);

	private static final String endpointName = "convert";
	private static final String viewName = "views/convertView";

	@Value("${fixer.url}")
	String baseUrl;

	@Value("${fixer.simulator.url}")
	String baseSimulatorUrl;

	@Value("${fixer.key}")
	String defaultAccessKey;

	@Value("${fixer.currency}")
	String defaultBaseCurrency;

	@RequestMapping(value = "/jFixer/convert", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public ModelAndView endpointLatest(HttpServletRequest request,
			@RequestParam(value = "access_key", required = false) String accessKey,
			@RequestParam(value = "from", required = false) String fromCurrency,
			@RequestParam(value = "to", required = false) String toCurrency,
			@RequestParam(value = "amount", required = false) Double amount,
			@RequestParam(value = "date", required = false) String date,
			@RequestParam(value = "debugMode", required = false) String debug) throws Exception {

		WebUtils.displayParameters(request,endpointName);
		
		if (amount==null) {
			amount = 1d;
		}
		
		String baseServiceUrl = baseUrl;
		boolean debugMode = false;
		if ("on".equalsIgnoreCase(debug) || SimulatorUtils.simulatorKey.equalsIgnoreCase(accessKey)) {
			debugMode = true;
			baseServiceUrl = baseSimulatorUrl;
		}

		if (StringUtils.isBlank(accessKey)) {
			accessKey = defaultAccessKey;
		}
		if (StringUtils.isBlank(fromCurrency)) {
			fromCurrency = defaultBaseCurrency;
		}
		if (StringUtils.isBlank(toCurrency)) {
			toCurrency = defaultBaseCurrency;
		}
		if (StringUtils.isBlank(date)) {
			LocalDate today = LocalDate.now();
			date = today.format(Common.formatter);
		}

		ModelAndView view = new ModelAndView(viewName);
		view.addObject("access_key", accessKey);
		view.addObject("from", fromCurrency);
		view.addObject("to", toCurrency);
		view.addObject("amount", amount);
		view.addObject("date", date);
		view.addObject("debugMode", debugMode);
		view.addObject("endpoint", endpointName);

		view.addObject("pageTitle", "jFixer - " + endpointName);

		FixerApiLoader loader = new FixerApiLoader(baseServiceUrl, accessKey, fromCurrency);

		
		try {
			double result = loader.getConversion(fromCurrency, toCurrency, amount, date);
			view.addObject("result", result);
			view.addObject("error", "");
		} catch (FixerException e) {
			log.debug("Error: {}", e.getLocalizedMessage());
			view.addObject("error", e.getLocalizedMessage());
		}

		Endpoint endpoint = new ConvertEndpoint();
		endpoint.setBaseUrl(baseServiceUrl);
		Set<EndpointField> params = new HashSet<>();
		if (StringUtils.isNotBlank(accessKey)) {
			EndpointField param = new EndpointField("access_key", accessKey);
			params.add(param);
		}
		if (StringUtils.isNotBlank(fromCurrency)) {
			EndpointField param = new EndpointField("from", fromCurrency);
			params.add(param);
		}
		if (StringUtils.isNotBlank(toCurrency)) {
			EndpointField param = new EndpointField("to", toCurrency);
			params.add(param);
		}
		EndpointField param = new EndpointField("amount", ""+amount);
		params.add(param);
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

		return view;
	}

}
