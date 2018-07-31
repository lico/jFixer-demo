package com.upandcoding.jfixerdemo.livetester;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.upandcoding.jfixerdemo.web.FixerLatestAndHistoryController;
import com.upandcoding.jfixerdemo.web.WebUtils;

@Controller
public class TestFixerIoApiController {

	private static final Logger log = LoggerFactory.getLogger(FixerLatestAndHistoryController.class);

	@Value("${fixer.url}")
	String baseUrl;

	@Value("${fixer.key}")
	String accessKey;

	@Value("${fixer.currency}")
	String baseCurrency;

	@Value("${export.dir}")
	String exportDir;

	@RequestMapping(value = "/jFixer/testNativeApi", method = { RequestMethod.GET, RequestMethod.POST })
	public void testAllUrls() {
		log.debug("");
		log.debug("*** Settings:");
		if (!baseUrl.endsWith("/")) {
			baseUrl = baseUrl + "/";
		}
		log.debug("BaseUrl: {}", baseUrl);
		log.debug("accessKey: {}", accessKey);
		log.debug("baseCurrency: {}", baseCurrency);

		log.debug("");

		int cnt = 1;
		Map<String, String> urls = new HashMap<>();
		// SUPPORTED SYMBOLS
		urls.put("TST" + cnt, baseUrl + "symbols");
		cnt++;
		urls.put("TST" + cnt, baseUrl + "symbols?access_key=" + accessKey + "");
		cnt++;
		urls.put("TST" + cnt, baseUrl + "symbols?access_key=INVALID_API_KEY");
		cnt++;

		// LATEST AND HISTORY
		String[] endpoints = { "latest", "2018-07-03" };
		for (String endpoint : endpoints) {
			urls.put("TST" + cnt, baseUrl + endpoint + "");
			cnt++;
			urls.put("TST" + cnt, baseUrl + endpoint + "?access_key=" + accessKey + "");
			cnt++;
			urls.put("TST" + cnt, baseUrl + endpoint + "?access_key=INVALID_API_KEY");
			cnt++;
			urls.put("TST" + cnt, baseUrl + endpoint + "?access_key=" + accessKey + "&base=EUR");
			cnt++;
			urls.put("TST" + cnt, baseUrl + endpoint + "?access_key=" + accessKey + "&base=TOTO");
			cnt++;
			urls.put("TST" + cnt, baseUrl + endpoint + "?access_key=" + accessKey + "&base=USD");
			cnt++;
			urls.put("TST" + cnt, baseUrl + endpoint + "?access_key=" + accessKey + "&symbols=CHF");
			cnt++;
			urls.put("TST" + cnt, baseUrl + endpoint + "?access_key=" + accessKey + "&base=EUR&symbols=EUR");
			cnt++;
			urls.put("TST" + cnt, baseUrl + endpoint + "?access_key=" + accessKey + "&base=EUR&symbols=CHF");
			cnt++;
			urls.put("TST" + cnt, baseUrl + endpoint + "?access_key=" + accessKey + "&base=EUR&symbols=JPY");
			cnt++;
			urls.put("TST" + cnt, baseUrl + endpoint + "?access_key=" + accessKey + "&base=EUR&symbols=TOTO");
			cnt++;
			urls.put("TST" + cnt, baseUrl + endpoint + "?access_key=" + accessKey + "&base=EUR&symbols=GBP,JPY,USD");
			cnt++;
			urls.put("TST" + cnt, baseUrl + endpoint + "?access_key=" + accessKey + "&base=EUR&symbols=CHF,CHF,CHF");
			cnt++;
			urls.put("TST" + cnt, baseUrl + endpoint + "?access_key=" + accessKey + "&base=EUR&symbols=CHF,CHF,USD,CHF");
		}

		// TIMESERIES AND FLUCTUATIONS
		String[] endpoints2 = { "timeseries", "fuctuation" };
		for (String endpoint : endpoints2) {
			urls.put("TST" + cnt, baseUrl + endpoint + "");
			cnt++;
			urls.put("TST" + cnt, baseUrl + endpoint + "?access_key=" + accessKey + "");
			cnt++;
			urls.put("TST" + cnt, baseUrl + endpoint + "?access_key=" + accessKey + "&base=EUR");
			cnt++;
			urls.put("TST" + cnt, baseUrl + endpoint + "?access_key=" + accessKey + "&start_date=2018-04-20");
			cnt++;
			urls.put("TST" + cnt, baseUrl + endpoint + "?access_key=" + accessKey + "&end_date=2018-04-20");
			cnt++;
			urls.put("TST" + cnt, baseUrl + endpoint + "?access_key=" + accessKey + "&start_date=2018-04-20&end_date=2018-04-30");
			cnt++;
			urls.put("TST" + cnt, baseUrl + endpoint + "?access_key=INVALID_API_KEY&start_date=2018-04-20&end_date=2018-04-30");
			cnt++;
			// Varying base
			urls.put("TST" + cnt, baseUrl + endpoint + "?access_key=" + accessKey + "&base=EUR&start_date=2018-04-20&end_date=2018-04-30");
			cnt++;
			urls.put("TST" + cnt, baseUrl + endpoint + "?access_key=" + accessKey + "&base=JPY&start_date=2018-04-20&end_date=2018-04-30");
			cnt++;
			urls.put("TST" + cnt, baseUrl + endpoint + "?access_key=" + accessKey + "&base=TOTO&start_date=2018-04-20&end_date=2018-04-30");
			cnt++;
			// Varying symbols
			urls.put("TST" + cnt, baseUrl + endpoint + "?access_key=" + accessKey + "&start_date=2018-04-20&end_date=2018-04-30&symbols=CHF");
			cnt++;
			urls.put("TST" + cnt, baseUrl + endpoint + "?access_key=" + accessKey + "&start_date=2018-04-20&end_date=2018-04-30&symbols=CHF,USD");
			cnt++;
			urls.put("TST" + cnt, baseUrl + endpoint + "?access_key=" + accessKey + "&start_date=2018-04-20&end_date=2018-04-30&symbols=CHF,CHF");
			cnt++;
			urls.put("TST" + cnt, baseUrl + endpoint + "?access_key=" + accessKey + "&start_date=2018-04-20&end_date=2018-04-30&symbols=CHF,USD,CHF");
			cnt++;
			// Erroneous dates
			urls.put("TST" + cnt, baseUrl + endpoint + "?access_key=" + accessKey + "&start_date=bla&end_date=bli");
			cnt++;
			urls.put("TST" + cnt, baseUrl + endpoint + "?access_key=" + accessKey + "&start_date=2018-04-30&end_date=2018-04-20");
			cnt++;
			urls.put("TST" + cnt, baseUrl + endpoint + "?access_key=" + accessKey + "&start_date=2018-20-20&end_date=2018-04-30");
			cnt++;
			urls.put("TST" + cnt, baseUrl + endpoint + "?access_key=" + accessKey + "&start_date=2018-04-20&end_date=2018-00-00");
			cnt++;
			urls.put("TST" + cnt, baseUrl + endpoint + "?access_key=" + accessKey + "&start_date=1815-04-20&end_date=1815-04-30");
			cnt++;
			urls.put("TST" + cnt, baseUrl + endpoint + "?access_key=" + accessKey + "&start_date=2118-04-20&end_date=2118-04-30");
			cnt++;
		}

		// CONVERT
		urls.put("TST" + cnt, baseUrl + "convert?access_key=" + accessKey);
		cnt++;
		urls.put("TST" + cnt, baseUrl + "convert?access_key=" + accessKey + "&from=EUR&to=CHF&amount=18.45");
		cnt++;
		urls.put("TST" + cnt, baseUrl + "convert?access_key=" + accessKey + "&from=CHF&to=USD&amount=18.45");
		cnt++;
		urls.put("TST" + cnt, baseUrl + "convert?access_key=" + accessKey + "&from=EUR&to=EUR&amount=18.45");
		cnt++;
		urls.put("TST" + cnt, baseUrl + "convert?access_key=" + accessKey + "&from=EUR&to=CHF,USD,JPY&amount=18.45");
		cnt++;
		urls.put("TST" + cnt, baseUrl + "convert?access_key=" + accessKey + "&from=CHF,USD,JPY&to=EUR&amount=18.45");
		cnt++;
		urls.put("TST" + cnt, baseUrl + "convert?access_key=" + accessKey + "&from=ERR1&to=EUR&amount=18.45");
		cnt++;
		urls.put("TST" + cnt, baseUrl + "convert?access_key=" + accessKey + "&from=EUR&to=ERR2&amount=18.45");
		cnt++;
		urls.put("TST" + cnt, baseUrl + "convert?access_key=" + accessKey + "&from=EUR&to=CHF&amount=-20");
		cnt++;
		urls.put("TST" + cnt, baseUrl + "convert?access_key=" + accessKey + "&from=EUR&to=CHF&amount=");
		cnt++;
		urls.put("TST" + cnt, baseUrl + "convert?access_key=" + accessKey + "&from=EUR&to=CHF&amount=1");
		cnt++;

		Set<String> keys = urls.keySet();
		for (String key : keys) {
			String fileName = exportDir + key + ".txt";
			String url = urls.get(key);
			try {
				log.debug("------------------------------------");
				log.debug("url: {}", url);
					String response = WebUtils.getResponse(url); 
					log.debug(response);

					FileUtils.writeStringToFile(new File(fileName), "URL:\n" + url + "\n\nRESPONSE:\n" + response, Charset.defaultCharset());

			} catch (Exception e) {
				log.debug(e.getLocalizedMessage());
				try {
				FileUtils.writeStringToFile(new File(fileName), "URL:\n" + url + "\n\nRESPONSE:\n" + e.getLocalizedMessage(), Charset.defaultCharset());
				} catch (IOException ie) {
					log.debug(ie.getLocalizedMessage());
				}
			}
		}
	}
	
	@RequestMapping(value = "/testFixerApi", method = { RequestMethod.GET, RequestMethod.POST })
	public void testFixerApi() {

	}
}
