package service.core;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.swing.SwingUtilities;
import service.database.JobDAO;
import service.exception.InvalidJobException;

/**
 * Background simulation thread that advances a service job through real-life service stages:
 * Job Received -> Inspection -> Repair -> Service Completed.
 * 
 * OOP CONCEPT: Multithreading (Unit 3)
 * Design Choice: Implements Runnable and wraps in a Thread rather than extending Thread.
 * Reason: Implementing Runnable decouples the task logic from the execution runner,
 * allows sharing task instances with thread pools / executors, and preserves single inheritance
 * in Java so the class could extend another class if needed in the future.
 * 
 * Thread Safety:
 * 1. Synchronized active job set prevents duplicate simultaneous simulations for the same job.
 * 2. All callbacks to ProgressListener are posted to the Swing Event Dispatch Thread (EDT)
 *    via SwingUtilities.invokeLater() to ensure GUI responsiveness and thread safety.
 */
public class JobProgressThread implements Runnable {

    // Synchronized set of currently running simulations to prevent duplicate runs
    private static final Set<String> ACTIVE_JOBS = Collections.synchronizedSet(new HashSet<>());

    private static final int STAGE_DELAY_MS = 2000; // 2 seconds per stage

    private final String jobId;
    private final ProgressListener listener;
    private final JobDAO jobDAO;

    /**
     * OOP CONCEPT: Constructor Overloading / Parameterized Constructor
     * 
     * @param jobId    ID of the job to simulate
     * @param listener GUI callback listener
     */
    public JobProgressThread(String jobId, ProgressListener listener) {
        this.jobId = jobId;
        this.listener = listener;
        this.jobDAO = new JobDAO();
    }

    /**
     * Checks if a simulation is already running for the given Job ID.
     */
    public static boolean isJobActive(String jobId) {
        return ACTIVE_JOBS.contains(jobId);
    }

    @Override
    public void run() {
        // Enforce concurrency guard
        synchronized (ACTIVE_JOBS) {
            if (ACTIVE_JOBS.contains(jobId)) {
                notifyError("Simulation is already running for Job " + jobId);
                return;
            }
            ACTIVE_JOBS.add(jobId);
        }

        try {
            // Verify job is eligible for simulation
            ServiceJob job = jobDAO.getJobById(jobId);
            if ("Completed".equalsIgnoreCase(job.getJobStatus())) {
                notifyError("Job " + jobId + " is already Completed. Simulation cannot be restarted.");
                return;
            }

            // Update database status: Pending -> In Progress
            jobDAO.updateJobStatus(jobId, "In Progress");

            // Define simulated service milestones
            String[] stages = {
                "Job Received & Vehicle Queued",
                "Diagnostic & Multi-Point Inspection",
                "Mechanical Repair & Parts Installation",
                "Final Testing & Service Completed"
            };

            int[] percentages = { 25, 50, 75, 100 };

            for (int i = 0; i < stages.length; i++) {
                final String stageName = stages[i];
                final int percent = percentages[i];

                // Notify GUI safely on the EDT
                notifyStage(stageName, percent);

                // Simulate execution duration
                Thread.sleep(STAGE_DELAY_MS);
            }

            // Update database status: In Progress -> Completed
            jobDAO.updateJobStatus(jobId, "Completed");

            // Final completion callback
            notifyFinished();

        } catch (InterruptedException e) {
            // Restore interrupted flag
            Thread.currentThread().interrupt();
            notifyError("Simulation for Job " + jobId + " was interrupted.");
        } catch (InvalidJobException e) {
            notifyError("Validation Error: " + e.getMessage());
        } catch (Exception e) {
            notifyError("Error simulating job: " + e.getMessage());
        } finally {
            // Remove from active running set
            ACTIVE_JOBS.remove(jobId);
        }
    }

    // Helper methods that post events to Swing EDT using SwingUtilities.invokeLater
    private void notifyStage(final String stageName, final int percent) {
        if (listener != null) {
            SwingUtilities.invokeLater(() -> listener.onStageChanged(stageName, percent));
        }
    }

    private void notifyFinished() {
        if (listener != null) {
            SwingUtilities.invokeLater(listener::onFinished);
        }
    }

    private void notifyError(final String errorMessage) {
        if (listener != null) {
            SwingUtilities.invokeLater(() -> listener.onError(errorMessage));
        }
    }
}
