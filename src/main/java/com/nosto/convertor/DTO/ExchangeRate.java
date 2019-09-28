package com.nosto.convertor.DTO;

import java.io.Serializable;
import java.util.Map;

public class ExchangeRate implements Serializable{
	private static final long serialVersionUID = 1L;
	private String base;
	private String date;
	private Map<String, Double> rates;
	private String error;

	public String getBase() {
		return base;
	}

	public void setBase(String base) {
		this.base = base;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public Map<String, Double>  getRates() {
		return rates;
	}

	public void setRates(Map<String, Double>  rates) {
		this.rates = rates;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}
}