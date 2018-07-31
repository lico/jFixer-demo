package com.upandcoding.jfixerdemo.simulator;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.upandcoding.fixer.model.Currency;
import com.upandcoding.jfixerdemo.Common;

@Controller
@RequestMapping(value = "/jFixer/api", method = RequestMethod.GET, produces = { "application/json; charset=UTF-8" })
public class SimulatorLatest {

	private static final Logger log = LoggerFactory.getLogger(SimulatorLatest.class);

	/*
	 * Latest Endpoint
	 */
	@RequestMapping(value = "/latest", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public String update(@RequestParam(value = "access_key", required = false) String accessKey,
			@RequestParam(value = "base", required = false) String baseCurrency,
			@RequestParam(value = "symbols", required = false) String symbols) {

		return getLatestJson(null, accessKey, baseCurrency, symbols, Common.LATEST);
	}

	/*
	 * Historical Endpoint
	 */
	@RequestMapping(value = "/{day}", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public String displayExchangeRate(@PathVariable String day,
			@RequestParam(value = "access_key", required = false) String accessKey,
			@RequestParam(value = "base", required = false) String baseCurrency,
			@RequestParam(value = "symbols", required = false) String symbols) throws Exception {
		return getLatestJson(day, accessKey, baseCurrency, symbols, Common.HISTORY);
	}

	/*
	 * Latest or Historical endpoint implementation
	 */
	private String getLatestJson(String day, String accessKey, String baseCurrency, String symbols, String endpointName) {

		StringBuffer jsonStr = new StringBuffer();
		jsonStr = SimulatorUtils.getBasicVerifications(accessKey, baseCurrency);
		if (StringUtils.isNotBlank(jsonStr.toString())) {
			return jsonStr.toString();

		} else {
			jsonStr = SimulatorUtils.getSymbolsVerifications(symbols);
			if (StringUtils.isNotBlank(jsonStr.toString())) {
				return jsonStr.toString();
			} else {

				String endpointFld = "";
				if (Common.LATEST.equalsIgnoreCase(endpointName)) {
					LocalDate today = LocalDate.now();
					day = today.format(Common.formatter);

				} else if (Common.HISTORY.equalsIgnoreCase(endpointName)) {
					jsonStr = SimulatorUtils.getHistoryDateVerifications(day);
					if (StringUtils.isNotBlank(jsonStr.toString())) {
						return jsonStr.toString();
					}
					endpointFld = "\"historical\": true,";
				}

				LocalDateTime now = LocalDateTime.now();

				LocalDate requestedDay = LocalDate.parse(day, Common.formatter);

				List<Currency> currencies = SimulatorUtils.getEnteredCurrencies(symbols);
				Map<String, Map<String, Double>> exchangeRates = SimulatorUtils.getExchangeRates(baseCurrency, false);

				jsonStr.append("{\"success\": true,");
				jsonStr.append(endpointFld);
				jsonStr.append("\"timestamp\": " + now.atZone(ZoneOffset.ofTotalSeconds(0)).toEpochSecond() + ", ");
				jsonStr.append("\"base\":\"" + baseCurrency + "\", ");
				jsonStr.append("\"date\":\"" + requestedDay + "\", ");
				jsonStr.append("\"rates\": {");
				boolean start = true;
				for (Currency currency : currencies) {
					if (!start) {
						jsonStr.append(", ");
					} else {
						start = false;
					}
					Map<String, Double> rates = exchangeRates.get(currency.getSymbol());
					if (rates.containsKey(day)) {
						jsonStr.append("\"" + currency.getSymbol() + "\":" + rates.get(day));
					} else {
						return SimulatorErrorMessages.INVALID_DATE;
					}
				}
				jsonStr.append("}");
				jsonStr.append("}");

			}
		}
		return jsonStr.toString();
	}
}
