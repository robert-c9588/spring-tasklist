package com.syntech.springtasklistcli.repo;

import com.syntech.springtasklistcli.domain.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * In-memory implementation of TaskRepository.
 *
 * This repository stores tasks in memory using a thread-safe map and provides
 * methods to perform CRUD operations. It is active when the property 'storage.type'
 * is set to 'mem' or is missing.
 */
@Repository // Marks this class as a Spring-managed bean
@ConditionalOnProperty(name = "storage.type", havingValue = "mem", matchIfMissing = true)
public class MemoryTaskRepository implements TaskRepository {

    // Thread-safe map to store tasks
    private final Map<Long, Task> db = new ConcurrentHashMap<>();
    // AtomicLong for generating unique task IDs
    private final AtomicLong seq = new AtomicLong(1);
    // Logger for logging debug and error messages
    private static final Logger log = LoggerFactory.getLogger(MemoryTaskRepository.class);

    /**
     * Constructor for MemoryTaskRepository.
     *
     * Initializes the in-memory repository and adds sample tasks for demonstration purposes.
     */
    public MemoryTaskRepository() {
        log.info("Initialized in-memory task repository.");
        Task task1 = new Task("Sample Task 1", "This is a sample task", false);
        Task task2 = new Task("Sample Task 2", "This is another sample task", false);
        Task task3 = new Task("Sample Task 3", "This is another sample task", true);
        Task task4 = new Task("Sample Task 4", "This is another sample task", false);
        this.add(task1);
        this.add(task2);
        this.add(task3);
        this.add(task4);
    }

    /**
     * Add a new task to the repository.
     *
     * @param task The task to add.
     */
    @Override
    public void add(Task task) {
        task.setId(seq.getAndIncrement());
        log.debug("Preparing to add task {}, {}", task.getId(), task.getTaskName());
        try {
            db.put(task.getId(), task);
        } catch (Exception e) {
            log.error(e.toString());
            log.error("Error while trying to add task {}, {}", task.getId(), task.getTaskName());
        }
        log.debug("Task: {} has been added to list.\n", task);
    }

    /**
     * Find a task by its ID.
     *
     * @param id The ID of the task to find.
     * @return The task if found, or null if not found.
     */
    @Override
    public Task findById(Long id) {
        if (db.containsKey(id)) {
            log.debug("Task: {} has been found.\n", id);
            log.debug("Returning task {}.", db.get(id));
            return db.get(id);
        }
        log.debug("Task: {} has NOT been found.\n", id);
        return null;
    }

    /**
     * Retrieve all tasks from the repository.
     *
     * @return A list of all tasks.
     */
    @Override
    public List<Task> findAll() {
        if (db.isEmpty()) {
            log.debug("Nothing in list.\n");
            return new ArrayList<>();
        }
        return new ArrayList<>(db.values());
    }

    /**
     * Delete a task by its ID.
     *
     * @param id The ID of the task to delete.
     */
    @Override
    public void deleteById(Long id) {
        if (db.containsKey(id)) {
            db.remove(id);
            log.debug("Task: {} has been deleted.\n", id);
        } else {
            log.debug("Task: {} has NOT been deleted.\n", id);
        }
    }

    /**
     * Delete all tasks from the repository.
     */
    @Override
    public void deleteAll() {
        if (db.isEmpty()) {
            log.debug("List is empty unable to clear.\n");
            return;
        }
        db.clear();
        log.debug("Cleared all entries in list.\n");
    }

    /**
     * Update an existing task.
     *
     * @param id          The ID of the task to update.
     * @param taskName    The new name of the task.
     * @param description The new description of the task.
     * @param completed   The new completion status of the task.
     */
    @Override
    public void update(Long id, String taskName, String description, Boolean completed) {
        Task task = db.get(id);
        if (db.containsKey(id)) {
            task.setTaskName(taskName);
            task.setDescription(description);
            task.setCompleted(completed);
            db.put(task.getId(), task);
        } else {
            log.debug("Task {} does not exist.\n", id);
            log.debug("Skipping update...\n");
        }
    }

    /**
     * Mark a task as completed.
     *
     * @param id The ID of the task to mark as completed.
     */
    @Override
    public void complete(Long id) {
        if (db.containsKey(id)) {
            Task task = db.get(id);
            task.setCompleted(true);
            db.put(id, task);
        }
    }

    /**
     * Generate a formatted table of tasks for display.
     *
     * @return A string representation of the task table.
     */
    @Override
    public String printTasks() {
        String tasksTbl = "";
        if (db.isEmpty()) {
            tasksTbl = "No tasks found.\n";
            return tasksTbl;
        }

        int idWidth = 2, nameWidth = 9, descWidth = 11, compWidth = 9;
        for (Task t : db.values()) {
            idWidth = Math.max(idWidth, String.valueOf(t.getId()).length());
            nameWidth = Math.max(nameWidth, t.getTaskName().length());
            descWidth = Math.max(descWidth, t.getDescription().length());
            compWidth = Math.max(compWidth, String.valueOf(t.isCompleted()).length());
        }

        String format = "| %-" + idWidth + "s | %-" + nameWidth + "s | %-" + descWidth + "s | %-" + compWidth + "s |\n";
        String line = "+" + "-".repeat(idWidth + 2) + "+" + "-".repeat(nameWidth + 2) + "+" + "-".repeat(descWidth + 2) + "+" + "-".repeat(compWidth + 2) + "+\n";
        tasksTbl += line;
        tasksTbl += String.format(format, "ID", "Task Name", "Description", "Completed");
        tasksTbl += line;
        for (Task t : db.values()) {
            tasksTbl += String.format(format, t.getId(), t.getTaskName(), t.getDescription(), t.isCompleted());
        }
        tasksTbl += line;

        return tasksTbl;
    }
}