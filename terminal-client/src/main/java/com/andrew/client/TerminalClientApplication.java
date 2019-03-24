package com.andrew.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author andrew
 */
@SpringBootApplication
@EnableFeignClients("com.andrew.client.proxy")
@EnableDiscoveryClient
public class TerminalClientApplication {

  public static void main(String[] args) {
    SpringApplication.run(TerminalClientApplication.class, args);
  }
}