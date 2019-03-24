package com.andrew.client.service;

import com.andrew.client.exception.UnableToAcquireLockException;
import com.andrew.client.repository.ClientTerminalRepository;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author andrew
 */
@Service
public class ClientLockManagerImpl implements ClientLockManager {

  private final ClientTerminalRepository terminalRepository;

  private final Map<String, ReentrantLock> locks = new ConcurrentHashMap<>();

  public ClientLockManagerImpl(ClientTerminalRepository terminalRepository) {
    this.terminalRepository = terminalRepository;
  }

  @Override
  public void createLock(String terminalId) {
    locks.putIfAbsent(terminalId, new ReentrantLock());
  }

  @Override
  public void removeLock(String terminalId) {
    locks.remove(terminalId);
  }

  @Override
  public void doInLock(String terminalId, Runnable action) {
    createLock(terminalId);
    final var lock = locks.get(terminalId);
    try {
      if (lock.tryLock(30, TimeUnit.SECONDS)) {
        action.run();
      } else {
        throw new UnableToAcquireLockException(String.format("Unable to acquire lock for %s", terminalId));
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    } finally {
      lock.unlock();
      removeLock(terminalId);
      terminalRepository.setInUse(terminalId, false);
    }
  }
}