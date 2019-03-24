package com.andrew.server.service;

import com.andrew.server.dto.Payload;
import com.andrew.server.exception.InvalidSequenceNumberException;
import com.andrew.server.exception.InvalidTerminalIdException;
import com.andrew.server.model.Log;
import com.andrew.server.repository.LogRepository;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static com.andrew.server.utility.TestUtil.runMultiThreaded;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author andrew
 */
@RunWith(MockitoJUnitRunner.class)
public class ServerTerminalServiceTest {

  @Rule
  public ExpectedException exceptionRule = ExpectedException.none();

  @Mock
  private ServerLockManager lockManager;

  @Mock
  private LogRepository logRepository;

  @InjectMocks
  private ServerTerminalServiceImpl sut;

  @Test
  public void logRequest() {
    final var terminalId = "1001";
    final var payload = new Payload(terminalId, 0, System.currentTimeMillis());

    when(logRepository.save(any())).thenReturn(new Log());

    doAnswer(invocation -> {
      logRepository.save(any());
      return null;
    }).when(lockManager).doInLock(eq(terminalId), any());

    sut.logRequest(payload);
    verify(lockManager, times(1)).doInLock(eq(terminalId), any());
    verify(logRepository, times(1)).save(any());
  }

  @Test
  public void logRequest_invalidTerminalId() {
    exceptionRule.expect(InvalidTerminalIdException.class);
    exceptionRule.expectMessage("Invalid terminal ID");

    final var terminalId = "1007";
    final var payload = new Payload(terminalId, 0, System.currentTimeMillis());
    doThrow(new InvalidTerminalIdException("Invalid terminal ID")).when(lockManager).doInLock(eq(terminalId), any());

    sut.logRequest(payload);
    verify(lockManager, times(1)).doInLock(eq(terminalId), any());
  }

  @Test
  public void logRequest_invalidSequence() throws Exception {
    exceptionRule.expect(InvalidSequenceNumberException.class);
    exceptionRule.expectMessage("Invalid sequence number");

    final var terminalId = "1004";
    final var payload = new Payload(terminalId, 8, System.currentTimeMillis());
    doThrow(new InvalidSequenceNumberException("Invalid sequence number")).when(lockManager).doInLock(eq(terminalId), any());

    sut.logRequest(payload);
    verify(lockManager, times(1)).doInLock(eq(terminalId), any());
  }

  @Test
  public void logRequest_concurrentTerminalId() throws Exception {
    final var terminalId = "1001";
    final var payload = new Payload(terminalId, 0, System.currentTimeMillis());

    when(logRepository.save(any())).thenReturn(new Log());

    doAnswer(invocation -> {
      logRepository.save(any());
      return null;
    }).when(lockManager).doInLock(eq(terminalId), any());

    runMultiThreaded(() -> {
          try {
            sut.logRequest(payload);

            sut.logRequest(payload);
          } catch (Exception e) {
            e.printStackTrace();
          }
        },
        5);

    verify(lockManager, times(10)).doInLock(eq(terminalId), any());
    verify(logRepository, times(10)).save(any());
  }

}