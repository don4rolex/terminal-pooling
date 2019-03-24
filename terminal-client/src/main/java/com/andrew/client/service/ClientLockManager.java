package com.andrew.client.service;

/**
 * @author andrew
 */
public interface ClientLockManager {

  void createLock(String terminalId);

  void removeLock(String terminalId);

  void doInLock(String terminalId, Runnable action);
}
