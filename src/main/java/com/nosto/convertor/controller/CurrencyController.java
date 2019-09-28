package com.nosto.convertor.controller;

import java.util.Date;
import java.util.Locale;
import java.util.Map;

import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nosto.convertor.DTO.ConvertRequest;
import com.nosto.convertor.DTO.ExchangeRate;
import com.nosto.convertor.controller.response.CurrencyConvertResponse;
import com.nosto.convertor.service.CurrencyService;

@RestController
@RequestMapping(path = "/api")
public class CurrencyController {
	private static final Logger logger = LoggerFactory.getLogger(CurrencyController.class);
	
	@Autowired
	CurrencyService currencyService;
	
	/**
	 * API End point that processes the currency converting base on API
	 * 
	 * @param ConvertRequest object that include attributes:
	 * 		 sourceCurrency - the currency that the exchange rates are generally quoted as a GIVEN 
	 * 		 targetCurrency - the currency that the exchange rates are generally quoted as an OUTPUT
	 * 		 value - the monetary value of source currency
	 * 
	 * @return CurrencyConvertResponse - object that contain the converted value and error message if existing
	 * */
	@PostMapping(path="/convert")
	public ResponseEntity<CurrencyConvertResponse> convert(
			@Valid @RequestBody ConvertRequest request){		
		long startTime = (new Date()).getTime();
		CurrencyConvertResponse responseEntity = new CurrencyConvertResponse();
		try {
			String sourceCurrency = request.getSource();
			String targetCurrency = request.getTarget();
			Double value = request.getValue();
			logger.info("convert: source=" + sourceCurrency + " - target=" + targetCurrency + " - value=" + value);

			// Validate input parameters
			// Return with message if source/target currency is missing or monetary value is negative
			if (StringUtils.isEmpty(targetCurrency) || StringUtils.isEmpty(sourceCurrency)) {
				responseEntity.setError("Source currency or target currency is missing");
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseEntity);
			}
			if (value < 0) {
				responseEntity.setError("Monetary value must be a positive number");
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseEntity);
			}

			// Make sure currency sign is upper case
			sourceCurrency = sourceCurrency.toUpperCase();
			targetCurrency = targetCurrency.toUpperCase();

			// Get the exchange rate list from the API
			ExchangeRate exchangeRate = currencyService.listExchangeRate(sourceCurrency);

			// Validate exchange rate
			// In case the list of exchange rate is exist => extract the target rate, assign to the response object and return
			if (exchangeRate != null && exchangeRate.getRates() != null) {
				logger.info("convert: fetching list of exchange rate successfully!");
				
				// return with error message in case the target currency is not found in the list of rate
				Map<String, Double> rateMap = exchangeRate.getRates();
				if(rateMap == null || ! rateMap.containsKey(targetCurrency)) {				
					logger.warn("convert: targetCurrency '" + targetCurrency + "' is not in the list");
					responseEntity.setError("Base '" + targetCurrency + "' is not supported.");
					return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseEntity);
				}
				
				// Get the target rate from the list				
				double rate = rateMap.get(targetCurrency);
				logger.info("convert: target rate = " + rate);
				
				// Get the locale base on currency code
				Locale locale = currencyService.getCurrencyLocale(targetCurrency);
				if(locale == null) {
					logger.error("convert: targetCurrency '" + targetCurrency + "' is not in the list");
					responseEntity.setError("Base '" + targetCurrency + "' is not supported.");
					return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseEntity);
				}
				logger.info("convert: locale = " + locale.toLanguageTag());
								
				// Prepare the HTTP Header
				long serverTime = (new Date()).getTime() - startTime;
				HttpHeaders responseHeaders = new HttpHeaders();
				responseHeaders.set("Server-Timing", serverTime + "ms");				
				
				
				// Return result
				responseEntity.setValue(value * rate);	
				responseEntity.setCurrency(targetCurrency);
				responseEntity.setLocalize(locale.toLanguageTag());
				return ResponseEntity.ok().headers(responseHeaders).body(responseEntity);
								
			// In case there's error message => Return the response object with this error message
			} else if (exchangeRate != null && !StringUtils.isEmpty(exchangeRate.getError())) {
				String errorMessage = exchangeRate.getError();
				responseEntity.setError(errorMessage);
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseEntity);
			// Other case: Internal sever error (which should not happen)
			} else {
				throw new Exception("Error in getting the list of currency exchange rate");
			}

		} catch (Exception e) {
			logger.error("convert: Error:" + e.getMessage());
			responseEntity.setError("Internal server error");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseEntity);
		}		
	}

}
