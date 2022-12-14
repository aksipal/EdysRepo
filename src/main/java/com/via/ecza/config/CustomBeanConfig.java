package com.via.ecza.config;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class CustomBeanConfig {
//	@Bean
//	public ModelMapper getModelMapper() {
//		return new ModelMapper();
//	}

	@Bean
	public ModelMapper getModelMapper() {
		ModelMapper modelmapper =new ModelMapper();
		modelmapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
		return modelmapper;
	}

//	@Bean
//	@Scope("prototype")
//	Logger logger(InjectionPoint injectionPoint){
//		return LoggerFactory.getLogger(injectionPoint.getMethodParameter().getContainingClass());
//
//	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public Logger logger(InjectionPoint injectionPoint){
		return LoggerFactory.getLogger(injectionPoint.getMethodParameter().getContainingClass());
	}
}
