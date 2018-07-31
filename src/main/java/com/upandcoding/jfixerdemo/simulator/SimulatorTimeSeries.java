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
import com.upandcoding.jfixerdemo.Common;

@Controller
@RequestMapping(value = "/jFixer/api", method = RequestMethod.GET, produces = { "application/json; charset=UTF-8" })
public class SimulatorTimeSeries {

	private static final Logger log = LoggerFactory.getLogger(SimulatorTimeSeries.class);

	private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	private double randomRate(double rate) {
		return rate * (1 + (10 * Math.random() - 5) / 100);
	}

	/*
	 * http://localhost:8088/jFixer/api/timeseries?access_key=API_KEY&start_date=2018-04-20&end_date=2018-04-30
	 */
	@RequestMapping(value = "/timeseries", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public String getTimeSeries(@RequestParam(value = "access_key", required = false) String accessKey,
			@RequestParam(value = "base", required = false) String baseCurrency,
			@RequestParam(value = "start_date", required = false) String startDate,
			@RequestParam(value = "end_date", required = false) String endDate,
			@RequestParam(value = "symbols", required = false) String symbols) {

		LocalDate today = LocalDate.now();
		//String day = today.format(formatter);

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

						LocalDate requestedStartDate = LocalDate.parse(startDate, formatter);
						LocalDate requestedEndDate = LocalDate.parse(endDate, formatter);
						
							//List<Currency> currencies = EndpointUtils.getSupportedCurrencies(false);
							jsonStr.append("{\"success\": true, \"timeseries\": true, \"base\":\"" + baseCurrency + "\", \"start_date\":\"" + startDate + "\", \"end_date\":\"" + endDate + "\", \"rates\":{");

							
							List<Currency> requestedCurrencies = SimulatorUtils.getEnteredCurrencies(symbols);
							Map<String, Map<String, Double>> exchangeRates = SimulatorUtils.getExchangeRates(baseCurrency, false);
							
							
							LocalDate tmpDate = requestedStartDate;
							boolean isStartDate = true;
							while (tmpDate.isBefore(requestedEndDate) || tmpDate.isEqual(requestedEndDate)) {
								if (isStartDate) {
									isStartDate = false;
								} else {
									jsonStr.append(",");
								}

								jsonStr.append("\"" + formatter.format(tmpDate) + "\": {");
								boolean isStartCur = true;
								for (Currency currency : requestedCurrencies) {
									if (isStartCur) {
										isStartCur = false;
									} else {
										jsonStr.append(",");
									}
									Map<String, Double> rates = exchangeRates.get(currency.getSymbol());
									String strDate = Common.formatter.format(tmpDate);
									jsonStr.append("\"" + currency.getSymbol() + "\":" + rates.get(strDate));
								}
								jsonStr.append("}");
								tmpDate = tmpDate.plusDays(1);
							}

							jsonStr.append("}");
							jsonStr.append("}");

					} catch (DateTimeParseException de) {
						return SimulatorErrorMessages.INVALID_TIME_FRAME;
					}
				}

				return jsonStr.toString();
			}
		}
	}
}
