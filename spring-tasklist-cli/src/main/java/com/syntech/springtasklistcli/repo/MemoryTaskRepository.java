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
 * This class is managed by Spring's dependency injection container.
 * It is only active if the property 'storage.type' is set to 'mem' (or missing).
 * See @Repository and @ConditionalOnProperty annotations.
 */
@Repository // Enables Spring to inject this as a bean
@ConditionalOnProperty(name = "storage.type", havingValue = "mem", matchIfMissing = true)
public class MemoryTaskRepository implements TaskRepository {

    // Thread-safe map to store tasks in memory
    private final Map<Long, Task> db = new ConcurrentHashMap<>();
    // Sequence generator for unique task IDs
    private final AtomicLong seq = new AtomicLong(0);
    // Logger for debug output
    private static final Logger log = LoggerFactory.getLogger(MemoryTaskRepository.class);


    @Override
    public Task add(Task task) {
        // Assign a unique ID to the task

        task.setId(seq.getAndIncrement());

        log.debug("Preparing to add task {}, {}", task.getId(), task.getTaskName());

        try {
            db.put(task.getId(), task);
        } catch (Exception e) {
            log.error(e.toString());
            log.error("Error while trying to add task {}, {}", task.getId(), task.getTaskName());
            return null;
        }
        log.debug("Task: {} has been added to list.\n", task);
        return task;
    }

    @Override
    public Task findById(Long id) {
        // Check if task exists in the map
        if (db.containsKey(id)) {
            log.debug("Task: {} has been found.\n", id);
            log.debug("Returning task {}.", db.get(id));

            return db.get(id);
        }

        log.debug("Task: {} has NOT been found.\n", id);
        return null;
    }

    @Override
    public List<Task> findAll() {
        // Return all tasks as a list
        if (db.isEmpty()) {
            log.debug("Nothing in list.\n");
            return new ArrayList<Task>();
        }
        return new ArrayList<Task>(db.values());
    }

    @Override
    public void deleteById(Long id) {
        // Remove task by ID if it exists
        if (db.containsKey(id)) {
            db.remove(id);
            log.debug("Task: {} has been deleted.\n", id);
        } else {
            log.debug("Task: {} has NOT been deleted.\n", id);
        }
    }

    @Override
    public void deleteAll() {
        // Clear all tasks from the map
        if (db.isEmpty()) {
            log.debug("List is empty unable to clear.\n");
            return;
        }
        db.clear();
        log.debug("Cleared all entries in list.\n");

    }

    @Override
    public void update(Long id, String taskName, String description, Boolean completed) {
        // Get the original task
        Task task = db.get(id);

        if (db.containsKey(id)) {
            // Update task fields and timestamp
            task.setUpdatedTime();
            task.setTaskName(taskName);
            task.setDescription(description);
            task.setCompleted(completed);

            db.put(task.getId(), task);
        } else {
            log.debug("Task {} does not exist.\n", task.getId());
            log.debug("Skipping update...\n");
        }
    }

    @Override
    public void complete(Long id) {
        // Mark task as completed if it exists
        if  (db.containsKey(id)) {
            Task task = db.get(id);
            task.setCompleted(true);
            task.setUpdatedTime();
            db.put(id, task);
        }
    }

    @Override
    public String printTasks() {
        String tasksTbl = "";

        if (db.isEmpty()) {
            tasksTbl = "No tasks found.\n";
            return tasksTbl;
        }

        int idWidth, nameWidth, descWidth, compWidth;
        idWidth = nameWidth = descWidth = compWidth = 0;
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
