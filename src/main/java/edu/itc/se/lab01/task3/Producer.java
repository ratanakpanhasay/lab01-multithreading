
package edu.itc.se.lab01.task3;

public class Producer implements Runnable {

    private final OrderQueue queue;
    private final int producerId;
    private final int orderCount;

    public Producer(OrderQueue queue, int producerId, int orderCount) {
        this.queue = queue;
        this.producerId = producerId;
        this.orderCount = orderCount;
    }

    @Override
    public void run() {
        try {
            for (int i = 0; i < orderCount; i++) {
                int id = producerId * 1000 + i;
                Order order = new Order(id, "item-" + id, 1);

                queue.put(order);

                System.out.println(
                        "Producer-" + producerId + " produced " + order);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
