package it.fds.taskmanager;

/**
 * Defines all of the operations required to the reporting features
 *
 * @author fds
 *
 */
public interface ReportService {

    /**
     * Takes as input a TaskState and count how many tasks there are in the database with that status
     *
     * @return the number of tasks marked with the provided status
     */
    String getNumberOfTaskWithStatus(String state);

    /**
     *
     * @return the number of tasks with the given priority
     */
    String getNumberOfTasksWithPriority(String priority);
}
