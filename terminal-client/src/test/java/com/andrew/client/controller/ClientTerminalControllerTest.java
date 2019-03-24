package com.andrew.client.controller;

import com.andrew.client.dto.TerminalDto;
import com.andrew.client.exception.NoAvailableTerminalException;
import com.andrew.client.exception.UnableToAcquireLockException;
import com.andrew.client.service.ClientTerminalService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static com.andrew.client.utility.TestUtil.runMultiThreaded;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author andrew
 */
@RunWith(SpringRunner.class)
@WebMvcTest(ClientTerminalController.class)
public class ClientTerminalControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private ClientTerminalService terminalService;

  @Test
  public void getTerminal() throws Exception {
    when(terminalService.getTerminal()).thenReturn(new TerminalDto("1001", 0, System.currentTimeMillis()));

    mockMvc.perform(MockMvcRequestBuilders.get("/client")
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.terminalId").value("1001"));

    verify(terminalService, times(1)).getTerminal();
  }

  @Test
  public void getTerminal_unableToAcquireLockFromServer() throws Exception {
    doThrow(new UnableToAcquireLockException("Unable to acquire lock")).when(terminalService).getTerminal();

    mockMvc.perform(MockMvcRequestBuilders.get("/client")
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isServiceUnavailable());

    verify(terminalService, times(1)).getTerminal();
  }

  @Test
  public void getTerminal_invalidPayload() throws Exception {
    doThrow(new IllegalArgumentException("Invalid payload")).when(terminalService).getTerminal();

    mockMvc.perform(MockMvcRequestBuilders.get("/client")
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());

    verify(terminalService, times(1)).getTerminal();
  }

  @Test
  public void getTerminal_noAvailableTerminal() throws Exception {
    doThrow(new NoAvailableTerminalException("No available terminal")).when(terminalService).getTerminal();

    mockMvc.perform(MockMvcRequestBuilders.get("/client")
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isServiceUnavailable());

    verify(terminalService, times(1)).getTerminal();
  }

  @Test
  public void getTerminal_concurrentRequest() throws Exception {
    when(terminalService.getTerminal()).thenReturn(new TerminalDto("1001", 0, System.currentTimeMillis()));

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
    verify(terminalService, times(15)).getTerminal();

  }
}