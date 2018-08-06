package com.upandcoding.jfixerdemo.simulator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.upandcoding.jfixerdemo.Common;

@Controller
@RequestMapping(value={"/jFixer/api", "/jFixer/api2"}, method = RequestMethod.GET, produces = { "application/json; charset=UTF-8" })
public class SimulatorConversion {

	private static final Logger log = LoggerFactory.getLogger(SimulatorConversion.class);

	@RequestMapping(value = "/convert", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public String update(@RequestParam(value = "access_key", required = false) String accessKey,
			@RequestParam(value = "date", required = false) String date,
			@RequestParam(value = "from", required = false) String fromCurrency,
			@RequestParam(value = "to", required = false) String targetCurrency,
			@RequestParam(value = "amount", required = false) Double amount) {

		StringBuffer jsonStr = new StringBuffer();

		jsonStr = SimulatorUtils.getBasicVerifications(accessKey, "EUR");
		if (StringUtils.isNotBlank(jsonStr)) {
			return jsonStr.toString();
		} else {
			jsonStr = SimulatorUtils.getHistoryDateVerifications(date);
			if (StringUtils.isNotBlank(jsonStr)) {
				return jsonStr.toString();
			} else {

				jsonStr = SimulatorUtils.getConvertCurrencyVerifications(fromCurrency, true);
				if (StringUtils.isNotBlank(jsonStr)) {
					return jsonStr.toString();
				} else {
					jsonStr = SimulatorUtils.getConvertCurrencyVerifications(fromCurrency, false);
					if (StringUtils.isNotBlank(jsonStr)) {
						return jsonStr.toString();
					} else {

						if (amount == null) {
							jsonStr.append(SimulatorErrorMessages.CONVERT_INVALID_AMOUNT);
						} else {
							try {

								LocalDate requestedDay = LocalDate.now();
								if (StringUtils.isNotBlank(date)) {
									requestedDay = LocalDate.parse(date, Common.formatter);
								}
								String day = requestedDay.format(Common.formatter);
								
								double rate = SimulatorUtils.getRateAtDate (fromCurrency, targetCurrency, day);
								double result = amount * rate;

								jsonStr.append("{");
								jsonStr.append("\"success\": true, ");
								jsonStr.append("\"query\": {");
								jsonStr.append("\"from\": \"" + fromCurrency + "\",");
								jsonStr.append("\"to\": \"" + targetCurrency + "\",");
								jsonStr.append("\"amount\": " + amount);
								jsonStr.append("},");
								jsonStr.append("\"info\": {");
								LocalDateTime now = LocalDateTime.now();
								jsonStr.append("\"timestamp\": " + now.atZone(ZoneOffset.ofTotalSeconds(0)).toEpochSecond() + ",");
								jsonStr.append("\"rate\": " + rate);
								jsonStr.append("},");
								jsonStr.append("\"historical\": \"\",");
								jsonStr.append("\"date\": \"" + day + "\",");
								jsonStr.append("\"result\": " + result);
								jsonStr.append("}");

							} catch (DateTimeParseException de) {
								jsonStr.append(SimulatorErrorMessages.INVALID_DATE);
							}
						}
					}
				}
			}
		}

		return jsonStr.toString();
	}
}
