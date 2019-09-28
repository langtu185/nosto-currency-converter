package com.nosto.convertor.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AppProperties {
	@Value("${app.exchangerate.url}")
	private String exchangeRateApiUrl;
	
	@Value("${app.redis.host}")
	private String cacheHost;
	
	@Value("${app.redis.port}")
	private String cachePort;
	
	@Value("${cache.timeout}")
	private String cacheTimeout;
	

	public String getExchangeRateApiUrl() {
		return exchangeRateApiUrl;
	}
	public String getCacheHost() {
		return cacheHost;
	}
	public int getCachePort() {
		return Integer.parseInt(cachePort);
	}
	public long getCacheTimeout() {
		return Long.parseLong(cacheTimeout);
	}

}
