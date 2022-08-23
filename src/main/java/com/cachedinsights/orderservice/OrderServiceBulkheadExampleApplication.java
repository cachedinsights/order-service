package com.cachedinsights.orderservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.client.RestTemplate;

import java.util.stream.IntStream;

@SpringBootApplication
public class OrderServiceBulkheadExampleApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrderServiceBulkheadExampleApplication.class, args);
		IntStream.range(0,10).parallel().forEach(t -> {
			String response = new RestTemplate().getForObject("http://localhost:9090/api/v1/order/process", String.class);
		});
	}

}
