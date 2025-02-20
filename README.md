# Juice Bottling Plant Simulation

## Overview
This project simulates a multi-threaded juice bottling plant using Java and Apache Ant. The simulation models stages like fetching, peeling, squeezing, and bottling oranges through a pipeline of worker threads.

## Getting Started

### Clone the repository
```bash
git clone https://github.com/Adenegar/Plant
```

### Build and Run with Ant

Move your working directory to the project if you haven't already

```bash
cd Plant
```

Now run the project using ant

```bash
ant run
```

## Key Operating System Concepts

- Multithreading: Each worker runs on its own thread, demonstrating concurrent processing.
- Synchronization: Synchronized queues ensure safe communication between threads, preventing race conditions.
- Pipeline Processing:
The simulation uses a pipeline architecture where oranges pass sequentially through different processing stages, illustrating inter-thread communication and scheduling.
