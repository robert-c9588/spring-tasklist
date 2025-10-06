package com.syntech.springtasklistcli;

import com.syntech.springtasklistcli.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

/**
 * Main Spring Boot application for the CLI task list.
 * 
 * This class serves as the entry point for the Spring Boot application
 * and provides a CommandLineRunner bean to implement an interactive CLI
 * for managing tasks.
 */
@SpringBootApplication
public class SpringTaskListCliApplication {
    private static final Logger log = LoggerFactory.getLogger(SpringTaskListCliApplication.class);
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    /**
     * Main method to start the Spring Boot application.
     *
     * @param args Command-line arguments passed to the application.
     */
    public static void main(String[] args) {
        log.info("Arguments: {}", args);
        SpringApplication.run(SpringTaskListCliApplication.class, args);
    }

    /**
     * CommandLineRunner bean to run the interactive CLI.
     * 
     * This method initializes a command-line interface for managing tasks.
     * It provides options to add, view, update, and delete tasks using the TaskService.
     *
     * @param tasks The TaskService used to perform task operations.
     * @return A CommandLineRunner instance that runs the CLI loop.
     */
    @Bean
    CommandLineRunner run(TaskService tasks) {
        return args -> {
            Scanner in = new Scanner(System.in); // Do NOT close System.in
            while (true) {
                clearScreen(); // Redraw the screen
                renderHeader("Syntech Task List (interactive)");
                System.out.println(tasks.printTable()); // Display the task table
                renderMenu(); // Display the menu options

                System.out.print("> ");
                String line = in.nextLine().trim();

                switch (line) {
                    case "list", "l":
                        // Refresh the view
                        continue;
                    case "q", "quit", "exit":
                        // Exit the application
                        System.out.println("Bye!");
                        break;
                    case "help", "h", "?":
                        // Display help information
                        System.out.println("Commands:");
                        System.out.println("  add (a)         : Add a new task");
                        System.out.println("  done (d)        : Mark a task as completed");
                        System.out.println("  list (l)        : List all tasks");
                        System.out.println("  view (v)        : View task details");
                        System.out.println("  remove (r)      : Remove a task");
                        System.out.println("  clear all       : Remove all tasks");
                        System.out.println("  help (h, ?)     : Show this help message");
                        System.out.println("  quit (q, exit)  : Exit the application");
                        System.out.println();
                        System.out.println("You can also use shortcuts in parentheses.");
                        System.out.println("Task names and descriptions cannot contain '|' or '\\' characters.");
                        System.out.println();
                        pause(in);
                        continue;
                    case "add", "a":
                        // Add a new task
                        System.out.println("Adding Task, please provide task details.");
                        String taskName;
                        String description;
                        while (true) {
                            System.out.print("Task Name: ");
                            taskName = in.nextLine().trim();

                            if (taskName.isEmpty()) {
                                System.out.println("Task name cannot be empty. Please try again.");
                                pause(in);
                                clearScreen();
                            } else if (taskName.contains("\\") || taskName.contains("|") || taskName.length() > 255) {
                                System.out.println("Task name cannot contain '\\', '|', or be more than 255 chars. Please try again.");
                                pause(in);
                                clearScreen();
                            } else {
                                break;
                            }
                        }
                        while (true) {
                            System.out.print("Description: ");
                            description = in.nextLine().trim();

                            if (description.isEmpty()) {
                                System.out.println("Description cannot be empty. Please try again.");
                                pause(in);
                                clearScreen();
                            } else if (description.contains("\\") || description.contains("|") || description.length() > 255) {
                                System.out.println("Description cannot contain '\\', '|', or be more than 255 chars. Please try again.");
                                pause(in);
                                clearScreen();
                            } else {
                                break;
                            }
                        }

                        tasks.add(taskName, description, false);
                        System.out.println("Added task: " + taskName);
                        pause(in);
                        continue;
                    case "done", "d", "complete":
                        // Mark a task as completed
                        while (true) {
                            System.out.print("Enter task ID to mark as complete: ");
                            String idStr = in.nextLine().trim();
                            try {
                                long id = Long.parseLong(idStr);
                                tasks.complete(id);
                                System.out.println("Completed #" + id);
                                break;
                            } catch (NumberFormatException e) {
                                System.out.println("Invalid ID. Please enter a numeric task ID.");
                            }
                        }
                        pause(in);
                        continue;
                    case "remove", "r", "rm", "delete", "del":
                        // Remove a task by ID
                        while (true) {
                            System.out.print("Enter task ID to remove: ");
                            String idStr = in.nextLine().trim();
                            try {
                                long id = Long.parseLong(idStr);
                                if (tasks.getById(id) == null) {
                                    System.out.println("Task ID " + id + " not found.");
                                } else {
                                    tasks.delete(id);
                                    System.out.println("Removed #" + id);
                                }
                                break;
                            } catch (NumberFormatException e) {
                                System.out.println("Invalid ID. Please enter a numeric task ID.");
                            }
                        }
                        pause(in);
                        continue;
                    case "clear all":
                        // Clear all tasks
                        System.out.print("Are you sure you want to clear all tasks? This action cannot be undone. (y/N): ");
                        String confirm = in.nextLine().trim().toLowerCase();
                        if (confirm.equals("y") || confirm.equals("yes")) {
                            tasks.clearAll();
                            System.out.println("All tasks cleared.");
                        } else {
                            System.out.println("Clear all cancelled.");
                        }
                        pause(in);
                        continue;
                    case "show", "view", "v", "see":
                        // View task details by ID
                        while (true) {
                            System.out.print("Enter task ID to view: ");
                            String idStr = in.nextLine().trim();
                            try {
                                long id = Long.parseLong(idStr);
                                var t = tasks.getById(id);
                                if (t == null) {
                                    System.out.println("Task ID " + id + " not found.");
                                } else {
                                    System.out.println("ID:          " + t.getId());
                                    System.out.println("Task Name:   " + t.getTaskName());
                                    System.out.println("Description: " + t.getDescription());
                                    System.out.println("Completed:   " + t.getCompleted());
                                }
                                break;
                            } catch (NumberFormatException e) {
                                System.out.println("Invalid ID. Please enter a numeric task ID.");
                            }
                        }
                        pause(in);
                        continue;
                    default:
                        // Handle unknown commands
                        System.out.println("Unknown command. Type 'help'.");
                        pause(in);
                        continue;
                }
                break;
            }
        };
    }

    /**
     * Render a header for the CLI.
     *
     * @param title The title to display in the header.
     */
    private static void renderHeader(String title) {
        System.out.println("=".repeat(70));
        System.out.println(title);
        System.out.println("=".repeat(70));
    }

    /**
     * Clear the terminal screen using ANSI escape codes.
     * 
     * This method works on most modern terminals (Windows 10+ supports ANSI).
     */
    private static void clearScreen() {
        System.out.print("\u001b[H\u001b[2J");
        System.out.flush();
    }

    /**
     * Pause execution until the user presses Enter.
     *
     * @param in The Scanner used to read user input.
     */
    private static void pause(Scanner in) {
        System.out.print("Press Enter to continue...");
        in.nextLine();
    }

    /**
     * Render the available menu commands.
     */
    private static void renderMenu() {
        System.out.println("[ add | done | list | view | remove | clear all | help | q ]");
    }
}
