package dev.notebook.notebook.util;

public class NonAtomicCounter {

  private long counter;

  public void increment() {
    counter++;
  }

  public long get() {
    return counter;
  }
}
