
package edu.itc.se.lab01.task3;

public class Consumer implements Runnable {

    private final OrderQueue queue;
    private final int consumerId;

    public Consumer(OrderQueue queue, int consumerId) {
        this.queue = queue;
        this.consumerId = consumerId;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Order order = queue.take();

                if (order.id() == -1) {
                    System.out.println(
                            "Consumer-" + consumerId + " stopping");
                    break;
                }

                System.out.println(
                        "Consumer-" + consumerId + " processed " + order);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
