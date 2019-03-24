package com.andrew.client.service;

import com.andrew.client.exception.NoAvailableTerminalException;
import com.andrew.client.exception.UnableToAcquireLockException;
import com.andrew.client.model.Terminal;
import com.andrew.client.repository.ClientTerminalRepository;
import com.andrew.client.service.ClientTerminalServiceIT.LocalRibbonClientConfiguration;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ServerList;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.netflix.ribbon.StaticServerList;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Duration;
import java.time.LocalDateTime;

import static com.andrew.client.utility.TestUtil.runMultiThreaded;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.OrderingComparison.greaterThanOrEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author andrew
 */

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = {LocalRibbonClientConfiguration.class})
public class ClientTerminalServiceIT {

  @Value("${max.wait.time}")
  private long maxWaitTime;

  @ClassRule
  public static WireMockClassRule wiremock = new WireMockClassRule(wireMockConfig().dynamicPort());

  @Rule
  public ExpectedException exceptionRule = ExpectedException.none();

  @Autowired
  private ClientTerminalService terminalService;

  @Autowired
  private ClientTerminalRepository terminalRepository;

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
  public void getTerminal() {
    stubFor(WireMock.post("/server")
        .willReturn(aResponse()
            .withStatus(HttpStatus.OK.value())));

    final var terminal = terminalService.getTerminal();
    assertEquals("1001", terminal.getTerminalId());
  }


  @Test
  public void getTerminal_unableToAcquireLockFromServer() {
    exceptionRule.expect(UnableToAcquireLockException.class);

    stubFor(WireMock.post("/server")
        .willReturn(aResponse()
            .withStatus(HttpStatus.SERVICE_UNAVAILABLE.value())));

    terminalService.getTerminal();
  }

  @Test
  public void getTerminal_invalidPayload() {
    exceptionRule.expect(IllegalArgumentException.class);
    exceptionRule.expectMessage("Invalid payload");

    stubFor(WireMock.post("/server")
        .willReturn(aResponse()
            .withStatus(HttpStatus.BAD_REQUEST.value())));

    terminalService.getTerminal();
  }

  @Test
  public void getTerminal_concurrentRequest() throws Exception {
    stubFor(WireMock.post("/server")
        .willReturn(aResponse()
            .withStatus(HttpStatus.OK.value())
            .withFixedDelay(1000)));

    runMultiThreaded(() -> {
          final var terminalOne = terminalService.getTerminal();
          assertNotNull(terminalOne.getTerminalId());

          final var terminalTwo = terminalService.getTerminal();
          assertNotNull(terminalTwo.getTerminalId());

          final var terminalThree = terminalService.getTerminal();
          assertNotNull(terminalThree.getTerminalId());
        },
        5);
  }

  @Test
  public void getTerminal_noAvailableTerminal() {
    exceptionRule.expect(NoAvailableTerminalException.class);
    exceptionRule.expectMessage(String.format("No available terminal after %d seconds", maxWaitTime));

    terminalRepository.deleteAll();

    final var timeOfRequest = LocalDateTime.now();
    terminalService.getTerminal();
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