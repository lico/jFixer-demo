package com.upandcoding.jfixerdemo.simulator;

public class SimulatorErrorMessages {

	// ERRORS 10x
	public static final String INVALID_KEY = "{\"success\":false,\"error\":{\"code\":101,\"type\":\"invalid_access_key\",\"info\":\"You have not supplied a valid API Access Key. [Technical Support: support@apilayer.com]\"}}";
	public static final String MISSING_KEY = "{\"success\":false,\"error\":{\"code\":101,\"type\":\"missing_access_key\",\"info\":\"You have not supplied an API Access Key. [Required format: access_key=YOUR_ACCESS_KEY]\"}}";
	public static final String NO_RATES_RESULT = "{\"success\":false,\"error\":{\"code\":106,\"type\":\"no_rates_available\",\"info\":\"Your query did not return any results. Please try again.\"}}";
	
	// ERRORS 20x
	public static final String INVALID_BASE_CURRENCY = "{\"success\":false,\"error\":{\"code\":201,\"type\":\"invalid_base_currency\"}}";
	public static final String INVALID_CURRENCY = "{\"success\":false,\"error\":{\"code\":202,\"type\":\"invalid_currency_codes\",\"info\":\"You have provided one or more invalid Currency Codes. [Required format: currencies=EUR,USD,GBP,...]\"}}";
	
	// ERRORS 30x
	// Covers [historical, convert], except 301 [historical] only
	// For dates in the future 2118-04-30 or dates in the past 1815-04-30 (before 1st Jan 1999)
	public static final String MISSING_DATE = "{\"success\":false,\"error\":{\"code\":301,\"type\":\"invalid_date\",\"info\":\"No date has been specified. [Required format: date=YYYY-MM-DD]\"}}";
	public static final String INVALID_DATE = "{\"success\":false,\"error\":{\"code\":302,\"type\":\"invalid_date\",\"info\":\"You have entered an invalid date. [Required format: date=YYYY-MM-DD]\"}}";
	
	// ERRORS 40x
	public static final String CONVERT_INVALID_TO_CURRENCY = "{\"success\":false,\"error\":{\"code\":402,\"type\":\"invalid_to_currency\",\"info\":\"You have entered an invalid \\\"to\\\" property. [Example: to=GBP]\"}}";
	public static final String CONVERT_INVALID_FROM_CURRENCY = "{\"success\":false,\"error\":{\"code\":402,\"type\":\"invalid_from_currency\",\"info\":\"You have entered an invalid \\\"from\\\" property. [Example: from=EUR]\"}}";
	// If amount not numeric or <0 or equal to 0
	public static final String CONVERT_INVALID_AMOUNT = "{\"success\":false,\"error\":{\"code\":403,\"type\":\"invalid_conversion_amount\",\"info\":\"You have not specified an amount to be converted. [Example: amount=5]\"}}";
	
	// ERRORS 50x 
	// Covers [timeseries, fluctuation], except 501 [timeseries] only
	public static final String MISSING_TIME_FRAME = "{\"success\":false,\"error\":{\"code\":501,\"type\":\"no_timeframe_supplied\",\"info\":\"You have not specified a Time-Frame. [Required format: ...&start_date=YYYY-MM-DD&end_date=YYYY-MM-DD]\"}}";
	public static final String INVALID_START_DATE = "{\"success\":false,\"error\":{\"code\":502,\"type\":\"invalid_start_date\",\"info\":\"You have entered an invalid \\\"start_date\\\" property. [Required format: start_date=YYYY-MM-DD]\"}}";
	public static final String INVALID_END_DATE = "{\"success\":false,\"error\":{\"code\":503,\"type\":\"invalid_end_date\",\"info\":\"You have entered an invalid \\\"end_date\\\" property. [Required format: end_date=YYYY-MM-DD]\"}}";
	public static final String INVALID_TIME_FRAME = "{\"success\":false,\"error\":{\"code\":504,\"type\":\"invalid_time_frame\",\"info\":\"You have entered an invalid Time-Frame. [Required format: ...&start_date=YYYY-MM-DD&end_date=YYYY-MM-DD]\"}}";
	public static final String EXCEED_TIME_FRAME = "{\"success\":false,\"error\":{\"code\":505,\"type\":\"invalid_time_frame\",\"info\":\"The specified timeframe is too long, exceeding 365 days.\"}}";
	
	
	
}
