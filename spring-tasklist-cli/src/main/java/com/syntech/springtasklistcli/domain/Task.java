package com.syntech.springtasklistcli.domain;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Objects;

/**
 * Represents a task in the task list application.
 *
 * This class serves as the domain model for tasks, encapsulating
 * task-related data such as ID, name, description, and completion status.
 */
public class Task {

    // Unique identifier for the task
    private long id;

    // Name/title of the task
    private String taskName;

    // Detailed description of the task
    private String description;

    // Status indicating if the task is completed
    private boolean completed;

    /**
     * Default constructor.
     *
     * Creates an empty Task object. This is required for certain frameworks
     * or deserialization processes.
     */
    public Task() {
        super();
    }

    /**
     * Constructor for creating a new Task without an ID.
     *
     * The ID will be assigned by the repository when the task is added.
     *
     * @param taskName    Name of the task
     * @param description Description of the task
     * @param completed   Completion status of the task
     */
    public Task(String taskName, String description, Boolean completed) {
        super();
        this.id = 0; // Default ID; repository will assign a unique ID
        this.taskName = taskName;
        this.description = description;
        this.completed = completed;
    }

    /**
     * Constructor for creating a Task with a specific ID.
     *
     * @param id          Unique identifier for the task
     * @param taskName    Name of the task
     * @param description Description of the task
     * @param completed   Completion status of the task
     */
    public Task(Long id, String taskName, String description, Boolean completed) {
        super();
        this.id = id;
        this.taskName = taskName;
        this.description = description;
        this.completed = completed;
    }

    /**
     * Get the unique identifier of the task.
     *
     * @return Task ID
     */
    public long getId() {
        return id;
    }

    /**
     * Get the name/title of the task.
     *
     * @return Task name
     */
    public String getTaskName() {
        return taskName;
    }

    /**
     * Get the detailed description of the task.
     *
     * @return Task description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Check if the task is completed.
     *
     * @return true if the task is completed, false otherwise
     */
    public boolean isCompleted() {
        return completed;
    }

    /**
     * Alternate getter for the completion status.
     *
     * @return Completion status of the task
     */
    public boolean getCompleted() {
        return completed;
    }

    /**
     * Set the unique identifier of the task.
     *
     * @param id Task ID to set
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * Set the name/title of the task.
     *
     * @param taskName Task name to set
     */
    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    /**
     * Set the detailed description of the task.
     *
     * @param description Task description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Set the completion status of the task.
     *
     * @param completed Completion status to set
     */
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    /**
     * Generate a string representation of the task.
     *
     * @return String representation of the task
     */
    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", taskName='" + taskName + '\'' +
                ", description='" + description + '\'' +
                ", completed=" + completed +
                '}';
    }

    /**
     * Compare this task with another object for equality.
     *
     * Two tasks are considered equal if their IDs, names, descriptions,
     * and completion statuses are the same.
     *
     * @param o Object to compare with
     * @return true if the tasks are equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id && completed == task.completed &&
                Objects.equals(taskName, task.taskName) &&
                Objects.equals(description, task.description);
    }

    /**
     * Generate a hash code for the task.
     *
     * The hash code is based on the task's ID, name, description, and completion status.
     *
     * @return Hash code of the task
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, taskName, description, completed);
    }
}