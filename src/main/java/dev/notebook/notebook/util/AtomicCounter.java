package dev.notebook.notebook.util;

import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;

@Component
public class AtomicCounter {

  private final AtomicLong counter = new AtomicLong();

  public long incrementAndGet() {
    return counter.incrementAndGet();
  }

  public long get() {
    return counter.get();
  }
}
