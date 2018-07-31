package com.upandcoding.jfixerdemo.web;

import java.io.IOException;
import java.text.DateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebUtils {

	private static final Logger log = LoggerFactory.getLogger(WebUtils.class);

	public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	protected static final void displayParameters(HttpServletRequest request, String endpointName) {
		log.debug("*** Calling Servlet : " + endpointName);
		log.debug("Parameters:");
		Enumeration<String> paramNames = request.getParameterNames();
		while (paramNames.hasMoreElements()) {
			String param = paramNames.nextElement();
			log.debug("{} -> {}", param, request.getParameter(param));
		}
	}

	protected static final Locale getLocale(String country) {
		Locale locale = null;
		Locale[] locales = DateFormat.getAvailableLocales();
		for (Locale lcl : locales) {
			if (StringUtils.isNotBlank(lcl.getCountry())) {
				String cntry = getCountry(lcl);
				if (cntry.equalsIgnoreCase(country)) {
					locale = lcl;
					break;
				}
			}
		}
		return locale;
	}

	protected static final String getCountry(Locale locale) {
		String country = locale.getCountry() + " - " + locale.getDisplayCountry(Locale.US);
		return country;
	}

	protected static final Set<String> getAvailableLocale() {
		Set<String> countries = new TreeSet<>();

		Locale[] locales = DateFormat.getAvailableLocales();
		for (Locale locale : locales) {
			if (StringUtils.isNotBlank(locale.getCountry())) {
				String cntry = getCountry(locale);
				countries.add(cntry);
			}
		}

		return countries;
	}

	public static String getResponse(String resource) throws IOException {
		String body = "";
		try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
			HttpGet httpGet = new HttpGet(resource);
			HttpResponse resp = client.execute(httpGet);
			ResponseHandler<String> handler = new BasicResponseHandler();
			body = handler.handleResponse(resp);
		} catch (IOException e) {
			body = e.getLocalizedMessage();
		}
		return body;
	}
}
