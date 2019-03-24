package com.andrew.server.controller;

import com.andrew.server.dto.Payload;
import com.andrew.server.exception.InvalidSequenceNumberException;
import com.andrew.server.exception.InvalidTerminalIdException;
import com.andrew.server.service.ServerTerminalService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static com.andrew.server.utility.TestUtil.runMultiThreaded;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author andrew
 */
@RunWith(SpringRunner.class)
@WebMvcTest(ServerTerminalController.class)
public class ServerTerminalControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private ServerTerminalService terminalService;

  @Test
  public void logRequest() throws Exception {
    final var payload = new Payload("1001", 0, System.currentTimeMillis());
    doNothing().when(terminalService).logRequest(payload);

    mockMvc.perform(post("/server")
        .content(objectMapper.writeValueAsString(payload))
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(terminalService, times(1)).logRequest(payload);
  }

  @Test
  public void logRequest_invalidTerminalId() throws Exception {
    final var payload = new Payload("1007", 0, System.currentTimeMillis());
    doThrow(new InvalidTerminalIdException("Invalid terminal ID")).when(terminalService).logRequest(payload);

    mockMvc.perform(post("/server")
        .content(objectMapper.writeValueAsString(payload))
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());

    verify(terminalService, times(1)).logRequest(payload);
  }

  @Test
  public void logRequest_invalidSequence() throws Exception {
    final var payload = new Payload("1005", 8, System.currentTimeMillis());
    doThrow(new InvalidSequenceNumberException("Invalid sequence")).when(terminalService).logRequest(payload);

    mockMvc.perform(post("/server")
        .content(objectMapper.writeValueAsString(payload))
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());

    verify(terminalService, times(1)).logRequest(payload);
  }

  @Test
  public void logRequest_concurrentTerminalId() throws Exception {
    final var payloadOne = new Payload("1001", 0, System.currentTimeMillis());
    final var payloadTwo = new Payload("1002", 1, System.currentTimeMillis());

    doNothing().when(terminalService).logRequest(payloadOne);
    doNothing().when(terminalService).logRequest(payloadTwo);

    runMultiThreaded(() -> {
          try {
            mockMvc.perform(post("/server")
                .content(objectMapper.writeValueAsString(payloadOne))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

            mockMvc.perform(post("/server")
                .content(objectMapper.writeValueAsString(payloadTwo))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
          } catch (Exception e) {
            e.printStackTrace();
          }
        },
        5);

    verify(terminalService, times(5)).logRequest(payloadOne);
    verify(terminalService, times(5)).logRequest(payloadTwo);
  }
}