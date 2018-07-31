package com.upandcoding.jfixerdemo.simulator;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import com.upandcoding.fixer.model.Currency;
import com.upandcoding.jfixerdemo.Common;

public class SimulatorUtils {

	private static final Logger log = LoggerFactory.getLogger(SimulatorUtils.class);

	public static final String simulatorKey = "API_KEY";
	public static final String invalidKey = "INVALID_API_KEY";

	protected static final String MIN_DATE = "1999-01-01";

	protected static final String[] supportedSymbols = { "CHF", "USD", "EUR", "CAD", "JPY", "GBP", "RUB" };

	private static final List<Currency> supportedCurrencies = new ArrayList<>();

	private static final Map<String, Map<String, Double>> exchangeRatesPerCurrency = new HashMap<>();

	/* **********************************************************
	 * COMMON VERIFICATIONS
	 * **********************************************************
	 */
	protected static StringBuffer getBasicVerifications(String accessKey, String baseCurrency) {
		StringBuffer jsonStr = new StringBuffer();

		log.debug("AccessKey: {}", accessKey);

		if (StringUtils.isBlank(accessKey)) {
			jsonStr.append(SimulatorErrorMessages.MISSING_KEY);
			return jsonStr;
		}

		if (!SimulatorUtils.isValidAccessKey(accessKey)) {
			jsonStr.append(SimulatorErrorMessages.INVALID_KEY);
			return jsonStr;
		}

		if (!SimulatorUtils.isSupportedBaseCurrency(baseCurrency)) {
			jsonStr.append(SimulatorErrorMessages.INVALID_BASE_CURRENCY);
			return jsonStr;
		}

		return jsonStr;
	}

	protected static StringBuffer getConvertCurrencyVerifications(String symbol, boolean isFrom) {
		StringBuffer jsonStr = new StringBuffer();

		if (StringUtils.isNotBlank(symbol)) {
			boolean isErr = false;
			List<Currency> supportedCurrencies = getSupportedCurrencies(false);
			List<Currency> enteredCurrencies = getEnteredCurrencies(symbol);
			for (Currency currency : enteredCurrencies) {
				boolean found = false;
				for (Currency supportedCurrency : supportedCurrencies) {
					if (currency.getSymbol().equalsIgnoreCase(supportedCurrency.getSymbol())) {
						found = true;
						break;
					}
				}
				if (!found) {
					isErr = true;
					break;
				}
			}

			if (isErr) {
				if (isFrom) {
					jsonStr.append(SimulatorErrorMessages.CONVERT_INVALID_FROM_CURRENCY);
				} else {
					jsonStr.append(SimulatorErrorMessages.CONVERT_INVALID_TO_CURRENCY);
				}
			}
		}

		return jsonStr;
	}

	protected static StringBuffer getSymbolsVerifications(String symbols) {
		StringBuffer jsonStr = new StringBuffer();

		if (StringUtils.isNotBlank(symbols)) {
			boolean isErr = false;
			List<Currency> supportedCurrencies = getSupportedCurrencies(false);
			List<Currency> enteredCurrencies = getEnteredCurrencies(symbols);
			for (Currency currency : enteredCurrencies) {
				boolean found = false;
				for (Currency supportedCurrency : supportedCurrencies) {
					if (currency.getSymbol().equalsIgnoreCase(supportedCurrency.getSymbol())) {
						found = true;
						break;
					}
				}
				if (!found) {
					isErr = true;
					break;
				}
			}

			if (isErr) {
				jsonStr.append(SimulatorErrorMessages.INVALID_CURRENCY);
			}
		}

		return jsonStr;
	}

	protected static StringBuffer getHistoryDateVerifications(String day) {
		StringBuffer jsonStr = new StringBuffer();

		if (StringUtils.isBlank(day)) {
			jsonStr.append(SimulatorErrorMessages.MISSING_DATE);
			return jsonStr;
		} else {
			try {
				LocalDate today = LocalDate.now();
				LocalDate requestedDay = LocalDate.parse(day, Common.formatter);
				LocalDate firstDate = LocalDate.parse(MIN_DATE, Common.formatter);
				if (requestedDay.isBefore(firstDate)) {
					jsonStr.append(SimulatorErrorMessages.INVALID_DATE);
				}
				if (requestedDay.isAfter(today)) {
					jsonStr.append(SimulatorErrorMessages.INVALID_DATE);
				}
			} catch (DateTimeParseException de) {
				jsonStr.append(SimulatorErrorMessages.INVALID_DATE);
			}
		}

		return jsonStr;
	}

	protected static StringBuffer getTimeSeriesDateVerifications(String startDate, String endDate) {
		StringBuffer jsonStr = new StringBuffer();

		if (StringUtils.isBlank(startDate) || StringUtils.isBlank(endDate)) {
			jsonStr.append(SimulatorErrorMessages.MISSING_TIME_FRAME);
			return jsonStr;

		} else {

			LocalDate today = LocalDate.now();
			LocalDate firstDate = LocalDate.parse(MIN_DATE, Common.formatter);
			LocalDate localStartDate;
			LocalDate localEndDate;

			try {
				localStartDate = LocalDate.parse(startDate, Common.formatter);
				if (localStartDate.isBefore(firstDate)) {
					jsonStr.append(SimulatorErrorMessages.EXCEED_TIME_FRAME);
					return jsonStr;
				}
				if (localStartDate.isAfter(today)) {
					jsonStr.append(SimulatorErrorMessages.INVALID_START_DATE);
					return jsonStr;
				}
			} catch (DateTimeParseException de) {
				jsonStr.append(SimulatorErrorMessages.INVALID_START_DATE);
				return jsonStr;
			}

			try {
				localEndDate = LocalDate.parse(endDate, Common.formatter);
				if (localEndDate.isBefore(firstDate)) {
					jsonStr.append(SimulatorErrorMessages.EXCEED_TIME_FRAME);
					return jsonStr;
				}
				if (localEndDate.isAfter(today)) {
					jsonStr.append(SimulatorErrorMessages.INVALID_END_DATE);
					return jsonStr;
				}
				if (localEndDate.isBefore(localStartDate)) {

					jsonStr.append(SimulatorErrorMessages.INVALID_TIME_FRAME);
					return jsonStr;
				}
			} catch (DateTimeParseException de) {
				jsonStr.append(SimulatorErrorMessages.INVALID_END_DATE);
				return jsonStr;
			}

		}

		return jsonStr;
	}

	/* **********************************************************
	 * RATES
	 * **********************************************************
	 */
	private static double randomWithRange(double min, double max) {
		double range = (max - min) + 1;
		return (Math.random() * range) + min;
	}

	protected static double getRateAtDate(String baseCurrency, String currency, String date) {
		double result = -1;
		Map<String, Map<String, Double>> exchangeRates = getExchangeRates(baseCurrency, false);
		if (exchangeRates != null && !exchangeRates.isEmpty() && StringUtils.isNotBlank(currency)) {
			Map<String, Double> ratesForCur = exchangeRates.get(currency);
			if (ratesForCur != null && !ratesForCur.isEmpty()) {
				Double rate = ratesForCur.get(date);
				if (rate != null) {
					return rate;
				}
			}
		}
		// log.debug("Currency:{} {} -> {}", currency, date, result);
		return result;
	}

	protected static double getFluctuation(String baseCurrency, String currency, String startDate, String endDate, boolean isPct) {
		double result = 0;
		// log.debug("Currency: {}. Start: {}, End: {}, isPct: {}", currency, startDate,
		// endDate, isPct);
		Map<String, Map<String, Double>> exchangeRates = getExchangeRates(baseCurrency, false);
		if (exchangeRates != null && !exchangeRates.isEmpty() && StringUtils.isNotBlank(currency)) {
			Map<String, Double> ratesForCur = exchangeRates.get(currency);
			if (ratesForCur != null && !ratesForCur.isEmpty()) {
				Double rateStart = ratesForCur.get(startDate);
				Double rateEnd = ratesForCur.get(endDate);
				if (rateStart != null && rateEnd != null) {
					if (isPct) {
						if (rateStart != 0) {
							return (rateEnd - rateStart) / rateStart;
						} else {
							return 0;
						}
					} else {
						return (rateEnd - rateStart);
					}
				}
			}
		}
		return result;
	}

	protected static Map<String, Map<String, Double>> getExchangeRates(String baseCurrency, boolean force) {

		if (exchangeRatesPerCurrency == null || exchangeRatesPerCurrency.isEmpty() || force) {

			exchangeRatesPerCurrency.clear();

			List<Currency> currencies = getSupportedCurrencies(false);
			LocalDate startDate = LocalDate.parse(MIN_DATE, Common.formatter);

			LocalDate today = LocalDate.now();

			for (Currency currency : currencies) {

				Map<String, Double> currencyRates = new HashMap<>();
				double startPoint = randomWithRange(0, 5);

				double previous = startPoint;
				LocalDate tmpDate = startDate;
				while (tmpDate.isBefore(today) || tmpDate.isEqual(today)) {

					double rate;
					if (currency.getSymbol().equalsIgnoreCase(baseCurrency)) {
						rate = 1;
					} else {
						rate = getRandomExchangeRate(previous);
					}
					currencyRates.put(Common.formatter.format(tmpDate), rate);
					previous = rate;

					tmpDate = tmpDate.plusDays(1);
				}

				exchangeRatesPerCurrency.put(currency.getSymbol(), currencyRates);

			}

		}
		return exchangeRatesPerCurrency;
	}

	private static double getRandomExchangeRate(double rate) {
		return rate * (1 + (10 * Math.random() - 5) / 100);
	}

	/* **********************************************************
	 * CURRENCIES
	 * **********************************************************
	 */
	protected static List<Currency> getEnteredCurrencies(String symbols) {
		List<Currency> currencies = new ArrayList<>();
		if (StringUtils.isNotBlank(symbols)) {
			List<String> symbolList = Arrays.asList(symbols.split(","));
			for (String symbol : symbolList) {
				for (Currency c : getSupportedCurrencies(false)) {
					if (c.getSymbol().equalsIgnoreCase(symbol)) {
						currencies.add(c);
					}
				}
			}
		} else {
			currencies = getSupportedCurrencies(false);
		}
		return currencies;
	}

	protected static List<Currency> getSupportedCurrencies(boolean force) {
		if (CollectionUtils.isEmpty(supportedCurrencies) || (force == true)) {
			supportedCurrencies.clear();
			for (String symbol : supportedSymbols) {
				java.util.Currency jCurrency = java.util.Currency.getInstance(symbol);
				String dName = jCurrency.getDisplayName(Locale.US);
				Currency currency = new Currency(symbol, dName);
				supportedCurrencies.add(currency);
			}
		}
		return supportedCurrencies;
	}

	protected static List<String> getCurrencySymbols(boolean force) {
		List<String> symbols = new ArrayList<>();
		List<Currency> currencies = getSupportedCurrencies(force);
		for (Currency currency : currencies) {
			symbols.add(currency.getSymbol());
		}
		return symbols;
	}

	protected static boolean isSupportedBaseCurrency(String baseCurrency) {
		// log.debug("baseCurrency: {}", baseCurrency);
		boolean isFound = false;
		if (StringUtils.isNotBlank(baseCurrency)) {
			if ("EUR".equalsIgnoreCase(baseCurrency)) {
				isFound = true;
			} else {
				List<Currency> currencies = getSupportedCurrencies(false);
				// log.debug("Currencies: {}", currencies.size());
				for (Currency currency : currencies) {
					if (currency.getSymbol().equalsIgnoreCase(baseCurrency)) {
						isFound = true;
						// log.debug("found: {}", baseCurrency);
						break;
					}
				}
			}
		}
		return isFound;
	}

	/* **********************************************************
	 * BASE CURRENCY
	 * **********************************************************
	 */
	protected static String getDefaultBaseCurrency(String accessKey) {
		return "EUR";
	}

	/* **********************************************************
	 * ACCESS KEY
	 * **********************************************************
	 */
	protected static boolean isValidAccessKey(String accessKey) {
		if (invalidKey.equalsIgnoreCase(accessKey)) {
			return false;
		} else {
			return StringUtils.isNotBlank(accessKey);
		}
	}

}
