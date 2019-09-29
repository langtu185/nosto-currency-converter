package com.nosto.convertor.config;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import com.nosto.convertor.controller.CurrencyController;

@Configuration
@ConfigurationProperties(prefix = "cache")
public class CacheConfig extends CachingConfigurerSupport {
	private static final Logger logger = LoggerFactory.getLogger(CacheConfig.class);
	private Map<String, Long> cacheExpirations = new HashMap<>();
	
	@Autowired
	AppProperties appProperties;
	
    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        String cacheHost = appProperties.getCacheHost(); // Get local Cache host
        int cachePort = appProperties.getCachePort(); // Get local Cache port
        // Get Redis Cloud if exist
        try {        	
        	logger.info("Redis Could URI: " + System.getenv("REDISCLOUD_URL"));
			URI redisUri = new URI(System.getenv("REDISCLOUD_URL"));			
			if(redisUri != null) {
				logger.info(redisUri.toString());
				cacheHost = redisUri.getHost();
				cachePort = redisUri.getPort();
			}			
			logger.info("Redis Could host: " + cacheHost);
			logger.info("Redis Could port: " + cachePort);
		} catch (URISyntaxException e) {
			// Do nothing
		}
        redisStandaloneConfiguration.setHostName(cacheHost);
        redisStandaloneConfiguration.setPort(cachePort);
        return new LettuceConnectionFactory(redisStandaloneConfiguration);
    }

    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory cf) {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<String, String>();
        redisTemplate.setConnectionFactory(cf);
        return redisTemplate;
    }

    @Bean
    public RedisCacheConfiguration cacheConfiguration() {
    	return createCacheConfiguration(appProperties.getCacheTimeout());
    }
    
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        for (Entry<String, Long> cacheNameAndTimeout : cacheExpirations.entrySet()) {
            cacheConfigurations.put(cacheNameAndTimeout.getKey(), createCacheConfiguration(cacheNameAndTimeout.getValue()));
        }
        
        return RedisCacheManager
                .builder(redisConnectionFactory)
                .cacheDefaults(cacheConfiguration())
                .withInitialCacheConfigurations(cacheConfigurations).build();
    }

    private static RedisCacheConfiguration createCacheConfiguration(long timeoutInSeconds) {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(timeoutInSeconds));
    }
    
	public Map<String, Long> getCacheExpirations() {
		return cacheExpirations;
	}

	public void setCacheExpirations(Map<String, Long> cacheExpirations) {
		this.cacheExpirations = cacheExpirations;
	}
}