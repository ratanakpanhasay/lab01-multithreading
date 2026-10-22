
package edu.itc.se.lab01.task5;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

public class DeadlockWatchdog implements Runnable {

    @Override
    public void run() {
        ThreadMXBean bean =
                ManagementFactory.getThreadMXBean();

        while (!Thread.currentThread().isInterrupted()) {
            long[] ids = bean.findDeadlockedThreads();

            if (ids != null) {
                ThreadInfo[] infos = bean.getThreadInfo(ids);

                System.out.print("[watchdog] DEADLOCK detected between: ");

                for (int i = 0; i < infos.length; i++) {
                    if (i > 0) {
                        System.out.print(", ");
                    }

                    System.out.print(infos[i].getThreadName());
                }

                System.out.println();
                return;
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
}
