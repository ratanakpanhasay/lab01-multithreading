
package edu.itc.se.lab01.task1;

import java.util.List;

public class Main {

    /*
     * start() creates a new thread to execute run().
     * run() executes directly on the current thread.
     * Therefore, start() allows concurrent execution,
     * while calling run() directly executes sequentially.
     */

    public static void main(String[] args)
            throws InterruptedException {

        boolean sequential = args.length > 0
                && args[0].equals("--sequential");

        // Method 1: extends Thread
        Thread apollo = new Countdown("apollo");

        // Method 2: implements Runnable
        Thread gemini = new Thread(   //create thread and excecute
                new CountdownTask(), "gemini");  // create task

        // Method 3: lambda expression
        Thread mercury = new Thread(  // create thread and excecute the task
                () -> new CountdownTask().run(), "mercury");  // labda exucute run of new CountdownTask

        // Method 4: virtual thread
        Thread artemis = Thread.ofVirtual()  // create a builder for virtual threads
                .name("artemis")  // thread name
                .unstarted(new CountdownTask()); // create thread object but does not start it

        List<Thread> threads = List.of(
                apollo, gemini, mercury, artemis);

        // Start or run all four threads
        for (Thread thread : threads) {
            if (sequential) {
                thread.run();
            } else {
                thread.start();
            }
        }

        System.out.println("Mission control: all launched");

        // Wait for all threads to finish
        for (Thread thread : threads) {
            thread.join();
        }

        System.out.println("Mission control: all landed");

        // Print thread information
        for (Thread thread : threads) {
            System.out.println(
                    thread.getName()
                    + " virtual=" + thread.isVirtual()
                    + " state=" + thread.getState()
            );
        }
    }
}
