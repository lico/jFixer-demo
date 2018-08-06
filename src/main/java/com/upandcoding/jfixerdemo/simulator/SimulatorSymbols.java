package com.upandcoding.jfixerdemo.simulator;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.upandcoding.fixer.model.Currency;

@Controller
@RequestMapping(value={"/jFixer/api", "/jFixer/api2"}, method = RequestMethod.GET, produces = { "application/json; charset=UTF-8" })
public class SimulatorSymbols {

	private static final Logger log = LoggerFactory.getLogger(SimulatorSymbols.class);

	@RequestMapping(value = "/symbols", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public String displayExchangeRate(@RequestParam(value = "access_key", required = false) String accessKey) throws Exception {

		StringBuffer jsonStr = new StringBuffer();

		if (SimulatorUtils.isValidAccessKey(accessKey)) {
			jsonStr.append("{\"success\": true, \"symbols\":{");

			List<Currency> currencies = SimulatorUtils.getSupportedCurrencies(false);

			boolean start = true;
			for (Currency currency : currencies) {
				if (!start) {
					jsonStr.append(", ");
				} else {
					start = false;
				}
				jsonStr.append("\"" + currency.getSymbol() + "\":\"" + currency.getDisplayName() + "\"");
			}

			jsonStr.append("}}");

		} else {
			jsonStr.append(SimulatorErrorMessages.INVALID_KEY);
		}

		return jsonStr.toString();
	}
}
