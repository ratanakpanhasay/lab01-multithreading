
package edu.itc.se.lab01.task4;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Main {

    static long countPrimes(long from, long to) {
        long count = 0;

        for (long n = Math.max(from, 2); n < to; n++) {
            boolean prime = true;

            for (long d = 2; d * d <= n; d++) {
                if (n % d == 0) {
                    prime = false;
                    break;
                }
            }

            if (prime) {
                count++;
            }
        }

        return count;
    }

    static class PrimeCounter
            implements java.util.concurrent.Callable<Long> {

        private final long from;
        private final long to;

        PrimeCounter(long from, long to) {
            this.from = from;
            this.to = to;
        }

        @Override
        public Long call() {
            return countPrimes(from, to);
        }
    }

    static void runDownloads(boolean virtual)
            throws InterruptedException {

        int total = 500;
        int ok = 0;
        int failed = 0;
        int timedOut = 0;

        long start = System.nanoTime();

        try (ExecutorService executor = virtual
                ? Executors.newVirtualThreadPerTaskExecutor()
                : Executors.newFixedThreadPool(20)) {

            List<Future<String>> futures = new ArrayList<>();

            for (int i = 1; i <= total; i++) {
                boolean shouldFail = i % 50 == 0;
                boolean shouldBeSlow = i == 1;

                FakeDownload task = new FakeDownload(
                        "https://example.com/file-" + i,
                        shouldFail,
                        shouldBeSlow);

                futures.add(executor.submit(task));
            }

            // Test the slow task first.
            try {
                futures.get(0).get(1, TimeUnit.SECONDS);
                ok++;
            } catch (TimeoutException e) {
                timedOut++;
                futures.get(0).cancel(true);
            } catch (ExecutionException e) {
                failed++;
            }

            // Collect the other 499 results.
            for (int i = 1; i < futures.size(); i++) {
                try {
                    futures.get(i).get();
                    ok++;
                } catch (ExecutionException e) {
                    failed++;
                    System.out.println(
                            "Download " + (i + 1) + " failed: "
                                    + e.getCause().getMessage());
                }
            }
        }

        long elapsed =
                (System.nanoTime() - start) / 1_000_000;

        String mode = virtual
                ? "virtual threads"
                : "fixed(20) platform threads";

        System.out.printf(
                "downloads on %s: ok=%d failed=%d%n",
                mode, ok, failed);

        System.out.printf(
                "timedOut=%d in %d ms%n", timedOut, elapsed);
    }

    
static void runCompletableFutureDemo() {
    int total = 500;
    int ok = 0;
    int failed = 0;

    long start = System.nanoTime();

    try (ExecutorService executor =
            Executors.newVirtualThreadPerTaskExecutor()) {

        List<CompletableFuture<String>> futures =
                new ArrayList<>();

        for (int i = 1; i <= total; i++) {
            final int id = i;

            FakeDownload task = new FakeDownload(
                    "https://example.com/file-" + id,
                    id % 50 == 0,
                    false);

            CompletableFuture<String> future =
                    CompletableFuture.supplyAsync(() -> {
                        try {
                            return task.call();
                        } catch (Exception e) {
                            throw new java.util.concurrent.CompletionException(e);
                        }
                    }, executor).handle((result, error) -> {
                        if (error != null) {
                            return "FAILED: " + id;
                        }

                        return result;
                    });

            futures.add(future);
        }

        CompletableFuture.allOf(
                futures.toArray(new CompletableFuture<?>[0])
        ).join();

        for (CompletableFuture<String> future : futures) {
            if (future.join().startsWith("FAILED:")) {
                failed++;
            } else {
                ok++;
            }
        }
    }

    long elapsed = (System.nanoTime() - start) / 1_000_000;

    System.out.printf(
            "CompletableFuture: ok=%d failed=%d in %d ms%n",
            ok, failed, elapsed);
}


    public static void main(String[] args)
            throws Exception {

        final long LIMIT = 4_000_000;
        final int CHUNKS = 16;

        // Part 1: Single-threaded prime counting
        long start = System.nanoTime();

        long single = countPrimes(0, LIMIT);

        System.out.printf(
                "single-threaded: %d primes in %d ms%n",
                single,
                (System.nanoTime() - start) / 1_000_000);

        // Part 1: Parallel prime counting
        start = System.nanoTime();

        long total = 0;

        try (ExecutorService pool =
                Executors.newFixedThreadPool(
                        Runtime.getRuntime().availableProcessors())) {

            List<Future<Long>> futures = new ArrayList<>();

            for (int i = 0; i < CHUNKS; i++) {
                long from = (long) i * LIMIT / CHUNKS;
                long to = (long) (i + 1) * LIMIT / CHUNKS;

                futures.add(pool.submit(
                        new PrimeCounter(from, to)));
            }

            for (Future<Long> future : futures) {
                total += future.get();
            }
        }

        System.out.printf(
                "pool: %d primes in %d ms%n",
                total,
                (System.nanoTime() - start) / 1_000_000);

        System.out.println("Results match: " + (single == total));

        // Part 2: Platform threads versus virtual threads
        runDownloads(false);
        runDownloads(true);
        runCompletableFutureDemo();
        
    }

    
}
