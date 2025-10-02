package com.syntech.springtasklistcli.service;

import com.syntech.springtasklistcli.domain.Task;
import com.syntech.springtasklistcli.repo.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service layer for managing tasks.
 *
 * This class provides methods to perform CRUD operations on tasks
 * and delegates the actual persistence logic to the TaskRepository.
 */
@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TaskService.class);

    /**
     * Constructor for TaskService.
     *
     * @param taskRepository the repository implementation to use for task persistence
     */
    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
        log.info("Using repository: {}", taskRepository.getClass().getSimpleName());
        System.out.println("Using repository: " + taskRepository.getClass().getSimpleName());
    }

    /**
     * Add a new task to the repository.
     *
     * @param taskName    the name of the task
     * @param description the description of the task
     * @param completed   the completion status of the task
     */
    public void add(String taskName, String description, Boolean completed) {
        this.taskRepository.add(new Task(taskName, description, completed));
    }

    /**
     * Mark a task as completed.
     *
     * @param id the ID of the task to mark as completed
     */
    public void complete(Long id) {
        this.taskRepository.complete(id);
    }

    /**
     * Update an existing task.
     *
     * @param id          the ID of the task to update
     * @param taskName    the new name of the task
     * @param description the new description of the task
     * @param completed   the new completion status of the task
     */
    public void update(Long id, String taskName, String description, Boolean completed) {
        this.taskRepository.update(id, taskName, description, completed);
    }

    /**
     * Delete a task by its ID.
     *
     * @param id the ID of the task to delete
     */
    public void delete(Long id) {
        this.taskRepository.deleteById(id);
    }

    /**
     * Retrieve all tasks from the repository.
     *
     * @return a list of all tasks
     */
    public List<Task> getAll() {
        return this.taskRepository.findAll();
    }

    /**
     * Retrieve a task by its ID.
     *
     * @param id the ID of the task to retrieve
     * @return the task with the specified ID, or null if not found
     */
    public Task getById(Long id) {
        return this.taskRepository.findById(id);
    }

    /**
     * Delete all tasks from the repository.
     */
    public void deleteAll() {
        this.taskRepository.deleteAll();
    }

    /**
     * Generate a formatted table of tasks for display in the CLI.
     *
     * @return a string representation of the task table
     */
    public String printTable() {
        return this.taskRepository.printTasks();
    }

    /**
     * Clear all tasks from the repository.
     *
     * This is an alias for the deleteAll method.
     */
    public void clearAll() {
        this.taskRepository.deleteAll();
    }
}