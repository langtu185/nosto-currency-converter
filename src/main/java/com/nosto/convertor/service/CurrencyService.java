package com.nosto.convertor.service;

import java.util.Locale;

import com.nosto.convertor.DTO.ExchangeRate;

public interface CurrencyService {
	public abstract ExchangeRate listExchangeRate (String baseCurrency) throws Exception;
	public abstract Locale getCurrencyLocale (String currencyCode);
}
