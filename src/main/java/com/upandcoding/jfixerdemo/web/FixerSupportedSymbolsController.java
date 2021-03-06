package com.upandcoding.jfixerdemo.web;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
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
import com.upandcoding.fixer.endpoint.Endpoint;
import com.upandcoding.fixer.endpoint.SupportedSymbolsEndpoint;
import com.upandcoding.fixer.endpoint.field.EndpointField;
import com.upandcoding.fixer.model.Currency;
import com.upandcoding.jfixerdemo.Common;
import com.upandcoding.jfixerdemo.simulator.SimulatorUtils;

@Controller
public class FixerSupportedSymbolsController {

	private static final Logger log = LoggerFactory.getLogger(FixerSupportedSymbolsController.class);

	private static final String endpointName = "symbols";
	private static final String viewName = "views/supportedSymbolsView";

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

	@RequestMapping(value = "/jFixer/symbols", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public ModelAndView endpointHistory(
			HttpServletRequest request,
			@RequestParam(value = "access_key", required = false) String accessKey,
			@RequestParam(value = "country", required = false) String country,
			@RequestParam(value = "httpsMode", required = false) String https,
			@RequestParam(value = "debugMode", required = false) String debug) throws Exception {

		WebUtils.displayParameters(request, endpointName);

		log.debug("");
		log.debug("BEFORE CONTROLLER");
		log.debug("AccesKey: {}", accessKey);
		log.debug("country: {}", country);
		log.debug("debug: {}", debug);

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
		if (Common.DEBUG_ON.equalsIgnoreCase(https)) {
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

		ModelAndView view = new ModelAndView(viewName);
		view.addObject("access_key", accessKey);
		view.addObject("debugMode", debugMode);
		view.addObject("httpsMode", httpsMode);
		view.addObject("endpoint", endpointName);

		view.addObject("pageTitle", "jFixer - " + endpointName);

		// Locale / Country
		Locale locale = Locale.getDefault();
		if (StringUtils.isBlank(country)) {
			country = WebUtils.getCountry(locale);
		} else {
			locale = WebUtils.getLocale(country);
		}
		view.addObject("country", country);
		view.addObject("locale", locale);

		// Currencies
		FixerApiLoader loader = new FixerApiLoader(baseServiceUrl, accessKey, defaultBaseCurrency);
		try {
			List<Currency> currencies = loader.getSupportedSymbols();
			view.addObject("currencies", currencies);
			view.addObject("error", "");
		} catch (FixerException e) {
			view.addObject("error", e.getLocalizedMessage());
		}

		// Available locales
		Set<String> countries = WebUtils.getAvailableLocale();
		view.addObject("countries", countries);

		Endpoint endpoint = new SupportedSymbolsEndpoint();
		endpoint.setBaseUrl(baseServiceUrl);
		Set<EndpointField> params = new HashSet<>();
		if (StringUtils.isNotBlank(accessKey)) {
			EndpointField param1 = new EndpointField("access_key", accessKey);
			params.add(param1);
		}
		endpoint.setRequestedEndpointParameters(params);
		String url = endpoint.getRequestUrl();
		view.addObject("serviceUrl", url);
		String response = loader.getJsonResponse();
		view.addObject("WebResponse", response);

		return view;
	}
}
