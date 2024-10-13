package com.youssefgamal.categoryservice.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;

@Configuration
public class OtlpConfig {

	
	@Bean
	public OtlpGrpcSpanExporter otlpHttpSpanExporter(@Value("${tracing.url}") String url) {
	  return OtlpGrpcSpanExporter.builder()
			  .setEndpoint(url)
			  .build();
	}
	
	
}
