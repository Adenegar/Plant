import java.util.HashMap;
import java.util.Map;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.Queue;

public class Plant implements Runnable {
    // How long do we want to run the juice processing
    public static final long PROCESSING_TIME = 5 * 1000;

    private static final int NUM_PLANTS = 2;

    public static enum WorkerType {
        Fetchers,
        Peelers,
        Squeezers,
        Bottlers,
        Processors
    }

    private static final int NUMBER_OF_FETCHERS = 1;
    private static final int NUMBER_OF_PEELERS = 1;
    private static final int NUMBER_OF_SQUEEZERS = 1;
    private static final int NUMBER_OF_BOTTLERS = 1;
    private static final int NUMBER_OF_PROCESSORS = 1;
    
    private static Map<WorkerType, Integer> workersPerStation = Map.of(
        WorkerType.Fetchers, NUMBER_OF_FETCHERS,
        WorkerType.Peelers, NUMBER_OF_PEELERS,
        WorkerType.Squeezers, NUMBER_OF_SQUEEZERS,
        WorkerType.Bottlers, NUMBER_OF_BOTTLERS,
        WorkerType.Processors, NUMBER_OF_PROCESSORS
    );

    public static void main(String[] args) {
        // Startup the plants
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

        // Stop the plant, and wait for it to shutdown
        for (Plant p : plants) {
            Worker[] workers = p.getWorkers();
            for (Worker w : workers) {
                w.stopWorker();
                // TODO: Try this without the following for loop
                // w.waitToStop();
            }
        }
        for (Plant p : plants) {
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
        System.out.println("Total provided/processed = " + totalProvided + "/" + totalProcessed);
        System.out.println("Created " + totalBottles +
                           ", wasted " + totalWasted + " oranges");
    }

    private static void delay(long time, String errMsg) {
        long sleepTime = Math.max(1, time);
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            System.err.println(errMsg);
        }
    }

    // New dynamic list of queues for each WorkerType using an EnumMap
    private final Map<WorkerType, Queue<Orange>> workerQueues = new EnumMap<>(WorkerType.class);

    public final int ORANGES_PER_BOTTLE = 3;

    private final Thread thread;
    private final Worker[] workers = new Worker[workersPerStation.values().stream().mapToInt(Integer::intValue).sum()];
    private int orangesProvided;
    private int orangesProcessed;
    private volatile boolean timeToWork;

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
                if (i == WorkerType.values().length - 1) {
                    after = null;
                } else {
                    after = workerQueues.get(WorkerType.values()[i + 1]);
                }
                workers[threadNum] = new Worker(plantNum, threadNum, WorkerType.values()[i], before, after);
                threadNum++;
            }
        }
        orangesProvided = 0;
        orangesProcessed = 0;
        thread = new Thread(this, "Plant[" + plantNum + "]");
    }

    public void startPlant() {
        timeToWork = true;
        thread.start();
    }

    @Override
    public void run() {
        while (timeToWork) {
            if (workerQueues.get(WorkerType.Fetchers).size() == 0) {
                workerQueues.get(WorkerType.Fetchers).add(new Orange());
                orangesProvided++;
            }
        }
        System.out.println(Thread.currentThread().getName() + " Done");
    }

    public int getProvidedOranges() {
        return orangesProvided;
    }

    public int getProcessedOranges() {
        for (Worker w : workers) {
            if (w.getType() == WorkerType.Processors) {
                orangesProcessed += w.getOrangesProcessed();
            }
        }
        System.out.println("Processed " + orangesProcessed + " oranges");
        return orangesProcessed;
    }

    public int getBottles() {
        return getProcessedOranges() / ORANGES_PER_BOTTLE;
    }

    public int getWaste() {
        return getProcessedOranges() % ORANGES_PER_BOTTLE;
    }


    public Worker[] getWorkers() {
        return workers;
    }
}
