package com.nosto.convertor.service.impl;

import java.text.NumberFormat;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.nosto.convertor.DTO.ExchangeRate;
import com.nosto.convertor.config.AppProperties;
import com.nosto.convertor.service.CurrencyService;

@Service
public class CurrencyServiceImpl implements CurrencyService{
	private static final Logger logger = LoggerFactory.getLogger(CurrencyServiceImpl.class);

	@Autowired
	AppProperties appProperties;
	@Autowired
	RestTemplate restTemplate;
	
	private Gson gson = new Gson();
	/**
	 * Method that call the 3rd party API to get the exchange rate info. Assume the
	 * input parameters is valid
	 * 
	 * @param baseCurrency - the currency that the exchange rates are generally
	 *                     quoted as a given country
	 * @return the ExchangeRate object. In case of HttpStatusCodeException, return
	 *         the ExchangeRate object with 'error' attribute.
	 * @throws Exception
	 */	
	@Override
	@Cacheable(cacheNames = "exchangeRate", key = "'exchangeRate_'.concat(#baseCurrency)")
	public ExchangeRate listExchangeRate(String baseCurrency) throws Exception {
		logger.info("listExchangeRate: baseCurrency=" + baseCurrency);

		try {
			// Prepare URL variables
			String requestUrl = appProperties.getExchangeRateApiUrl() + baseCurrency;
			logger.info("listExchangeRate: requestUrl=" + requestUrl);

			// Make an API call to get the exchange rate information
			// Include URL variables in the call
			ResponseEntity<String> responseEntity = restTemplate.getForEntity(requestUrl, String.class);
			logger.info("listExchangeRate: responseEntity=" + responseEntity.toString());
			
			// Parse JSON response into Object
			return gson.fromJson(responseEntity.getBody(), ExchangeRate.class);

		} catch (HttpStatusCodeException e) {
			ExchangeRate errorObject = gson.fromJson(e.getResponseBodyAsString(), ExchangeRate.class);
			logger.warn("listExchangeRate - HTTPStatusError: " + errorObject.getError());
			return errorObject;
		}
	}
	
	/** 
	 * Method that find the locale base on currency code
	 * @param currency code - 3 characters as currency code according to ISO 4217
	 * @return locale object in case currency code is valid and found in the list.
	 * 			Otherwise return null
	 * */
	@Override
	@Cacheable(cacheNames = "currencyLocale", key = "'currencyLocale'.concat(#currencyCode)")
	public Locale getCurrencyLocale(String currencyCode) {
		if (StringUtils.isEmpty(currencyCode)) return null;
		
		for (Locale locale : NumberFormat.getAvailableLocales()) {
	        String code = NumberFormat.getCurrencyInstance(locale).getCurrency().getCurrencyCode();
	        if (currencyCode.equals(code)) {
	            return locale;
	        }
	    }  
	    return null;
	}
}
