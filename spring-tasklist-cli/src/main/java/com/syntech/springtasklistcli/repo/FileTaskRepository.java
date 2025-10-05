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
 * File-based implementation of the TaskRepository interface.
 *
 * This repository stores tasks in a CSV file and provides methods to perform
 * CRUD operations. It is activated only when the property 'storage.type' is set to 'file'.
 */
@Repository
@ConditionalOnProperty(name = "storage.type", havingValue = "file")
public class FileTaskRepository implements TaskRepository {

    // Path to the CSV file for storing tasks
    private final Path file;
    // AtomicLong for generating unique task IDs
    private final AtomicLong seq = new AtomicLong(0);
    // Logger for logging debug and error messages
    private static final Logger log = LoggerFactory.getLogger(FileTaskRepository.class);
    // Delimiters for CSV file operations
    private static final String RAW_DELIM = "|";     // Delimiter for writing
    private static final String SPLIT_DELIM = "\\|"; // Delimiter for reading (regex)
    private static final String HEADER = "id|taskName|description|completed";

    /**
     * Constructor for FileTaskRepository.
     *
     * Initializes the file path, creates the file if it does not exist, and loads tasks from the file.
     * The file path can be overridden using the system property 'todo.file'.
     *
     * @throws IOException if the file cannot be created or accessed.
     */
    public FileTaskRepository() throws IOException {
        String path = System.getProperty("todo.file", "tasks.csv");
        this.file = Paths.get(path);
        if (Files.notExists(file)) {
            Files.createDirectories(file.toAbsolutePath().getParent() == null ? Paths.get(".") : file.toAbsolutePath().getParent());
            Files.createFile(file);
        }
        List<Task> tasks = load();
        long maxId = tasks.stream().mapToLong(Task::getId).max().orElse(0L);
        this.seq.set(maxId + 1);
    }

    /**
     * Converts a Task object to a CSV row string.
     *
     * @param t The task to convert.
     * @return A string representing the task in CSV format.
     */
    private String toRow(Task t) {
        String desc = t.getDescription() == null ? "" : t.getDescription().replace("\r", " ").replace("\n", " ");
        String name = t.getTaskName() == null ? "" : t.getTaskName().replace("\r", " ").replace("\n", " ");
        return t.getId() + RAW_DELIM + name + RAW_DELIM + desc + RAW_DELIM + t.isCompleted();
    }

    /**
     * Adds a new task to the repository.
     *
     * @param task The task to add.
     */
    @Override
    public void add(Task task) {
        task.setId(seq.getAndIncrement());
        log.debug("Preparing to add task {}, {} to file", task.getId(), task.getTaskName());
        try (BufferedWriter w = Files.newBufferedWriter(file, StandardOpenOption.APPEND)) {
            w.write(toRow(task));
            w.newLine();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Finds a task by its ID.
     *
     * @param id The ID of the task to find.
     * @return The task if found, or null if not found.
     */
    @Override
    public Task findById(Long id) {
        Task task = this.load().stream().filter(t -> t.getId() == id).findFirst().orElse(null);
        if (task != null) {
            log.debug("Task: {} has been found.", id);
            return task;
        }
        log.debug("Task: {} has NOT been found.", id);
        return null;
    }

    /**
     * Retrieves all tasks from the repository.
     *
     * @return A list of all tasks.
     */
    @Override
    public List<Task> findAll() {
        List<Task> tasks = this.load();
        if (tasks.isEmpty()) {
            log.debug("Nothing in list.");
            return new ArrayList<>();
        }
        return tasks;
    }

    /**
     * Deletes a task by its ID.
     *
     * @param id The ID of the task to delete.
     */
    @Override
    public void deleteById(Long id) {
        List<Task> tasks = this.load();
        if (!tasks.isEmpty()) {
            tasks.removeIf(t -> t.getId() == id);
            log.debug("Task: {} has been deleted.", id);
            writeAll(tasks);
        } else {
            log.debug("Task: {} has NOT been deleted.", id);
        }
    }

    /**
     * Writes all tasks to the CSV file.
     *
     * @param tasks The list of tasks to write.
     */
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

    /**
     * Deletes all tasks from the repository.
     */
    @Override
    public void deleteAll() {
        List<Task> tasks = this.load();
        if (tasks.isEmpty()) {
            log.debug("List is empty, unable to clear.");
            return;
        }
        writeAll(new ArrayList<>());
        log.debug("Cleared all entries in list.");
    }

    /**
     * Updates an existing task.
     *
     * @param id          The ID of the task to update.
     * @param taskName    The new name of the task.
     * @param description The new description of the task.
     * @param completed   The new completion status of the task.
     */
    @Override
    public void update(Long id, String taskName, String description, Boolean completed) {
        List<Task> tasks = this.load();
        tasks.stream().filter(t -> t.getId() == id).findFirst().ifPresent(t -> {
            t.setTaskName(taskName);
            t.setDescription(description);
            t.setCompleted(completed);
            writeAll(tasks);
        });
    }

    /**
     * Marks a task as completed.
     *
     * @param id The ID of the task to mark as completed.
     */
    @Override
    public void complete(Long id) {
        List<Task> tasks = this.load();
        tasks.stream().filter(t -> t.getId() == id).findFirst().ifPresent(t -> {
            t.setCompleted(true);
            writeAll(tasks);
        });
    }

    /**
     * Loads tasks from the CSV file.
     *
     * @return A list of tasks loaded from the file.
     */
    private List<Task> load() {
        try {
            log.debug("Reading file: {}", file.toAbsolutePath());
            if (Files.size(file) == 0) return List.of();
            List<String> lines = Files.readAllLines(file);
            List<Task> tasks = new ArrayList<>();
            for (String l : lines) {
                try {
                    if (l.isBlank()) continue;
                    String[] p = l.split(SPLIT_DELIM, 4);
                    if (p[0].equals("id")) continue;
                    Task t = new Task(Long.parseLong(p[0]), p[1], p[2], Boolean.parseBoolean(p[3]));
                    if (t.getId() >= this.seq.get()) {
                        this.seq.set(t.getId() + 1);
                    }
                    tasks.add(t);
                } catch (Exception e) {
                    log.error("Error while reading file: {}", file.toAbsolutePath());
                    log.error("Skipping invalid task: {}", l);
                    System.exit(1);
                }
            }
            return tasks;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Generates a formatted table of tasks for display.
     *
     * @return A string representation of the task table.
     */
    @Override
    public String printTasks() {
        String tasksTbl = "";
        List<Task> tasks = this.load();
        if (tasks.isEmpty()) {
            return "No tasks found.\n";
        }
        int idWidth = 2, nameWidth = 9, descWidth = 11, compWidth = 9;
        for (Task t : tasks.stream().sorted(Comparator.comparingLong(Task::getId)).collect(Collectors.toList())) {
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
        for (Task t : tasks.stream().sorted(Comparator.comparingLong(Task::getId)).collect(Collectors.toList())) {
            if (t.getDescription().length() > 50)
                tasksTbl += String.format(format, t.getId(), t.getTaskName(), t.getDescription().substring(0, 49) + "...", t.isCompleted());
            else
                tasksTbl += String.format(format, t.getId(), t.getTaskName(), t.getDescription(), t.isCompleted());
        }
        tasksTbl += line;
        return tasksTbl;
    }
}