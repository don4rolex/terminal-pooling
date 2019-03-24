package com.andrew.server.service;

import com.andrew.server.exception.UnableToAcquireLockException;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author andrew
 */
@Service
public class ServerLockManagerImpl implements ServerLockManager {
  private final Map<String, ReentrantLock> locks = new ConcurrentHashMap<>();

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
    }
  }
}