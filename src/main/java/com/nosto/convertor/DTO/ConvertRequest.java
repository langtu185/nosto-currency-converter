package com.nosto.convertor.DTO;

import java.io.Serializable;

import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

public class ConvertRequest implements Serializable{
	private static final long serialVersionUID = 1L;
	@Positive (message = "Monetary value must be a positive number")
	private Double value;
	@Size(max=3,min=3, message = "Source should be 3 characters")
	private String source;
	@Size(max=3,min=3, message = "Target should be 3 characters")
	private String target;
	
	public ConvertRequest() {}
	public ConvertRequest(Double value, String source, String target) {
		this.value = value;
		this.source = source;
		this.target = target;
	}
	
	public Double getValue() {
		return value;
	}
	public void setValue(Double value) {
		this.value = value;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public String getTarget() {
		return target;
	}
	public void setTarget(String target) {
		this.target = target;
	}
	
}
