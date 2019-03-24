package com.andrew.server.utility;

import java.util.LinkedList;

/**
 * @author andrew
 */
public final class TestUtil {

  private TestUtil() {
  }

  public static void runMultiThreaded(Runnable runnable, int threadCount) throws InterruptedException {
    final var threadList = new LinkedList<Thread>();

    for (int i = 0; i < threadCount; i++) {
      threadList.add(new Thread(runnable));
    }

    for (Thread t : threadList) {
      t.start();
    }

    for (Thread t : threadList) {
      t.join();
    }
  }
}
