package com.andrew.server.service;

/**
 * @author andrew
 */
public interface ServerLockManager {

  void createLock(String terminalId);

  void removeLock(String terminalId);

  void doInLock(String terminalId, Runnable action);
}
