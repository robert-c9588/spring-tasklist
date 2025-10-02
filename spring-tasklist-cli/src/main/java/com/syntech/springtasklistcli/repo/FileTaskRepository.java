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
    // Sequence generator for unique task IDs
    private final AtomicLong seq = new AtomicLong(0);
    // Logger for debug output
    private static final Logger log = LoggerFactory.getLogger(FileTaskRepository.class);
    // Pipe Delimiter for CSV
    private static final String RAW_DELIM = "|";     // for writing
    private static final String SPLIT_DELIM = "\\|"; // for reading (regex)
    private static final String HEADER = "id|taskName|description|completed";

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

        long maxId = tasks.stream().mapToLong(Task::getId).max().orElse(0L);
        this.seq.set(maxId + 1);
    }

    private String toRow(Task t) {
        // keep descriptions single-line; you can also escape RAW_DELIM if needed
        String desc = t.getDescription() == null ? "" : t.getDescription().replace("\r", " ").replace("\n", " ");
        String name = t.getTaskName() == null ? "" : t.getTaskName().replace("\r", " ").replace("\n", " ");
        return t.getId() + RAW_DELIM + name + RAW_DELIM + desc + RAW_DELIM + t.isCompleted();
    }

    @Override
    public void add(Task task) {
        // Assign a unique ID to the task
        task.setId(seq.getAndIncrement());

        log.debug("Preparing to add task {}, {} to file", task.getId(), task.getTaskName());

        try (BufferedWriter w = Files.newBufferedWriter(file, StandardOpenOption.APPEND)) {
            w.write(toRow(task));
            w.newLine();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public Task findById(Long id) {
        // Check if task exists in the map
        Task task = this.load().stream().filter(t-> t.getId() == id).findFirst().orElse(null);
        if (task != null) {
            log.debug("Task: {} has been found.\n", id);
            log.debug("Returning task {}.", task.getId());
            return task;
        }
        log.debug("Task: {} has NOT been found.\n", id);
        return null;
    }

    @Override
    public List<Task> findAll() {
        // Return all tasks as a list
        List<Task> tasks = this.load();
        if (tasks.isEmpty()) {
            log.debug("Nothing in list.\n");
            return new ArrayList<Task>();
        }
        return tasks;
    }

    @Override
    public void deleteById(Long id) {
        // Remove task by ID if it exists
        List<Task> tasks = this.load();
        if (!tasks.isEmpty()) {
            tasks.removeIf(t -> t.getId() == id);
            log.debug("Task: {} has been deleted.\n", id);
            log.debug("Writing updated tasks to file.\n");
            writeAll(tasks);
            log.debug("Writing completed.\n");
        } else {
            log.debug("Task: {} has NOT been deleted.\n", id);
        }
    }

    private void writeAll(List<Task> tasks) {
        try (BufferedWriter w = Files.newBufferedWriter(file, StandardOpenOption.TRUNCATE_EXISTING)) {
            w.write(HEADER);
            w.newLine();
            for (Task t : tasks) {
                w.write(toRow(t));
                w.newLine();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void deleteAll() {
        // Clear all tasks from the map
        List<Task> tasks = this.load();
        if (tasks.isEmpty()) {
            log.debug("List is empty unable to clear.\n");
            return;
        }

        // Clear the file by writing only the header
        writeAll(new ArrayList<>());
        log.debug("Cleared all entries in list.\n");
    }

    @Override
    public void update(Long id, String taskName, String description, Boolean completed) {
        // Load current tasks
        List<Task> tasks = this.load();
        // Find the task and update it
        tasks.stream().filter(t -> t.getId() == id).findFirst().ifPresent(t -> {
            t.setTaskName(taskName);
            t.setDescription(description);
            t.setCompleted(completed);
            writeAll(tasks);
        });
    }

    @Override
    public void complete(Long id) {
        // Load current tasks
        List<Task> tasks = this.load();
        // Find the task by ID and mark it as completed
        tasks.stream().filter(t -> t.getId() == id).findFirst().ifPresent(t -> {
            t.setCompleted(true);
            writeAll(tasks);
        });
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
            List<Task> tasks = new ArrayList<>();

            log.debug(lines.toString());


                for (String l : lines) {
                    try {
                        if (l.isBlank()) continue;
                        String[] p = l.split(SPLIT_DELIM, 4);

                        if (p[0].equals("id")) {
                            continue;
                        }
                        Task t = new Task(Long.parseLong(p[0]), p[1], p[2], Boolean.parseBoolean(p[3]));
                        if (t.getId() >= this.seq.get()) {
                            this.seq.set(t.getId() + 1);
                        }
                        tasks.add(t);
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

        if (this.load().isEmpty()) {
            tasksTbl = "No tasks found.\n";
            return tasksTbl;
        }

        int idWidth, nameWidth, descWidth, compWidth;
        idWidth = nameWidth = descWidth = 0;
        compWidth = 9;
        for (Task t : this.load().stream().sorted(Comparator.comparingLong(Task::getId)).collect(Collectors.toList())) {
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
        for (Task t : this.load().stream().sorted(Comparator.comparingLong(Task::getId)).collect(Collectors.toList())) {
            if (t.getDescription().length() > 50)
                tasksTbl += String.format(format, t.getId(), t.getTaskName(), t.getDescription().substring(0,49) + "...", t.isCompleted());
            else
                tasksTbl += String.format(format, t.getId(), t.getTaskName(), t.getDescription(), t.isCompleted());
        }
        tasksTbl += line;



        return tasksTbl;
    }
}
