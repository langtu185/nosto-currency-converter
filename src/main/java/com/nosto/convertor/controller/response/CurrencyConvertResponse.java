package com.nosto.convertor.controller.response;

import java.io.Serializable;

public class CurrencyConvertResponse extends BaseResponse implements Serializable {
	static final long serialVersionUID = 1L;
	private Double value;
	private String currency;
	private String localize;
	
	public Double getValue() {
		return value;
	}
	public void setValue(Double value) {
		this.value = value;
	}
	public String getCurrency() {
		return currency;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	public String getLocalize() {
		return localize;
	}
	public void setLocalize(String localize) {
		this.localize = localize;
	}
	
}
