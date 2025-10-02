package com.syntech.springtasklistcli.repo;

import com.syntech.springtasklistcli.domain.Task;

import java.util.List;

/**
 * Interface for Task repository operations.
 *
 * This interface defines the contract for task persistence operations.
 * Implementations can use different storage backends (e.g., in-memory, file-based, database).
 */
public interface TaskRepository {

    /**
     * Add a new task to the repository.
     *
     * @param task The task to add.
     */
    void add(Task task);

    /**
     * Find a task by its ID.
     *
     * @param id The ID of the task to find.
     * @return The task if found, or null if no task with the given ID exists.
     */
    Task findById(Long id);

    /**
     * Retrieve all tasks from the repository.
     *
     * @return A list of all tasks in the repository.
     */
    List<Task> findAll();

    /**
     * Delete a task by its ID.
     *
     * @param id The ID of the task to delete.
     */
    void deleteById(Long id);

    /**
     * Delete all tasks from the repository.
     *
     * This operation removes all tasks and leaves the repository empty.
     */
    void deleteAll();

    /**
     * Update the details of an existing task.
     *
     * @param id          The ID of the task to update.
     * @param taskName    The new name for the task.
     * @param description The new description for the task.
     * @param completed   The new completion status for the task.
     */
    void update(Long id, String taskName, String description, Boolean completed);

    /**
     * Mark a task as completed.
     *
     * @param id The ID of the task to mark as completed.
     */
    void complete(Long id);

    /**
     * Generate a formatted string representation of all tasks.
     *
     * @return A string containing a table-like representation of tasks.
     */
    String printTasks();
}