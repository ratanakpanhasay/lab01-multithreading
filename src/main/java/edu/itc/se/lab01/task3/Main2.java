
package edu.itc.se.lab01.task3;

import java.util.ArrayList;
import java.util.List;

public class Main2 {

    public static void main(String[] args)
            throws InterruptedException {

        int producerCount = 2;
        int consumerCount = 3;
        int totalOrders = 100;
        int capacity = 5;

        OrderQueue queue = new BlockingOrderQueue(capacity);

        List<Thread> consumers = new ArrayList<>();

        for (int i = 1; i <= consumerCount; i++) {
            Thread consumer = new Thread(
                    new Consumer(queue, i), "consumer-" + i);

            consumers.add(consumer);
            consumer.start();
        }

        List<Thread> producers = new ArrayList<>();
        int ordersPerProducer = totalOrders / producerCount;

        for (int i = 1; i <= producerCount; i++) {
            Thread producer = new Thread(
                    new Producer(queue, i, ordersPerProducer),
                    "producer-" + i);

            producers.add(producer);
            producer.start();
        }

        for (Thread producer : producers) {
            producer.join();
        }

        for (int i = 0; i < consumerCount; i++) {
            queue.put(new Order(-1, "STOP", 0));
        }

        for (Thread consumer : consumers) {
            consumer.join();
        }

        System.out.println(
                "All producers and consumers finished using ArrayBlockingQueue.");
    }
}
