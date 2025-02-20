import java.util.Map;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Represents a juice bottling plant simulation.
 * <p>
 * A Plant is composed of several Worker threads, each corresponding to a stage
 * in the processing
 * pipeline: fetching, peeling, squeezing, bottling, and processing. Oranges
 * flow through these stages,
 * and the plant aggregates performance metrics at the end of the simulation.
 * </p>
 * 
 * Note: AI was used to generate javadocs for this file
 */
public class Plant implements Runnable {
    /**
     * Total processing time for which the juice processing should run (in
     * milliseconds).
     */
    public static final long PROCESSING_TIME = 5 * 1000;

    /**
     * The number of plant instances to simulate.
     */
    private static final int NUM_PLANTS = 3;

    /**
     * Enumeration representing the different types of workers/stations in the
     * plant.
     */
    public static enum WorkerType {
        Fetchers,
        Peelers,
        Squeezers,
        Bottlers
    }

    /**
     * Number of fetcher workers. (Note: Instantiation time for fetchers is
     * non-negligible, which is why our number of fetchers doesn't line up with the
     * listed processing time)
     */
    private static final int NUMBER_OF_FETCHERS = 5;
    /**
     * Number of peeler workers.
     */
    private static final int NUMBER_OF_PEELERS = 5;
    /**
     * Number of squeezer workers.
     */
    private static final int NUMBER_OF_SQUEEZERS = 3;
    /**
     * Number of bottler workers.
     */
    private static final int NUMBER_OF_BOTTLERS = 1;

    /**
     * Mapping from WorkerType to the number of workers assigned to that station.
     */
    private static Map<WorkerType, Integer> workersPerStation = Map.of(
            WorkerType.Fetchers, NUMBER_OF_FETCHERS,
            WorkerType.Peelers, NUMBER_OF_PEELERS,
            WorkerType.Squeezers, NUMBER_OF_SQUEEZERS,
            WorkerType.Bottlers, NUMBER_OF_BOTTLERS);

    /**
     * Main method to start the juice bottling simulation.
     * <p>
     * It initializes multiple Plant instances, starts their workers, runs the
     * simulation for the defined
     * processing time, and then stops the plants and workers. Finally, it
     * summarizes the simulation results.
     * </p>
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        // Startup the plants and workers
        Plant[] plants = new Plant[NUM_PLANTS];
        for (int i = 0; i < NUM_PLANTS; i++) {
            plants[i] = new Plant(i);
            plants[i].startPlant();
            Worker[] workers = plants[i].getWorkers();
            for (Worker w : workers) {
                w.startWorker();
            }
        }

        // Give the plants time to do work
        delay(PROCESSING_TIME, "Plant malfunction");

        // Stop the plants/workers
        for (Plant p : plants) {
            p.stopPlant();
            Worker[] workers = p.getWorkers();
            for (Worker w : workers) {
                w.stopWorker();
            }
        }

        // Wait for shutdown. Separating the stop and wait reduces post-simulation orange processing
        for (Plant p : plants) {
            p.waitToStop();
            Worker[] workers = p.getWorkers();
            for (Worker w : workers) {
                w.waitToStop();
            }
        }

        // Summarize the results
        int totalProvided = 0;
        int totalProcessed = 0;
        int totalBottles = 0;
        int totalWasted = 0;
        for (Plant p : plants) {
            totalProvided += p.getProvidedOranges();
            totalProcessed += p.getProcessedOranges();
            totalBottles += p.getBottles();
            totalWasted += p.getWaste();
        }
        System.out.println("----------------------------------------");
        System.out.println("Simulation results:");
        System.out.println("Total provided/processed = " + totalProvided + "/" + totalProcessed);
        System.out.println("Created " + totalBottles + " bottles" +
                ", wasted " + totalWasted + " oranges");
        // Print the number of oranges left in each queue for each plan
        int plantIndex = 0;
        for (Plant p : plants) {
            System.out.println("----------------------------------------");
            System.out.println("Plant " + plantIndex++);
            for (WorkerType type : p.workerQueues.keySet()) {
                if (type == WorkerType.Fetchers) { // Fetchers create the orange instances, so they don't have any leftover oranges in their 'before' queue
                    continue;
                }
                Queue<Orange> o = p.workerQueues.get(type);
                System.out.println(type + " Leftover oranges: " + o.size());
            }
        }
    }

    /**
     * Causes the current thread to sleep for the specified time.
     * If the sleep is interrupted, an error message is printed.
     *
     * @param time   the time to sleep in milliseconds (minimum 1 ms)
     * @param errMsg the error message to print if interrupted
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
     * Map of worker queues for each WorkerType.
     * <p>
     * Each queue holds Orange objects that are to be processed by workers of that
     * type.
     * The queues are initialized dynamically using an EnumMap.
     * </p>
     */
    private final Map<WorkerType, Queue<Orange>> workerQueues = new EnumMap<>(WorkerType.class);

    /**
     * Number of oranges required to produce one bottle of juice.
     */
    public final int ORANGES_PER_BOTTLE = 3;

    /**
     * Thread that manages the lifecycle of this Plant.
     */
    private final Thread thread;
    /**
     * Array holding all the Worker threads associated with this Plant.
     */
    private final Worker[] workers = new Worker[workersPerStation.values().stream().mapToInt(Integer::intValue).sum()];
    /**
     * Flag to indicate whether the plant should continue processing.
     */
    private volatile boolean timeToWork;

    /**
     * Constructs a new Plant instance.
     * <p>
     * This constructor initializes the worker queues for each WorkerType and
     * creates the
     * Worker threads, linking the queues appropriately to simulate the processing
     * pipeline.
     * </p>
     *
     * @param plantNum an identifier for this Plant instance
     */
    Plant(int plantNum) {
        // Initialize workerQueues dynamically for each WorkerType
        for (WorkerType type : WorkerType.values()) {
            workerQueues.put(type, new LinkedList<>());
        }
        // Initialize workers dynamically for each WorkerType
        int threadNum = 0;
        for (int i = 0; i < WorkerType.values().length; i++) {
            int stationWorkers = workersPerStation.get(WorkerType.values()[i]);
            for (int j = 0; j < stationWorkers; j++) {
                Queue<Orange> before = workerQueues.get(WorkerType.values()[i]);
                Queue<Orange> after;
                // For Processors and Bottlers, no subsequent queue is required
                if (WorkerType.values()[i] == WorkerType.Bottlers) {
                    after = null;
                } else {
                    after = workerQueues.get(WorkerType.values()[i + 1]);
                }
                workers[threadNum] = new Worker(plantNum, threadNum, WorkerType.values()[i], before, after);
                threadNum++;
            }
        }
        thread = new Thread(this, "Plant[" + plantNum + "]");
    }

    /**
     * Starts the Plant's processing thread.
     * <p>
     * Sets the working flag to true and starts the thread that manages the Plant.
     * </p>
     */
    public void startPlant() {
        timeToWork = true;
        thread.start();
    }

    /**
     * Signals the Plant to stop processing.
     */
    public void stopPlant() {
        timeToWork = false;
    }

    /**
     * Waits for the Plant's processing thread to finish execution.
     * <p>
     * If the thread is interrupted while waiting, an error message is printed.
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
     * Main loop of the Plant's processing thread.
     * <p>
     * This method runs until the {@code timeToWork} flag is set to false.
     * Currently, the loop does not perform any processing.
     * </p>
     */
    @Override
    public void run() {
        while (timeToWork) {
            continue; // CURRENTLY WE ARE NOT DOING ANYTHING WITH THIS; REMOVE LATER
        }
        System.out.println(Thread.currentThread().getName() + " Done");
    }

    /**
     * Aggregates the total number of oranges provided by all fetcher workers.
     *
     * @return total number of oranges provided to this plant
     */
    public int getProvidedOranges() {
        int op = 0;
        for (Worker w : workers) {
            if (w.getType() == WorkerType.Fetchers) {
                op += w.getOrangesProvided();
            }
        }
        return op;
    }

    /**
     * Aggregates the total number of oranges processed (bottled) by all bottler
     * workers.
     * <p>
     * Note: This method could be enhanced by maintaining a dedicated counter.
     * </p>
     *
     * @return total number of oranges processed in this plant
     */
    public int getProcessedOranges() {
        int op = 0;
        for (Worker w : workers) {
            if (w.getType() == WorkerType.Bottlers) {
                op += w.getOrangesProcessed();
            }
        }
        return op;
    }

    /**
     * Calculates the total number of juice bottles created.
     * <p>
     * This is determined by dividing the number of processed oranges by the number
     * required per bottle.
     * </p>
     *
     * @return total number of bottles produced
     */
    public int getBottles() {
        return getProcessedOranges() / ORANGES_PER_BOTTLE;
    }

    /**
     * Calculates the total number of oranges that were wasted.
     * <p>
     * Waste is defined as the difference between the number of oranges provided and
     * those processed.
     * </p>
     *
     * @return total number of wasted oranges
     */
    public int getWaste() {
        return getProvidedOranges() - getProcessedOranges();
    }

    /**
     * Retrieves the array of Worker threads associated with this Plant.
     *
     * @return array of Worker objects
     */
    public Worker[] getWorkers() {
        return workers;
    }
}