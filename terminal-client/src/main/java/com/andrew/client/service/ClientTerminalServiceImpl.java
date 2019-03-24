package com.andrew.client.service;

import com.andrew.client.dto.Payload;
import com.andrew.client.dto.TerminalDto;
import com.andrew.client.exception.NoAvailableTerminalException;
import com.andrew.client.exception.UnableToAcquireLockException;
import com.andrew.client.proxy.TerminalServerProxy;
import com.andrew.client.repository.ClientTerminalRepository;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author andrew
 */
@Service
public class ClientTerminalServiceImpl implements ClientTerminalService {

  @Value("${sequence.no.start}")
  private int sequenceNoStart;

  @Value("${sequence.no.end}")
  private int sequenceNoEnd;

  @Value("${response.sleep.duration}")
  private long responseSleepDuration;

  @Value("${max.wait.time}")
  private long maxWaitTime;

  private final ClientLockManager lockManager;
  private final ClientTerminalRepository terminalRepository;
  private final TerminalServerProxy terminalServerProxy;

  private final AtomicInteger sequenceNoCounter = new AtomicInteger(sequenceNoStart);

  public ClientTerminalServiceImpl(ClientLockManager lockManager,
                                   ClientTerminalRepository terminalRepository,
                                   TerminalServerProxy terminalServerProxy) {
    this.lockManager = lockManager;
    this.terminalRepository = terminalRepository;
    this.terminalServerProxy = terminalServerProxy;
  }

  public TerminalDto getTerminal() {
    final var payload = buildPayload(LocalDateTime.now());

    lockManager.doInLock(payload.getTerminalId(), () -> {
      try {
        terminalServerProxy.logRequest(payload);
      } catch (FeignException e) {
        handleFeignException(e, payload);
      }
    });

    return new TerminalDto(
        payload.getTerminalId(),
        payload.getSequenceNo(),
        payload.getTimestamp()
    );
  }

  private Payload buildPayload(LocalDateTime timeOfRequest) {
    var sequenceNo = sequenceNoCounter.getAndIncrement();
    if (sequenceNo > sequenceNoEnd) {
      sequenceNo = sequenceNoStart;
      sequenceNoCounter.set(sequenceNoStart);
    }

    final var terminalId = getAvailableTerminalId(timeOfRequest);

    return new Payload(terminalId, sequenceNo, System.currentTimeMillis());
  }

  private String getAvailableTerminalId(LocalDateTime timeOfRequest) {
    final var terminalId = getTerminalId();
    if (terminalId == null) {
      try {
        Thread.sleep(responseSleepDuration);

        final var currentTime = LocalDateTime.now();
        final var waitTime = Duration.between(timeOfRequest, currentTime).getSeconds();
        if (waitTime > maxWaitTime) {
          throw new NoAvailableTerminalException(String.format("No available terminal after %d seconds", maxWaitTime));
        }

        getAvailableTerminalId(timeOfRequest);
      } catch (InterruptedException ignored) {
      }
    }

    return terminalId;
  }

  private synchronized String getTerminalId() {
    final var availableTerminals = terminalRepository.getAvailableTerminals();
    if (availableTerminals != null && availableTerminals.size() > 0) {
      final var terminalId = availableTerminals.get(0).getId();
      terminalRepository.setInUse(terminalId, true);

      return terminalId;
    }

    return null;
  }

  private void handleFeignException(FeignException e, Payload payload) {
    final var status = e.status();

    if (HttpStatus.SERVICE_UNAVAILABLE.value() == status) {
      throw new UnableToAcquireLockException(String.format("Unable to acquire lock for %s", payload.getTerminalId()));
    }

    if (HttpStatus.BAD_REQUEST.value() == status) {
      throw new IllegalArgumentException("Invalid payload");
    }
  }
}
