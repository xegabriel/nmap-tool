package ro.gabe.nmap_processor.helpers;

import java.util.concurrent.TimeUnit;

public class ExponentialBackoffHelper {

  private final int maxRetries;
  private final int initialBackoffMilliseconds;

  public ExponentialBackoffHelper(int maxRetries, int initialBackoffMilliseconds) {
    this.maxRetries = maxRetries;
    this.initialBackoffMilliseconds = initialBackoffMilliseconds;
  }

  public interface RetryableOperation<T> {
    T execute() throws Exception;
  }

  public <T> T retry(RetryableOperation<T> operation) throws Exception {
    int attempt = 0;
    while (attempt < maxRetries) {
      try {
        return operation.execute();
      } catch (Exception e) {
        attempt++;
        if (attempt >= maxRetries) {
          throw e;
        }

        long backoffTime = initialBackoffMilliseconds * (long) Math.pow(2, attempt - 1);
        System.out.println("Retrying operation... Attempt: " + attempt + ", backoff time: " + backoffTime + " ms");

        try {
          TimeUnit.MILLISECONDS.sleep(backoffTime);
        } catch (InterruptedException ie) {
          Thread.currentThread().interrupt();
          throw new RuntimeException("Retry interrupted", ie);
        }
      }
    }
    throw new RuntimeException("Operation failed after " + maxRetries + " attempts");
  }
}
