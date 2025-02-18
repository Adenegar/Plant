import java.util.Queue;

public class Worker implements Runnable {

    private static void delay(long time, String errMsg) {
        long sleepTime = Math.max(1, time);
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            System.err.println(errMsg);
        }
    }

    public final int ORANGES_PER_BOTTLE = 3;

    private final Thread thread;
    private Queue<Orange> before;
    private Queue<Orange> after;
    private int orangesProvided;
    private int orangesProcessed;
    private Plant.WorkerType type;
    private volatile boolean timeToWork;
    
    

    Worker(int plantNum, int threadNum, Plant.WorkerType type, Queue<Orange> before, Queue<Orange> after) {
        orangesProvided = 0;
        orangesProcessed = 0;
        this.type = type;
        this.before = before;
        this.after = after;
        thread = new Thread(this, "Worker[plant " + plantNum + ", thread " + threadNum + ", type " + type + "]");
    }

    public void startWorker() {
        timeToWork = true;
        thread.start();
    }

    public void stopWorker() {
        timeToWork = false;
    }

    public void waitToStop() {
        try {
            thread.join();
        } catch (InterruptedException e) {
            System.err.println(thread.getName() + " stop malfunction");
        }
    }

    @Override
    public void run() {
        System.out.print(Thread.currentThread().getName() + " Processing oranges");
        while (timeToWork) {
            // Check if we can grab an orange
            if (before.isEmpty()) { // TODO: Synchronize this block
                System.out.println("Waiting for oranges");
                delay(10, Thread.currentThread().getName() + " no oranges to process, waiting...");
                continue;
            } else {
                // TODO: Add sanity check for orange state
                orangesProvided++;
                Orange o = before.remove();
                o.runProcess();
                if (after != null) { // This means we're not the last worker in the line TODO: Sanity check for last worker type
                    after.add(o);
                } 
                orangesProcessed++;
                System.out.print(".");
            }
        }
        System.out.println("");
        System.out.println(Thread.currentThread().getName() + " Done");
    }

    public Plant.WorkerType getType() {
        return type;
    }

    public int getOrangesProcessed() {
        return orangesProcessed;
    }
}
