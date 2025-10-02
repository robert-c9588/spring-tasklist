package com.syntech.springtasklistcli.repo;

import com.syntech.springtasklistcli.domain.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.nio.file.*;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * File-based implementation of TaskRepository.
 *
 * This class is managed by Spring's dependency injection container.
 * It is only active if the property 'storage.type' is set to 'file'.
 * See @Repository and @ConditionalOnProperty annotations.
 */
@Repository // Enables Spring to inject this as a bean
@ConditionalOnProperty(name = "storage.type", havingValue = "file")
public class FileTaskRepository implements TaskRepository {

    // Path to the CSV file for storing tasks
    private final Path file;
    // In-memory map for fast access to tasks
    private final Map<Long, Task> db = new ConcurrentHashMap<>();
    // Sequence generator for unique task IDs
    private final AtomicLong seq = new AtomicLong(0);
    // Logger for debug output
    private static final Logger log = LoggerFactory.getLogger(FileTaskRepository.class);
    // Pipe Delimiter for CSV
    private static final String DELIMITER = "\\|";

    /**
     * Constructor initializes file and loads tasks from file.
     * The file path can be overridden with the system property 'todo.file'.
     * Throws IOException if file cannot be created.
     */
    public FileTaskRepository() throws IOException {
        String path = System.getProperty("todo.file", "tasks.csv"); // override with -Dtodo.file=/path/file.csv
        this.file = Paths.get(path);
        // Create file and parent directories if they don't exist
        if (Files.notExists(file)) {
            Files.createDirectories(file.toAbsolutePath().getParent() == null ? Paths.get(".") : file.toAbsolutePath().getParent());
            Files.createFile(file);
        }
        // Initialize sequence based on max ID in file
        List<Task> tasks = load();
        long maxId = 0;
        if (!tasks.isEmpty()) {
            for (Task task : tasks) {
                if (task.getId() > maxId) {
                    maxId = (long) task.getId();
                }
                setSequence(maxId);
                try {
                    this.db.put(task.getId(), task);
                } catch (Exception e) {
                    log.error(e.toString());
                    log.error("Error while trying to initialize db {}, {}", task.getId(), task.getTaskName());
                    log.error("Closing application");
                    System.exit(1);
                }
                log.debug("Task: {} has been added to list.\n", task);
            }
        }
    }

    private void setSequence(long value) {
        this.seq.set(value);
    }
    @Override
    public Task add(Task task) {
        // Assign a unique ID to the task
        task.setId(seq.getAndIncrement());

        log.debug("Preparing to add task {}, {}", task.getId(), task.getTaskName());

        try {
            this.db.put(task.getId(), task);
        } catch (Exception e) {
            log.error(e.toString());
            log.error("Error while trying to add task {}, {}", task.getId(), task.getTaskName());
            log.error("Closing application");
            System.exit(1);
        }
        log.debug("Task: {} has been added to list.\n", task);
        return task;
    }

    @Override
    public Task findById(Long id) {
        // Check if task exists in the map
        if (this.db.containsKey(id)) {
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
        if (this.db.isEmpty()) {
            log.debug("Nothing in list.\n");
            return new ArrayList<Task>();
        }
        return new ArrayList<Task>(db.values());
    }

    @Override
    public void deleteById(Long id) {
        // Remove task by ID if it exists
        if (this.db.containsKey(id)) {
            this.db.remove(id);
            log.debug("Task: {} has been deleted.\n", id);
        } else {
            log.debug("Task: {} has NOT been deleted.\n", id);
        }
    }

    @Override
    public void deleteAll() {
        // Clear all tasks from the map
        if (this.db.isEmpty()) {
            log.debug("List is empty unable to clear.\n");
            return;
        }
        this.db.clear();
        log.debug("Cleared all entries in list.\n");
    }

    @Override
    public void update(Long id, String taskName, String description, Boolean completed) {
        // Get the original task
        Task task = this.db.get(id);

        if (this.db.containsKey(id)) {
            // Update task fields and timestamp
            task.setUpdatedTime();
            task.setTaskName(taskName);
            task.setDescription(description);
            task.setCompleted(completed);

            this.db.put(task.getId(), task);
        } else {
            log.debug("Task {} does not exist.\n", task.getId());
            log.debug("Skipping update...\n");
        }
    }

    @Override
    public void complete(Long id) {
        // Mark task as completed if it exists
        if  (this.db.containsKey(id)) {
            Task task = db.get(id);
            task.setCompleted(true);
            task.setUpdatedTime();
            this.db.put(id, task);
        }
    }

    /**
     * Loads tasks from the CSV file into the in-memory map.
     * @return List of completed tasks (for initialization)
     */
    private List<Task> load() {
        try {
            log.debug("Reading file: " + file.toAbsolutePath());

            if (Files.size(file) == 0) return List.of();

            List<String> lines = Files.readAllLines(file);
            List<Task> tasks;

            log.debug(lines.toString());


                for (String l : lines) {
                    try {
                    if (l.isBlank()) continue;
                    String[] p = l.split(DELIMITER, 4);

                    if (p[0].equals("id")) {
                        continue;
                    }
                    //log.debug("Reading:" + p[0] + " " + p[1] + " " + p[2] + " " + p[3]);

                    Task t = new Task(Long.parseLong(p[0]), p[1], p[2], Boolean.parseBoolean(p[3]));
                    this.db.put(t.getId(), t);
                    } catch (Exception e) {
                        log.error(e.toString());
                        log.error("Error while trying to read file: {}", file.toAbsolutePath());
                        log.error("Skipping adding Task: {}", l);
                        log.error("Please fix the file and restart the application.");
                        log.error("Please use delimiter '|' for separating values.");
                        log.error("Ensure the data is in the format: id|taskName|description|completed");
                        log.error("Closing application");
                        System.exit(1);
                    }
                }
                // Only return completed tasks (for some reason)
                tasks = this.db.values().stream().filter(Task::isCompleted).collect(Collectors.toList());


            return tasks;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Prints tasks as a table (method stub).
     * @return String representation of tasks table
     */
    @Override
    public String printTasks() {
        String tasksTbl = "";

        if (this.db.isEmpty()) {
            tasksTbl = "No tasks found.\n";
            return tasksTbl;
        }

        int idWidth, nameWidth, descWidth, compWidth;
        idWidth = nameWidth = descWidth = 0;
        compWidth = 9;
        for (Task t : this.db.values()) {
            idWidth = Math.max(idWidth, String.valueOf(t.getId()).length());
            nameWidth = Math.max(nameWidth, t.getTaskName().length());
            descWidth = Math.max(descWidth, t.getDescription().length());
        }

        if (descWidth > 50) descWidth = 53;



        String format = "| %-" + idWidth + "s | %-" + nameWidth + "s | %-" + descWidth + "s | %-" + compWidth + "s |\n";
        String line = "+" + "-".repeat(idWidth + 2) + "+" + "-".repeat(nameWidth + 2) + "+" + "-".repeat(descWidth + 2) + "+" + "-".repeat(compWidth + 2) + "+\n";
        tasksTbl += line;
        tasksTbl += String.format(format, "ID", "Task Name", "Description", "Completed");
        tasksTbl += line;
        for (Task t : this.db.values()) {
            if (t.getDescription().length() > 50)
                tasksTbl += String.format(format, t.getId(), t.getTaskName(), t.getDescription().substring(0,49) + "...", t.isCompleted());
            else
                tasksTbl += String.format(format, t.getId(), t.getTaskName(), t.getDescription(), t.isCompleted());
        }
        tasksTbl += line;



        return tasksTbl;
    }
}
