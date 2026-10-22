
package edu.itc.se.lab01.task5;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

public class TryLockBank {

    static final class Account {
        final int id;
        private long balance;
        final ReentrantLock lock = new ReentrantLock();

        Account(int id, long balance) {
            this.id = id;
            this.balance = balance;
        }

        long balance() {
            return balance;
        }

        void withdraw(long amount) {
            balance -= amount;
        }

        void deposit(long amount) {
            balance += amount;
        }
    }

    static final AtomicLong retries = new AtomicLong();

    static void backoff() throws InterruptedException {
        Thread.sleep(ThreadLocalRandom.current().nextInt(1, 5));
    }

    static void transfer(
            Account from, Account to, long amount)
            throws InterruptedException {

        while (true) {
            boolean firstLocked = false;

            try {
                firstLocked = from.lock.tryLock(
                        10, TimeUnit.MILLISECONDS);

                if (!firstLocked) {
                    retries.incrementAndGet();
                    backoff();
                    continue;
                }

                boolean secondLocked = false;

                try {
                    secondLocked = to.lock.tryLock(
                            10, TimeUnit.MILLISECONDS);

                    if (!secondLocked) {
                        retries.incrementAndGet();
                    } else {
                        if (from.balance() >= amount) {
                            from.withdraw(amount);
                            to.deposit(amount);
                        }

                        return;
                    }
                } finally {
                    if (secondLocked) {
                        to.lock.unlock();
                    }
                }
            } finally {
                if (firstLocked) {
                    from.lock.unlock();
                }
            }

            backoff();
        }
    }

    public static void main(String[] args)
            throws InterruptedException {

        Account[] accounts = new Account[4];

        for (int i = 0; i < accounts.length; i++) {
            accounts[i] = new Account(i, 1_000);
        }

        int workersCount = 8;
        int transfersPerWorker = 100_000;
        Thread[] workers = new Thread[workersCount];

        retries.set(0);
        long start = System.nanoTime();

        for (int w = 0; w < workersCount; w++) {
            workers[w] = new Thread(() -> {
                var random = ThreadLocalRandom.current();

                try {
                    for (int i = 0; i < transfersPerWorker; i++) {
                        Account a =
                                accounts[random.nextInt(accounts.length)];
                        Account b =
                                accounts[random.nextInt(accounts.length)];

                        if (a != b) {
                            transfer(a, b, random.nextInt(1, 50));
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "trylock-transfer-" + w);

            workers[w].start();
        }

        for (Thread worker : workers) {
            worker.join();
        }

        long total = 0;

        for (Account account : accounts) {
            total += account.balance();
        }

        long elapsed = (System.nanoTime() - start) / 1_000_000;

        System.out.printf(
                "transfers=%d retries=%d total=%d (expected 4000) in %d ms%n",
                workersCount * transfersPerWorker,
                retries.get(),
                total,
                elapsed);
    }
}
