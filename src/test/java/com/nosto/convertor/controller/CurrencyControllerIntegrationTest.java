package com.nosto.convertor.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.Filter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.nosto.convertor.App;
import com.nosto.convertor.DTO.ConvertRequest;
import com.nosto.convertor.DTO.ExchangeRate;
import com.nosto.convertor.controller.response.CurrencyConvertResponse;
import com.nosto.convertor.service.CurrencyService;

@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = App.class)
@AutoConfigureMockMvc
public class CurrencyControllerIntegrationTest {

	@Autowired
	private WebApplicationContext context;

	@Autowired
	private Filter springSecurityFilterChain;

	private MockMvc mvc;
	
	@Autowired
	ObjectMapper mapper;
	
	@MockBean
	private CurrencyService currencyService;
	
	@Before
	public void setup() {
		mvc = MockMvcBuilders
              .webAppContextSetup(context)
              .addFilters(springSecurityFilterChain)
              .build();
	}

	@Test
	public void testConvertEndpoint() throws Exception {
		final Gson gson = new Gson();
		final String url = "/currency/convert"; 
		String validBaseCurrency = "CAD";
		String validTargetCurrency = "USD";
		Double expectedRate = 1.32;
		String invalidBaseCurrency = "ABC";
		String invalidTargetCurrency = "XYZ";
		CurrencyConvertResponse successfulResponseEntity = new CurrencyConvertResponse();
		successfulResponseEntity.setValue(2.64); // Convert for 2$ CAD
		CurrencyConvertResponse errorResponseEntity = new CurrencyConvertResponse();
		errorResponseEntity.setError("Base '" + invalidTargetCurrency + "' is not supported.");

		Map<String, Double> rateMap = new HashMap<String, Double>();
		rateMap.put(validTargetCurrency, expectedRate);
		rateMap.put("EUR", 0.91);
		ExchangeRate successfulExchangeRate = new ExchangeRate();
		successfulExchangeRate.setBase(validBaseCurrency);
		successfulExchangeRate.setRates(rateMap);
		ExchangeRate errorExchangeRate = new ExchangeRate();
		errorExchangeRate.setError("Base '" + invalidBaseCurrency + "' is not supported.");

		when(currencyService.listExchangeRate(validBaseCurrency)).thenReturn(successfulExchangeRate);
		when(currencyService.listExchangeRate(invalidBaseCurrency)).thenReturn(errorExchangeRate);
		when(currencyService.listExchangeRate(invalidTargetCurrency)).thenThrow(Exception.class);
		when(currencyService.getCurrencyLocale(validTargetCurrency)).thenReturn(new Locale("en", "US"));

		// Test missing source currency
		MvcResult mvcResult = mvc.perform(post(url).with(csrf().asHeader()).content(mapper.writeValueAsString(new ConvertRequest(2.0, "", validTargetCurrency)))
			      .contentType(MediaType.APPLICATION_JSON)
			      .accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andReturn();

		// Test missing target currency
		mvcResult = mvc.perform(post(url).content(mapper.writeValueAsString(new ConvertRequest(2.0, validBaseCurrency, "")))
			      .contentType(MediaType.APPLICATION_JSON)
			      .accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andReturn();
		
		// Test negative value
		mvcResult = mvc.perform(post(url).content(mapper.writeValueAsString(new ConvertRequest(-2.0, validBaseCurrency, validTargetCurrency)))
			      .contentType(MediaType.APPLICATION_JSON)
			      .accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andReturn();
				
		// Test invalid base currency
		mvcResult = mvc.perform(post(url).content(mapper.writeValueAsString(new ConvertRequest(2.0, invalidBaseCurrency, validTargetCurrency)))
			      .contentType(MediaType.APPLICATION_JSON)
			      .accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andReturn();
		CurrencyConvertResponse responseEntity = gson.fromJson(mvcResult.getResponse().getContentAsString(), CurrencyConvertResponse.class);
		assertEquals(responseEntity.getError(), "Base '" + invalidBaseCurrency + "' is not supported.");
		
		// Test invalid target currency
		mvcResult = mvc.perform(post(url).content(mapper.writeValueAsString(new ConvertRequest(2.0, validBaseCurrency, invalidTargetCurrency)))
			      .contentType(MediaType.APPLICATION_JSON)
			      .accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andReturn();
		responseEntity = gson.fromJson(mvcResult.getResponse().getContentAsString(), CurrencyConvertResponse.class);
		assertEquals(responseEntity.getError(), "Base '" + invalidTargetCurrency + "' is not supported.");
		
		// Test valid case
		mvcResult = mvc.perform(post(url).content(mapper.writeValueAsString(new ConvertRequest(2.0, validBaseCurrency, validTargetCurrency)))
			      .contentType(MediaType.APPLICATION_JSON)
			      .accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		responseEntity = gson.fromJson(mvcResult.getResponse().getContentAsString(), CurrencyConvertResponse.class);		
		assertEquals(responseEntity.getValue(), (Double)(expectedRate*2));
		assertEquals(responseEntity.getCurrency(), validTargetCurrency);
		assertEquals(responseEntity.getLocalize(), "en-US");
		
		// Test general exception
		mvcResult = mvc.perform(post(url).content(mapper.writeValueAsString(new ConvertRequest(2.0, invalidTargetCurrency, validTargetCurrency)))
			      .contentType(MediaType.APPLICATION_JSON)
			      .accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isInternalServerError())
				.andReturn();
		responseEntity = gson.fromJson(mvcResult.getResponse().getContentAsString(), CurrencyConvertResponse.class);		 
		assertEquals(responseEntity.getError(), "Internal server error");

	}

}
