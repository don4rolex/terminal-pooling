package com.andrew.server.controller;

import com.andrew.server.dto.Payload;
import com.andrew.server.model.Terminal;
import com.andrew.server.repository.ServerTerminalRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static com.andrew.server.utility.TestUtil.runMultiThreaded;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author andrew
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
public class ServerTerminalControllerIT {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

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
  public void logRequest() throws Exception {
    mockMvc.perform(post("/server")
        .content(objectMapper.writeValueAsString(new Payload("1001", 0, System.currentTimeMillis())))
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  public void logRequest_invalidTerminalId() throws Exception {
    mockMvc.perform(post("/server")
        .content(objectMapper.writeValueAsString(new Payload("1007", 0, System.currentTimeMillis())))
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void logRequest_invalidSequence() throws Exception {
    mockMvc.perform(post("/server")
        .content(objectMapper.writeValueAsString(new Payload("1007", 8, System.currentTimeMillis())))
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void logRequest_concurrentTerminalId() throws Exception {
    runMultiThreaded(() -> {
          try {
            mockMvc.perform(post("/server")
                .content(objectMapper.writeValueAsString(new Payload("1001", 0, System.currentTimeMillis())))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

            mockMvc.perform(post("/server")
                .content(objectMapper.writeValueAsString(new Payload("1002", 1, System.currentTimeMillis())))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
          } catch (Exception e) {
            e.printStackTrace();
          }
        },
        5);
  }
}