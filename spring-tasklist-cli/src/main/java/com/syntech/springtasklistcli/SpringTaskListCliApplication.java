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



@SpringBootApplication
public class SpringTaskListCliApplication {
    private static final Logger log = LoggerFactory.getLogger(SpringTaskListCliApplication.class);
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());


    public static void main(String[] args) {
        log.info("Arguments: {}", args);

        SpringApplication.run(SpringTaskListCliApplication.class, args);
    }

    @Bean
    CommandLineRunner run(TaskService tasks) {
        return args -> {

            Scanner in = new Scanner(System.in); // do NOT close System.in
            while (true) {
                clearScreen();                          // redraw like a “single screen”
                renderHeader("Syntech Task List (interactive)");
                System.out.println(tasks.printTable());
                renderMenu();

                System.out.print("> ");
                String line = in.nextLine().trim();

                switch (line) {
                    case "list","l":
                        continue; // just redraw
                    case "q","quit","exit":
                        System.out.println("Bye!");
                        break;
                    case "help", "h", "?":
                        System.out.println("Commands:");
                        System.out.println("  add                 - add a new task");
                        System.out.println("  done <id>           - mark a task complete");
                        System.out.println("  list                - re-render task list");
                        System.out.println("  q / quit / exit     - exit");
                        System.out.println("Press Enter to continue...");
                        in.nextLine();
                        continue;
                    case "add","a":
                        System.out.println("Adding Task, please provide task details.");
                        String taskName;
                        String description;
                        while (true) {
                            System.out.print("Task Name: ");
                            taskName = in.nextLine().trim();

                            if (taskName.isEmpty()) {
                                System.out.println("Task name cannot be empty. Please try again.");
                                continue;
                            } else if (taskName.contains("\\") || taskName.contains("|") || taskName.length() > 255) {
                                System.out.println("Task name cannot contain '\\', '|', or be more than 255 chars. Please try again.");
                                continue;
                            } else {
                                break;
                            }
                        }
                        while (true) {
                            System.out.print("Description: ");
                            description = in.nextLine().trim();

                            if (description.isEmpty()) {
                                System.out.println("Description cannot be empty. Please try again.");
                                continue;
                            } else if (description.contains("\\") || description.contains("|") || description.length() > 255) {
                                System.out.println("Description cannot contain '\\', '|', or be more than 255 chars. Please try again.");
                                continue;
                            } else {
                                break;
                            }
                        }

                        tasks.add(taskName, description, false);
                        System.out.println("Added task: " + taskName);
                        pause(in);
                        continue;
                    case "done","d","complete":
                        try {
                            long id = Long.parseLong(line.substring(5).trim());
                            tasks.complete(id);
                            System.out.println("Completed #" + id);
                        } catch (NumberFormatException e) {
                            System.out.println("Usage: done <id>");
                        }
                        pause(in);
                        continue;
                    default:
                        System.out.println("Unknown command. Type 'help'.");
                        pause(in);
                        continue;

                }
                break;
            }
        };
    }

    private static void renderHeader(String title) {
        System.out.println("=".repeat(70));
        System.out.println(title);
        System.out.println("=".repeat(70));
    }


    private static void clearScreen() {
        // ANSI clear; works in most modern terminals (Windows 10+ supports it).
        System.out.print("\u001b[H\u001b[2J");
        System.out.flush();
    }

    private static void pause(Scanner in) {
        System.out.print("Press Enter to continue...");
        in.nextLine();
    }

    private static void renderMenu() {
        System.out.println("[ add | done <id> | list | load | help/? | q ]");
    }
}