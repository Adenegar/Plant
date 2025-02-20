/**
 * Represents an orange in the juice bottling simulation.
 * <p>
 * An Orange undergoes a series of processing states defined by the
 * {@link State} enum.
 * Each state corresponds to a stage in the processing pipeline with an
 * associated processing time.
 * </p>
 * 
 * Note: AI was used to generate javadocs for this file
 */
public class Orange {

    /**
     * Enum representing the processing states of an Orange.
     */
    public enum State {
        /**
         * The orange has been fetched.
         */
        Fetched(15),
        /**
         * The orange has been peeled.
         */
        Peeled(38),
        /**
         * The orange has been squeezed.
         */
        Squeezed(29),
        /**
         * The orange has been bottled.
         */
        Bottled(17),
        /**
         * The orange is fully processed. Processing at this state throws an exception.
         */
        Processed(1);

        /**
         * The final index for state transitions.
         */
        private static final int finalIndex = State.values().length - 1;

        /**
         * The time (in milliseconds) required to complete processing for this state.
         */
        final int timeToComplete;

        /**
         * Constructs a state with the specified processing time.
         *
         * @param timeToComplete the processing time in milliseconds for this state
         */
        State(int timeToComplete) {
            this.timeToComplete = timeToComplete;
        }

        /**
         * Retrieves the next processing state in the sequence.
         *
         * @return the next {@link State} in the processing pipeline
         * @throws IllegalStateException if this state is already the final state
         */
        State getNext() {
            int currIndex = this.ordinal();
            if (currIndex >= finalIndex) {
                throw new IllegalStateException("Already at final state");
            }
            return State.values()[currIndex + 1];
        }
    }

    /**
     * The current processing state of this Orange.
     */
    private State state;

    /**
     * Constructs a new Orange and begins its processing.
     * <p>
     * The orange is initialized in the {@link State#Fetched} state and immediately
     * performs work
     * to transition to the next state.
     * </p>
     */
    public Orange() {
        state = State.Fetched;
        doWork();
    }

    /**
     * Retrieves the current state of the Orange.
     *
     * @return the current {@link State} of the orange
     */
    public State getState() {
        return state;
    }

    /**
     * Processes the orange by transitioning it to the next state and simulating the
     * work required.
     * <p>
     * This method first checks if the orange is already fully processed. If it is,
     * an exception is thrown.
     * Otherwise, it advances the orange to the next state and simulates the
     * processing by calling {@link #doWork()}.
     * </p>
     *
     * @throws IllegalStateException if the orange is already fully processed
     */
    public void runProcess() {
        // Prevent processing an already completed orange.
        if (state == State.Processed) {
            throw new IllegalStateException("This orange has already been processed");
        }
        // Transition to the next state before performing the associated work.
        // ANDREW MODIFIED: Flipped order with next call to avoid having to fetch twice.
        state = state.getNext();
        doWork();
    }

    /**
     * Simulates the processing work by causing the thread to sleep for the duration
     * associated
     * with the current state.
     * <p>
     * If the sleep is interrupted, an error message is printed.
     * </p>
     */
    private void doWork() {
        // Sleep for the time required to simulate work for the current state.
        try {
            Thread.sleep(state.timeToComplete);
        } catch (InterruptedException e) {
            System.err.println("Incomplete orange processing, juice may be bad");
        }
    }
}