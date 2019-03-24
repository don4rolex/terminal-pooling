package com.andrew.client.service;

import com.andrew.client.exception.NoAvailableTerminalException;
import com.andrew.client.exception.UnableToAcquireLockException;
import com.andrew.client.model.Terminal;
import com.andrew.client.proxy.TerminalServerProxy;
import com.andrew.client.repository.ClientTerminalRepository;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDateTime;

import static com.andrew.client.utility.TestUtil.runMultiThreaded;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author andrew
 */

@RunWith(MockitoJUnitRunner.class)
public class ClientTerminalServiceTest {

  @Rule
  public ExpectedException exceptionRule = ExpectedException.none();

  @Mock
  private ClientLockManager lockManager;

  @Mock
  private ClientTerminalRepository terminalRepository;

  @Mock
  private TerminalServerProxy terminalServerProxy;

  @InjectMocks
  private ClientTerminalServiceImpl sut;

  @Test
  public void getTerminal() {
    final var terminalId = "1001";
    final var availableTerminals = singletonList(new Terminal(terminalId, false, LocalDateTime.now()));

    when(terminalRepository.getAvailableTerminals())
        .thenReturn(availableTerminals);
    doNothing().when(lockManager).doInLock(eq(terminalId), any());

    final var terminal = sut.getTerminal();
    assertEquals(terminalId, terminal.getTerminalId());
    verify(terminalRepository, times(1)).getAvailableTerminals();
    verify(lockManager, times(1)).doInLock(eq(terminalId), any());
  }


  @Test
  public void getTerminal_unableToAcquireLockFromServer() {
    exceptionRule.expect(UnableToAcquireLockException.class);
    exceptionRule.expectMessage("Unable to acquire lock");

    final var terminalId = "1001";
    final var availableTerminals = singletonList(new Terminal(terminalId, false, LocalDateTime.now()));

    when(terminalRepository.getAvailableTerminals())
        .thenReturn(availableTerminals);

    doAnswer(invocation -> {
      terminalServerProxy.logRequest(any());
      return null;
    }).when(lockManager).doInLock(eq(terminalId), any());

    doThrow(new UnableToAcquireLockException("Unable to acquire lock")).when(terminalServerProxy).logRequest(any());

    sut.getTerminal();
    verify(terminalRepository, times(1)).getAvailableTerminals();
    verify(lockManager, times(1)).doInLock(eq(terminalId), any());
    verify(terminalServerProxy, times(1)).logRequest(any());
  }

  @Test
  public void getTerminal_invalidPayload() {
    exceptionRule.expect(IllegalArgumentException.class);
    exceptionRule.expectMessage("Invalid payload");

    final var terminalId = "1001";
    final var availableTerminals = singletonList(new Terminal(terminalId, false, LocalDateTime.now()));

    when(terminalRepository.getAvailableTerminals())
        .thenReturn(availableTerminals);

    doAnswer(invocation -> {
      terminalServerProxy.logRequest(any());
      return null;
    }).when(lockManager).doInLock(eq(terminalId), any());

    doThrow(new IllegalArgumentException("Invalid payload")).when(terminalServerProxy).logRequest(any());

    sut.getTerminal();
    verify(terminalRepository, times(1)).getAvailableTerminals();
    verify(lockManager, times(1)).doInLock(eq(terminalId), any());
    verify(terminalServerProxy, times(1)).logRequest(any());
  }

  @Test
  public void getTerminal_noAvailableTerminal() {
    exceptionRule.expect(NoAvailableTerminalException.class);
    exceptionRule.expectMessage("No available terminal");

    when(terminalRepository.getAvailableTerminals())
        .thenThrow(new NoAvailableTerminalException("No available terminal"));

    sut.getTerminal();
    verify(terminalRepository, times(1)).getAvailableTerminals();
  }

  @Test
  public void getTerminal_concurrentRequest() throws Exception {
    final var terminalId = "1001";
    final var availableTerminals = singletonList(new Terminal(terminalId, false, LocalDateTime.now()));

    when(terminalRepository.getAvailableTerminals())
        .thenReturn(availableTerminals);
    doNothing().when(lockManager).doInLock(eq(terminalId), any());

    runMultiThreaded(() -> {
          final var terminalOne = sut.getTerminal();
          assertNotNull(terminalOne.getTerminalId());

          final var terminalTwo = sut.getTerminal();
          assertNotNull(terminalTwo.getTerminalId());

          final var terminalThree = sut.getTerminal();
          assertNotNull(terminalThree.getTerminalId());
        },
        5);

    verify(terminalRepository, times(15)).getAvailableTerminals();
    verify(lockManager, times(15)).doInLock(eq(terminalId), any());
  }

}