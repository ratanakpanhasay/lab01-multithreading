
package edu.itc.se.lab01.task5;

import java.util.concurrent.ThreadLocalRandom;

public class OrderedBank {

    static final class Account {
        final int id;
        private long balance;

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

    static void transfer(
            Account a, Account b, long amount) {

        Account first = a.id < b.id ? a : b;
        Account second = a.id < b.id ? b : a;

        synchronized (first) {
            synchronized (second) {
                if (a.balance() >= amount) {
                    a.withdraw(amount);
                    b.deposit(amount);
                }
            }
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

        long start = System.nanoTime();

        for (int w = 0; w < workersCount; w++) {
            workers[w] = new Thread(() -> {
                var random = ThreadLocalRandom.current();

                for (int i = 0; i < transfersPerWorker; i++) {
                    Account a =
                            accounts[random.nextInt(accounts.length)];
                    Account b =
                            accounts[random.nextInt(accounts.length)];

                    if (a != b) {
                        transfer(a, b, random.nextInt(1, 50));
                    }
                }
            }, "ordered-transfer-" + w);

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
                "transfers=%d total=%d (expected 4000) in %d ms%n",
                workersCount * transfersPerWorker,
                total,
                elapsed);
    }
}
