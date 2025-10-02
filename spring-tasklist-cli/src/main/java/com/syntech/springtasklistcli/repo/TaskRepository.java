package com.syntech.springtasklistcli.repo;

import com.syntech.springtasklistcli.domain.Task;

import java.util.List;

/**
 * Interface for Task repository operations.
 * Implementations can use different storage backends (memory, file, etc).
 */
public interface TaskRepository {
    /**
     * Add a new task to the repository.
     * @param task Task to add
     * @return The added Task
     */
    void add(Task task);

    /**
     * Find a task by its ID.
     * @param id Task ID
     * @return Task if found, null otherwise
     */
    Task findById(Long id);

    /**
     * Find all tasks in the repository.
     * @return List of all tasks
     */
    List<Task> findAll();

    /**
     * Delete a task by its ID.
     * @param id Task ID
     */
    void deleteById(Long id);

    /**
     * Delete all tasks from the repository.
     */
    void deleteAll();

    /**
     * Update a task's details.
     * @param id Task ID
     * @param taskName New task name
     * @param description New description
     * @param completed New completion status
     */
    void update(Long id, String taskName, String description, Boolean completed);

    /**
     * Mark a task as completed.
     * @param id Task ID
     */
    void complete(Long id);

    String printTasks();
}
