package com.andrew.server.service;

import com.andrew.server.dto.Payload;
import com.andrew.server.exception.InvalidSequenceNumberException;
import com.andrew.server.exception.InvalidTerminalIdException;
import com.andrew.server.model.Log;
import com.andrew.server.repository.LogRepository;
import com.andrew.server.repository.ServerTerminalRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * @author andrew
 */
@Service
public class ServerTerminalServiceImpl implements ServerTerminalService {

  @Value("${sequence.no.start}")
  private int sequenceNoStart;

  @Value("${sequence.no.end}")
  private int sequenceNoEnd;

  @Value("${response.sleep.duration}")
  private long responseSleepDuration;

  private final ServerLockManager lockManager;
  private final ServerTerminalRepository terminalRepository;
  private final LogRepository logRepository;

  public ServerTerminalServiceImpl(ServerLockManager lockManager, ServerTerminalRepository terminalRepository, LogRepository logRepository) {
    this.lockManager = lockManager;
    this.terminalRepository = terminalRepository;
    this.logRepository = logRepository;
  }

  public void logRequest(Payload payload) {
    lockManager.doInLock(payload.getTerminalId(), () -> {
      verifyPayload(payload);
      logRepository.save(payloadToLog(payload));

      try {
        Thread.sleep(responseSleepDuration);
      } catch (InterruptedException ignored) {
      }
    });
  }

  private void verifyPayload(Payload payload) {
    final var sequenceNo = payload.getSequenceNo();
    if (sequenceNo < sequenceNoStart || sequenceNo > sequenceNoEnd) {
      throw new InvalidSequenceNumberException(String.format("Invalid sequence number %d", sequenceNo));
    }
    final var terminalId = payload.getTerminalId();
    terminalRepository.findById(terminalId)
        .orElseThrow(() -> new InvalidTerminalIdException(String.format("Invalid terminalId %s", terminalId)));
  }

  private Log payloadToLog(Payload payload) {
    final var log = new Log();
    log.setTerminalId(payload.getTerminalId());
    log.setSequenceNo(payload.getSequenceNo());
    log.setCreated(LocalDateTime.now());

    return log;
  }
}
