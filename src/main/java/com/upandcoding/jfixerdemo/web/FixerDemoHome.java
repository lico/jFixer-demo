package com.upandcoding.jfixerdemo.web;

import java.io.IOException;

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
import com.upandcoding.fixer.FixerException;
import com.upandcoding.jfixerdemo.simulator.SimulatorUtils;

@Controller
public class FixerDemoHome {

	private static final Logger log = LoggerFactory.getLogger(FixerDemoHome.class);

	private static final String LATEST = "latest";
	private static final String HISTORY = "history";

	@Value("${fixer.url}")
	String baseUrl;

	@Value("${fixer.key}")
	String defaultAccessKey;

	@Value("${fixer.currency}")
	String defaultBaseCurrency;

	@Value("${fixer.simulator.url}")
	String baseSimulatorUrl;

	@RequestMapping(value = "/jFixer", method = { RequestMethod.GET, RequestMethod.POST }) 
	@ResponseBody
	public ModelAndView endpointLatest(HttpServletRequest request,
			@RequestParam(value = "access_key", required = false) String accessKey,
			@RequestParam(value = "base", required = false) String baseCurrency,
			@RequestParam(value = "debugMode", required = false) String debug)
			throws JsonParseException, FixerException, IOException {

		WebUtils.displayParameters(request, "home");

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

		log.debug("");
		log.debug("BaseURL: {}", baseServiceUrl);
		log.debug("BaseCurrency: {}", baseCurrency);
		log.debug("AccesKey: {}", accessKey);
		log.debug("debugMode: {}", debugMode);

		ModelAndView view = new ModelAndView("views/home");
		view.addObject("access_key", accessKey);
		view.addObject("base", baseCurrency);
		view.addObject("debugMode", debugMode);
		view.addObject("endpoint", "home");

		view.addObject("pageTitle", "jFixer API Demo");

		return view;
	}

}
