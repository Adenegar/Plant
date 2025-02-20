import java.util.Queue;

/**
 * Represents a worker in the juice bottling simulation.
 * <p>
 * Each Worker performs a specific task (fetching, peeling, squeezing, bottling,
 * or processing)
 * based on its assigned {@link Plant.WorkerType}. Workers operate in a pipeline
 * by taking oranges
 * from an input queue ({@code before}) and, after processing, passing them to
 * an output queue ({@code after}).
 * </p>
 * <p>
 * The Worker class implements {@link Runnable} so that each worker can run on
 * its own thread.
 * </p>
 * 
 * Note: AI was used to generate javadocs for this file
 */
public class Worker implements Runnable {

    /**
     * Utility method that causes the current thread to sleep.
     * <p>
     * This method ensures that the thread sleeps for at least 1 millisecond. If the
     * sleep is interrupted,
     * it prints an error message.
     * </p>
     *
     * @param time   time to sleep in milliseconds (minimum 1 ms)
     * @param errMsg error message to display if interrupted
     */
    private static void delay(long time, String errMsg) {
        long sleepTime = Math.max(1, time);
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            System.err.println(errMsg);
        }
    }

    /**
     * Number of oranges required to produce one bottle of juice.
     */
    public final int ORANGES_PER_BOTTLE = 3;

    /**
     * The thread on which this Worker runs.
     */
    private final Thread thread;
    /**
     * The input queue from which the worker receives oranges.
     */
    private Queue<Orange> before;
    /**
     * The output queue to which the worker sends processed oranges.
     */
    private Queue<Orange> after;
    /**
     * Counter for the number of oranges this worker has provided.
     * <p>
     * This is mainly used by fetcher-type workers.
     * </p>
     */
    private int orangesProvided;
    /**
     * Counter for the number of oranges processed by this worker.
     */
    private int orangesProcessed;
    /**
     * The type of this worker, determining its role in the simulation.
     */
    private Plant.WorkerType type;
    /**
     * Flag to indicate whether the worker should continue processing.
     */
    private volatile boolean timeToWork;

    /**
     * Constructs a new Worker instance.
     * <p>
     * Initializes the worker with the provided parameters including the input and
     * output queues.
     * The thread is also initialized with a descriptive name for debugging
     * purposes.
     * </p>
     *
     * @param plantNum  identifier for the plant instance this worker belongs to
     * @param threadNum unique identifier for this worker thread within the plant
     * @param type      the type of worker, as defined in {@link Plant.WorkerType}
     * @param before    the input queue from which this worker will receive oranges
     * @param after     the output queue to which this worker will send processed
     *                  oranges; may be null for terminal stages
     */
    Worker(int plantNum, int threadNum, Plant.WorkerType type, Queue<Orange> before, Queue<Orange> after) {
        orangesProvided = 0;
        orangesProcessed = 0;
        this.type = type;
        this.before = before;
        this.after = after;
        thread = new Thread(this, "Worker[plant " + plantNum + ", thread " + threadNum + ", type " + type + "]");
    }

    /**
     * Starts the worker's thread and begins processing.
     */
    public void startWorker() {
        timeToWork = true;
        thread.start();
    }

    /**
     * Signals the worker to stop processing.
     */
    public void stopWorker() {
        timeToWork = false;
    }

    /**
     * Waits for the worker's thread to terminate.
     * <p>
     * If the thread is interrupted during the join, an error message is printed.
     * </p>
     */
    public void waitToStop() {
        try {
            thread.join();
        } catch (InterruptedException e) {
            System.err.println(thread.getName() + " stop malfunction");
        }
    }

    /**
     * Main processing loop for the worker.
     * <p>
     * Based on the worker type:
     * <ul>
     * <li>Fetcher-type workers create new oranges (if the output queue is not
     * overloaded) and process them.</li>
     * <li>Other worker types attempt to retrieve an orange from the input queue,
     * process it, and pass it to the output queue.</li>
     * </ul>
     * </p>
     */
    @Override
    public void run() {
        System.out.print(Thread.currentThread().getName() + " Processing oranges");
        while (timeToWork) {
            // For fetchers: generate new oranges if there is capacity in the output queue.
            if (type == Plant.WorkerType.Fetchers) {
                fetchOrange();
            } else {
                // For other worker types: attempt to retrieve an orange from the input queue.
                Orange o = checkAndRemove();
                if (o == null) {
                    delay(10, Thread.currentThread().getName() + " no oranges to process, waiting...");
                    continue;
                } else {
                    // Process the orange
                    o.runProcess();
                    // Pass the processed orange to the next stage if applicable, if this is the final state, we're currently not saving the orange to any place
                    if (after != null) {
                        synchronized (after) {
                            after.add(o);
                        }
                    }
                    orangesProcessed++;
                    System.out.print(".");
                }
            }
        }
        System.out.println("");
        System.out.println(Thread.currentThread().getName() + " Done");
    }

    /**
     * Fetches a new orange, processes it, and adds it to the output queue.
     * <p>
     * This method is exclusively used by fetcher-type workers.
     * </p>
     */
    public void fetchOrange() {
        Orange o = new Orange();
        orangesProvided++;
        // Process the orange immediately after fetching
        o.runProcess();
        synchronized (after) {
            after.add(o);
        }
        orangesProcessed++;
        System.out.print(".");
    }

    /**
     * Checks the input queue and removes an orange for processing.
     * <p>
     * The method synchronizes on the input queue to ensure thread safety.
     * </p>
     *
     * @return the next orange from the input queue, or {@code null} if the queue is
     *         empty
     */
    public Orange checkAndRemove() {
        synchronized (before) {
            if (before.isEmpty()) {
                return null;
            } else {
                return before.remove();
            }
        }
    }

    /**
     * Retrieves the type of this worker.
     *
     * @return the worker's type as defined in {@link Plant.WorkerType}
     */
    public Plant.WorkerType getType() {
        return type;
    }

    /**
     * Retrieves the total number of oranges processed by this worker.
     *
     * @return the number of oranges processed
     */
    public int getOrangesProcessed() {
        return orangesProcessed;
    }

    /**
     * Retrieves the total number of oranges provided by this worker.
     * <p>
     * This is primarily used by fetcher-type workers.
     * </p>
     *
     * @return the number of oranges provided
     */
    public int getOrangesProvided() {
        return orangesProvided;
    }
}