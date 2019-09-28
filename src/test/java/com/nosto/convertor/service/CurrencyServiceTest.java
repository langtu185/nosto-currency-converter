package com.nosto.convertor.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import com.nosto.convertor.App;
import com.nosto.convertor.DTO.ExchangeRate;
import com.nosto.convertor.config.AppProperties;
import com.nosto.convertor.service.impl.CurrencyServiceImpl;

@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = App.class)
public class CurrencyServiceTest {
	@Autowired
	AppProperties testProp;
	
	@Mock
    private RestTemplate restTemplate;	
	@Mock
	private AppProperties appProperties;
 
    @InjectMocks
    private CurrencyService currencyService = new CurrencyServiceImpl();
 
    @Test
    public void testListingExchangeRate() {     
    	// Prepare mock data
    	String validCurrency = "USD";
    	String invalidCurrency = "ABC";
        String successfulJSON = "{\"base\": \"" + validCurrency + "\", \"date\": \"2018-04-08\", \"rates\": { \"CAD\": 1.565, \"CHF\": 1.1798}}";
        String errorJSON = "{\"error\":\"Base '" + invalidCurrency + "' is not supported.\"}";
        ResponseEntity<String> successfulEntity = new ResponseEntity<String>(successfulJSON, HttpStatus.OK);
        ResponseEntity<String> errorEntity = new ResponseEntity<String>(errorJSON, HttpStatus.BAD_REQUEST);
		
		try {
			when(appProperties.getExchangeRateApiUrl()).thenReturn(testProp.getExchangeRateApiUrl());			
			when(restTemplate.getForEntity(testProp.getExchangeRateApiUrl()+validCurrency, String.class))
	          .thenReturn(successfulEntity);			 
			// Test with successful case
			ExchangeRate exchangeRate = currencyService.listExchangeRate(validCurrency);			
			assertTrue(exchangeRate.getRates().size()>0);
			assertEquals(exchangeRate.getBase(), validCurrency);
			assertNull(exchangeRate.getError());
			assertEquals(exchangeRate.getRates().get("CAD"), (Double)1.565);
			
			// Test with non-support currency
			when(restTemplate.getForEntity(testProp.getExchangeRateApiUrl()+invalidCurrency, String.class))
	          .thenReturn(errorEntity);
			exchangeRate = currencyService.listExchangeRate(invalidCurrency);
			assertNull(exchangeRate.getBase());
			assertNull(exchangeRate.getRates());
			assertEquals(exchangeRate.getError(),"Base '" + invalidCurrency + "' is not supported.");
		} catch (Exception e) {
			fail("Exception " + e);
		}
    }
}
