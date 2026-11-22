package service.core;

/**
 * Callback interface to communicate progress updates from a background thread
 * to the Swing GUI without violating thread safety.
 * 
 * OOP CONCEPT: Interface (Unit 2)
 * OOP CONCEPT: Multithreading (Unit 3) - Event notification decoupling
 */
public interface ProgressListener {

    /**
     * Triggered when a new stage in the service simulation begins.
     * 
     * @param stageName Descriptive name of the current stage
     * @param percent   Progress percentage (0 to 100)
     */
    void onStageChanged(String stageName, int percent);

    /**
     * Triggered when the service workflow has completed successfully.
     */
    void onFinished();

    /**
     * Triggered if an error occurs during simulation.
     * 
     * @param errorMessage Detailed error description
     */
    void onError(String errorMessage);
}
