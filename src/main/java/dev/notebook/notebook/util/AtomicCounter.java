package dev.notebook.notebook.util;

import java.util.concurrent.atomic.AtomicLong;

public class AtomicCounter {

  private final AtomicLong counter = new AtomicLong();

  public void increment() {
    counter.incrementAndGet();
  }

  public long get() {
    return counter.get();
  }
}
