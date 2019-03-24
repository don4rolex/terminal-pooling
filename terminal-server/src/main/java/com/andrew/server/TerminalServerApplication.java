package com.andrew.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author andrew
 */
@SpringBootApplication
@EnableDiscoveryClient
public class TerminalServerApplication {

  public static void main(String[] args) {
    SpringApplication.run(TerminalServerApplication.class, args);
  }
}