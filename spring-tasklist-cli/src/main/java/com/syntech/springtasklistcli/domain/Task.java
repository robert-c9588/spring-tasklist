package com.syntech.springtasklistcli.domain;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Objects;

/**
 * Represents a task in the task list application.
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

    // Timestamp when the task was created
    private Timestamp createdTime;
    
    // Timestamp when the task was last updated
    private Timestamp updatedTime;

    /**
     * Default constructor.
     */
    public Task() {
        super();
    }

    /**
     * Constructor for creating a new Task without an ID.
     * The ID will be set by the repository.
     * @param taskName Name of the task
     * @param description Description of the task
     * @param completed Completion status
     */
    public Task(String taskName, String description, Boolean completed) {
        super();
        this.id = 0; //starting at default, TaskRepository will set the ID once added to repo
        this.taskName = taskName;
        this.description = description;
        this.completed = completed;

        this.createdTime = Timestamp.from(Instant.now());
        this.updatedTime = Timestamp.from(Instant.now());
    }

    /**
     * Constructor for creating a Task with a specific ID.
     * @param id Task ID
     * @param taskName Name of the task
     * @param description Description of the task
     * @param completed Completion status
     */
    public Task(Long id, String taskName, String description, Boolean completed) {
        super();
        this.id = id;
        this.taskName = taskName;
        this.description = description;
        this.completed = completed;

        this.createdTime = Timestamp.from(Instant.now());
        this.updatedTime = Timestamp.from(Instant.now());
    }

    // Getter for ID
    public long getId() {
        return id;
    }

    // Getter for task name
    public String getTaskName() {
        return taskName;
    }

    // Getter for description
    public String getDescription() {
        return description;
    }

    // Getter for completed status
    public boolean isCompleted() {
        return completed;
    }

    // Getter for created time
    public Timestamp getCreatedTime() {
        return createdTime;
    }

    // Getter for updated time
    public Timestamp getUpdatedTime() {
        return updatedTime;
    }

    // Alternate getter for completed status
    public boolean getCompleted() {
        return completed;
    }

    // Setter for ID
    public void setId(long id) {
        this.id = id;
    }

    // Setter for task name
    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    // Setter for description
    public void setDescription(String description) {
        this.description = description;
    }

    // Setter for completed status
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    // Setter for created time
    public void setCreatedTime(Timestamp createdTime) {
        this.createdTime = createdTime;
    }

    // Updates the updatedTime to the current timestamp
    public void setUpdatedTime() {
        this.updatedTime = Timestamp.from(Instant.now());
    }

    /**
     * Returns a string representation of the Task.
     */
    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", taskName='" + taskName + '\'' +
                ", description='" + description + '\'' +
                ", completed=" + completed +
                ", createdTime=" + createdTime +
                ", updatedTime=" + updatedTime +
                '}';
    }

    /**
     * Checks if this Task is equal to another object.
     * @param o The object to compare with
     * @return true if equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id && completed == task.completed && Objects.equals(taskName, task.taskName) && Objects.equals(description, task.description) && Objects.equals(createdTime, task.createdTime) && Objects.equals(updatedTime, task.updatedTime);
    }

    /**
     * Returns the hash code for this Task.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, taskName, description, completed, createdTime, updatedTime);
    }

}
