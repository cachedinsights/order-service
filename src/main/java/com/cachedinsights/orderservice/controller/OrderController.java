package com.cachedinsights.orderservice.controller;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import org.apache.coyote.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.time.LocalTime;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/order")
public class OrderController
{
    private static final Logger logger = LoggerFactory.getLogger("OrderController");
    private static final String INSTANCE_1 = "backendA";
    private static final String INSTANCE_2 = "backendB";
    private static final String PROTOCOL = "http";
    private static final String HOST = "localhost";
    private static final String PORT = "9091";
    private static final String DELIMITER1 = ":";
    private static final String DELIMITER2 = "/";

    @GetMapping("/{orderId}")
    @Bulkhead(name = INSTANCE_1, fallbackMethod = "bulkheadFallback")
    public ResponseEntity<String> order(@PathVariable("orderId") String orderId)
    {
        logger.debug("Initiating order for order id: {orderId}... ", orderId);
        RestTemplate restTemplate = new RestTemplate();
        StringBuilder urlBuilder =  new StringBuilder();
        String path = "api"+DELIMITER2+"v1"+DELIMITER2+"pay";

        String paymentUrl = urlBuilder.append(PROTOCOL)
                .append(DELIMITER1)
                .append(DELIMITER2)
                .append(DELIMITER2)
                .append(HOST)
                .append(DELIMITER1)
                .append(PORT)
                .append(DELIMITER2)
                .append(path)
                .append(DELIMITER2)
                .toString();

        logger.debug("Initiating payment process ...");
        logger.info(LocalTime.now() + " Thread = " + Thread.currentThread().getName());
        ResponseEntity<String> response = restTemplate.getForEntity(paymentUrl + orderId, String.class);
        return response;
    }

    @GetMapping("/process")
    @Bulkhead(name = INSTANCE_2, type = Bulkhead.Type.THREADPOOL, fallbackMethod = "fallback")
    public CompletableFuture<ResponseEntity<String>> processOrder()
    {
        RestTemplate restTemplate = new RestTemplate();
        StringBuilder urlBuilder =  new StringBuilder();
        String path = "api"+DELIMITER2+"v1"+DELIMITER2+"pay";

        String paymentUrl = urlBuilder.append(PROTOCOL)
                .append(DELIMITER1)
                .append(DELIMITER2)
                .append(DELIMITER2)
                .append(HOST)
                .append(DELIMITER1)
                .append(PORT)
                .append(DELIMITER2)
                .append(path)
                .append(DELIMITER2)
                .toString();

        logger.debug("Initiating payment process ...");
        logger.info(LocalTime.now() + " Thread = " + Thread.currentThread().getName());

        return CompletableFuture.completedFuture(restTemplate.getForEntity(paymentUrl + 1, String.class));
    }

    public ResponseEntity<String> bulkheadFallback(Exception e)
    {
        System.out.println("Fallback method : "+ e.getMessage());
        return new ResponseEntity<String>("Payment service is not available at the moment. Please try after sometime.", HttpStatus.TOO_MANY_REQUESTS);
    }

    public CompletableFuture<ResponseEntity<String>> fallback(Exception e)
    {
        System.out.println("Threadpool Fallback : "+e.getMessage());
        return CompletableFuture.completedFuture(new ResponseEntity<String>("Payment service is not available at the moment. Please try after sometime.",HttpStatus.TOO_MANY_REQUESTS));
    }
}
