
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



## Task 3 — Producer–Consumer

### Configuration

- Producers: 2
- Consumers: 3
- Total orders: 100
- Buffer capacity: 5

### Implementation 1: BoundedBuffer

Implemented a bounded buffer using ArrayDeque, synchronized
methods, wait(), and notifyAll().

A producer waits while the buffer is full. A consumer waits
while the buffer is empty. The conditions are checked using
while loops to ensure they are re-evaluated after waking up.

Poison pills with order ID -1 signal the consumers to stop.

### Implementation 2: ArrayBlockingQueue

Implemented the same OrderQueue interface using Java's
ArrayBlockingQueue. The built-in queue handles waiting and
coordination when full or empty.

### Verification

Both implementations completed successfully:
- 100 orders produced
- 100 orders processed
- 3 consumers stopped

### Comparison

BoundedBuffer demonstrates how synchronization and condition
waiting can be implemented manually. ArrayBlockingQueue provides
a ready-made, thread-safe bounded queue with the coordination
logic handled internally.



## Task 4 — Thread Pools, Futures and Virtual Threads

### Part 1: Prime Counting
- Counted primes below 4,000,000 sequentially and in parallel.
- Split the range into 16 chunks.
- Used Callable, ExecutorService and Future.
- Both approaches returned 283,146 primes.

### Part 2: Simulated Downloads
- Submitted 500 download tasks.
- Compared a fixed pool of 20 platform threads with virtual threads.
- Handled 10 IOException failures without crashing.
- Tested a 1-second timeout and cancelled the slow task.

### Part 3: CompletableFuture Bonus
- Used supplyAsync() to execute downloads asynchronously.
- Used allOf() to wait for all tasks.
- Used handle() to convert failed tasks into fallback strings.
- Verified 490 successful and 10 failed downloads.

### Key Findings
Virtual threads performed well for the simulated I/O-bound workload.
CompletableFuture provided another way to compose asynchronous
tasks and handle errors. Execution times depend on the machine.
