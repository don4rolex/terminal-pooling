package com.andrew.client.controller;

import com.andrew.client.controller.ClientTerminalControllerIT.LocalRibbonClientConfiguration;
import com.andrew.client.model.Terminal;
import com.andrew.client.repository.ClientTerminalRepository;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ServerList;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.netflix.ribbon.StaticServerList;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.Duration;
import java.time.LocalDateTime;

import static com.andrew.client.utility.TestUtil.runMultiThreaded;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.OrderingComparison.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author andrew
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ContextConfiguration(classes = {LocalRibbonClientConfiguration.class})
public class ClientTerminalControllerIT {

  @Value("${max.wait.time}")
  private long maxWaitTime;

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ClientTerminalRepository terminalRepository;

  @ClassRule
  public static WireMockClassRule wiremock = new WireMockClassRule(wireMockConfig().dynamicPort());

  @Before
  public void setUp() {
    final var terminalOne = new Terminal("1001", false, LocalDateTime.now());
    terminalRepository.save(terminalOne);

    final var terminalTwo = new Terminal("1002", false, LocalDateTime.now());
    terminalRepository.save(terminalTwo);

    final var terminalThree = new Terminal("1003", false, LocalDateTime.now());
    terminalRepository.save(terminalThree);

    final var terminalFour = new Terminal("1004", false, LocalDateTime.now());
    terminalRepository.save(terminalFour);

    final var terminalFive = new Terminal("1005", false, LocalDateTime.now());
    terminalRepository.save(terminalFive);
  }

  @After
  public void tearDown() {
    terminalRepository.deleteAll();
  }

  @Test
  public void getTerminal() throws Exception {
    stubFor(WireMock.post("/server")
        .willReturn(aResponse()
            .withStatus(HttpStatus.OK.value())));

    mockMvc.perform(MockMvcRequestBuilders.get("/client")
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.terminalId").value("1001"));
  }

  @Test
  public void getTerminal_unableToAcquireLockFromServer() throws Exception {
    stubFor(WireMock.post("/server")
        .willReturn(aResponse()
            .withStatus(HttpStatus.SERVICE_UNAVAILABLE.value())));

    mockMvc.perform(MockMvcRequestBuilders.get("/client")
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isServiceUnavailable());
  }

  @Test
  public void getTerminal_invalidPayload() throws Exception {
    stubFor(WireMock.post("/server")
        .willReturn(aResponse()
            .withStatus(HttpStatus.BAD_REQUEST.value())));

    mockMvc.perform(MockMvcRequestBuilders.get("/client")
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void getTerminal_concurrentRequest() throws Exception {
    stubFor(WireMock.post("/server")
        .willReturn(aResponse()
            .withStatus(HttpStatus.OK.value())
            .withFixedDelay(1000)));

    runMultiThreaded(() -> {
          try {
            mockMvc.perform(MockMvcRequestBuilders.get("/client")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

            mockMvc.perform(MockMvcRequestBuilders.get("/client")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

            mockMvc.perform(MockMvcRequestBuilders.get("/client")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
          } catch (Exception e) {
            e.printStackTrace();
          }
        },
        5);
  }

  @Test
  public void getTerminal_noAvailableTerminal() throws Exception {
    terminalRepository.deleteAll();

    final var timeOfRequest = LocalDateTime.now();
    mockMvc.perform(MockMvcRequestBuilders.get("/client")
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isServiceUnavailable());
    final var currentTime = LocalDateTime.now();

    final var waitTime = Duration.between(timeOfRequest, currentTime).getSeconds();
    assertThat(waitTime, greaterThanOrEqualTo(maxWaitTime));
  }

  @TestConfiguration
  public static class LocalRibbonClientConfiguration {
    @Bean
    public ServerList<Server> ribbonServerList() {
      return new StaticServerList<>(new Server("localhost", wiremock.port()));
    }
  }
}