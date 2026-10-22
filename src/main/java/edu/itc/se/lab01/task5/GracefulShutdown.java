
package edu.itc.se.lab01.task5;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

public class GracefulShutdown {

    public static void main(String[] args)
            throws InterruptedException {

        TryLockBank.Account[] accounts =
                new TryLockBank.Account[4];

        for (int i = 0; i < accounts.length; i++) {
            accounts[i] = new TryLockBank.Account(i, 1_000);
        }

        int workerCount = 8;
        Thread[] workers = new Thread[workerCount];
        AtomicLong completed = new AtomicLong();

        TryLockBank.retries.set(0);

        for (int w = 0; w < workerCount; w++) {
            workers[w] = new Thread(() -> {
                var random = ThreadLocalRandom.current();

                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        TryLockBank.Account a =
                                accounts[random.nextInt(accounts.length)];
                        TryLockBank.Account b =
                                accounts[random.nextInt(accounts.length)];

                        if (a != b) {
                            TryLockBank.transfer(
                                    a, b, random.nextInt(1, 50));

                            completed.incrementAndGet();
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "transfer-" + w);

            workers[w].start();
        }

        Thread.sleep(2_000);

        System.out.println(
                "[main] interrupting " + workerCount
                        + " workers after 2000 ms");

        for (Thread worker : workers) {
            worker.interrupt();
        }

        for (Thread worker : workers) {
            worker.join(1_000);
        }

        boolean allStopped = true;

        for (Thread worker : workers) {
            if (worker.isAlive()) {
                allStopped = false;
                System.out.println(
                        worker.getName() + " still running");
            }
        }

        long total = 0;

        for (TryLockBank.Account account : accounts) {
            total += account.balance();
        }

        System.out.println(
                "completed transfers=" + completed.get());
        System.out.println(
                "all workers stopped=" + allStopped
                        + " total=" + total + " (expected 4000)");
    }
}
