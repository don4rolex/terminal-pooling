package com.andrew.server.service;

import com.andrew.server.dto.Payload;
import com.andrew.server.exception.InvalidSequenceNumberException;
import com.andrew.server.exception.InvalidTerminalIdException;
import com.andrew.server.model.Terminal;
import com.andrew.server.repository.ServerTerminalRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;

import static com.andrew.server.utility.TestUtil.runMultiThreaded;

/**
 * @author andrew
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class ServerTerminalServiceIT {

  @Rule
  public ExpectedException exceptionRule = ExpectedException.none();

  @Autowired
  private ServerTerminalService terminalService;

  @Autowired
  private ServerTerminalRepository terminalRepository;

  @Before
  public void setUp() {
    final var terminalOne = new Terminal("1001", LocalDateTime.now());
    terminalRepository.save(terminalOne);

    final var terminalTwo = new Terminal("1002", LocalDateTime.now());
    terminalRepository.save(terminalTwo);

    final var terminalThree = new Terminal("1003", LocalDateTime.now());
    terminalRepository.save(terminalThree);

    final var terminalFour = new Terminal("1004", LocalDateTime.now());
    terminalRepository.save(terminalFour);

    final var terminalFive = new Terminal("1005", LocalDateTime.now());
    terminalRepository.save(terminalFive);
  }

  @After
  public void tearDown() {
    terminalRepository.deleteAll();
  }

  @Test
  public void logRequest() {
    terminalService.logRequest(new Payload("1001", 0, System.currentTimeMillis()));
  }

  @Test
  public void logRequest_invalidTerminalId() {
    final var terminalId = "1008";

    exceptionRule.expect(InvalidTerminalIdException.class);
    exceptionRule.expectMessage(String.format("Invalid terminalId %s", terminalId));

    terminalService.logRequest(new Payload(terminalId, 0, System.currentTimeMillis()));
  }

  @Test
  public void logRequest_invalidSequence() {
    final var sequenceNo = 8;

    exceptionRule.expect(InvalidSequenceNumberException.class);
    exceptionRule.expectMessage(String.format("Invalid sequence number %d", sequenceNo));

    terminalService.logRequest(new Payload("1001", sequenceNo, System.currentTimeMillis()));
  }

  @Test
  public void logRequest_concurrentTerminalId() throws Exception {
    final var payload = new Payload("1001", 0, System.currentTimeMillis());
    runMultiThreaded(() -> {
          try {
            terminalService.logRequest(payload);

            terminalService.logRequest(payload);
          } catch (Exception e) {
            e.printStackTrace();
          }
        },
        5);
  }
}