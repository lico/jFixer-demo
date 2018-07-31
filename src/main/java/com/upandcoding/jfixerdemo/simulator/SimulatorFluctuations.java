package com.upandcoding.jfixerdemo.simulator;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.upandcoding.fixer.model.Currency;

@Controller
@RequestMapping(value = "/jFixer/api", method = RequestMethod.GET, produces = { "application/json; charset=UTF-8" })
public class SimulatorFluctuations {

	private static final Logger log = LoggerFactory.getLogger(SimulatorFluctuations.class);

	private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	@RequestMapping(value = "/fluctuation", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public String getFluctuations(@RequestParam(value = "access_key", required = false) String accessKey,
			@RequestParam(value = "base", required = false) String baseCurrency,
			@RequestParam(value = "start_date", required = false) String startDate,
			@RequestParam(value = "end_date", required = false) String endDate,
			@RequestParam(value = "symbols", required = false) String symbols) {

		StringBuffer jsonStr = new StringBuffer();
		jsonStr = SimulatorUtils.getBasicVerifications(accessKey, baseCurrency);
		if (StringUtils.isNotBlank(jsonStr)) {
			return jsonStr.toString();
		} else {

			jsonStr = SimulatorUtils.getSymbolsVerifications(symbols);
			if (StringUtils.isNotBlank(jsonStr)) {
				return jsonStr.toString();
			} else {

				jsonStr = SimulatorUtils.getTimeSeriesDateVerifications(startDate, endDate);
				if (StringUtils.isNotBlank(jsonStr.toString())) {
					return jsonStr.toString();
				} else {

					try {

						jsonStr.append("{\"success\": true, \"fluctuation\": true, \"base\":\"" + baseCurrency + "\", \"start_date\":\"" + startDate + "\", \"end_date\":\"" + endDate + "\", \"rates\":{");
						
						List<Currency> requestedCurrencies = SimulatorUtils.getEnteredCurrencies(symbols);
						boolean dispComma = false;
						for (Currency currency : requestedCurrencies) {
							if (dispComma) {
								jsonStr.append(",");
							} else {
								dispComma = true;
							}
							jsonStr.append("\"" + currency.getSymbol() + "\":{");
							jsonStr.append("\"start_rate\":" + SimulatorUtils.getRateAtDate(baseCurrency, currency.getSymbol(), startDate) + ",");
							jsonStr.append("\"end_rate\":" + SimulatorUtils.getRateAtDate(baseCurrency, currency.getSymbol(), endDate) + ",");
							jsonStr.append("\"change\":" + SimulatorUtils.getFluctuation(baseCurrency, currency.getSymbol(), startDate, endDate, false) + ",");
							jsonStr.append("\"change_pct\":" + SimulatorUtils.getFluctuation(baseCurrency, currency.getSymbol(), startDate, endDate, true) + "");
							jsonStr.append("}");
						}
						jsonStr.append("}");
						jsonStr.append("}");

					} catch (DateTimeParseException de) {
						return SimulatorErrorMessages.INVALID_TIME_FRAME;
					}
				}
			}
		}

		return jsonStr.toString();
	}
}
