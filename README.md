
# Lab 01 — Multithreading in Java

## Environment

- Java: 21
- Build tool: Apache Maven 3.10.0
- Project: lab01-multithreading

## Task 1 — Creating and Running Threads

Created four threads using:
- Extending Thread
- Implementing Runnable
- Lambda expression
- Virtual thread builder

Tested concurrent execution with start() and sequential execution with run().

Used join() to wait for threads to finish.

In concurrent mode, all four threads terminated successfully. Only the
artemis thread was virtual.

## Task 2 — Fixing a Race Condition

### Configuration

- Threads: 8
- Increments per thread: 1,000,000
- Expected counter value: 8,000,000

### Benchmark results

| Implementation | Final value | Correct | Time (ms) |
|---|---:|---|---:|
| UnsafeCounter #1 | 4,003,662 | No | 40 |
| UnsafeCounter #2 | 7,944,001 | No | 0 |
| UnsafeCounter #3 | 8,000,000 | Yes | 1 |
| SynchronizedCounter | 8,000,000 | Yes | 240 |
| LockCounter | 8,000,000 | Yes | 226 |
| AtomicCounter | 8,000,000 | Yes | 146 |
| LongAdderCounter | 8,000,000 | Yes | 29 |

Timings are from one run and can vary between executions.

### Explanation

UnsafeCounter uses count++, which is not atomic. Concurrent threads
can overwrite one another's updates, causing a lost-update race condition.

SynchronizedCounter protects access using synchronized methods.

LockCounter uses ReentrantLock and releases it in a finally block.

AtomicCounter uses AtomicLong to increment the counter atomically.

LongAdderCounter distributes updates across internal cells, which can
reduce contention when many threads increment concurrently.

Synchronizing only increment() does not coordinate an unsynchronized
get() through the same monitor. Synchronizing both methods provides
the corresponding synchronization and visibility guarantees.
