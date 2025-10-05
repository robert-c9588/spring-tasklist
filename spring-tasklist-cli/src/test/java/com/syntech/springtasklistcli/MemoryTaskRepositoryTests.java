package com.syntech.springtasklistcli.repo;

import com.syntech.springtasklistcli.domain.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MemoryTaskRepositoryTests {

    private MemoryTaskRepository repository;

    @BeforeEach
    void setUp() {
        repository = new MemoryTaskRepository();
        repository.deleteAll();
    }

    @Test
    void addTaskIncreasesRepositorySize() {
        Task task = new Task("New Task", "Task Description", false);
        repository.add(task);
        assertEquals(1, repository.findAll().size());
    }

    @Test
    void findByIdReturnsCorrectTask() {
        Task task = new Task("Find Task", "Task Description", false);
        repository.add(task);
        Task foundTask = repository.findById(task.getId());
        assertNotNull(foundTask);
        assertEquals(task.getTaskName(), foundTask.getTaskName());
    }

    @Test
    void findByIdReturnsNullForNonExistentTask() {
        assertNull(repository.findById(999L));
    }

    @Test
    void deleteByIdRemovesTask() {
        Task task = new Task("Delete Task", "Task Description", false);
        repository.add(task);
        repository.deleteById(task.getId());
        assertNull(repository.findById(task.getId()));
    }

    @Test
    void deleteByIdDoesNothingForNonExistentTask() {
        assertDoesNotThrow(() -> repository.deleteById(999L));
    }

    @Test
    void updateTaskUpdatesFields() {
        Task task = new Task("Update Task", "Task Description", false);
        repository.add(task);
        repository.update(task.getId(), "Updated Name", "Updated Description", true);
        Task updatedTask = repository.findById(task.getId());
        assertNotNull(updatedTask);
        assertEquals("Updated Name", updatedTask.getTaskName());
        assertEquals("Updated Description", updatedTask.getDescription());
        assertTrue(updatedTask.isCompleted());
    }

    @Test
    void completeMarksTaskAsCompleted() {
        Task task = new Task("Complete Task", "Task Description", false);
        repository.add(task);
        repository.complete(task.getId());
        Task completedTask = repository.findById(task.getId());
        assertNotNull(completedTask);
        assertTrue(completedTask.isCompleted());
    }

    @Test
    void findAllReturnsAllTasks() {
        Task task1 = new Task("Task 1", "Description 1", false);
        Task task2 = new Task("Task 2", "Description 2", true);
        repository.add(task1);
        repository.add(task2);
        List<Task> tasks = repository.findAll();
        assertEquals(2, tasks.size());
    }

    @Test
    void deleteAllClearsAllTasks() {
        Task task1 = new Task("Task 1", "Description 1", false);
        Task task2 = new Task("Task 2", "Description 2", true);
        repository.add(task1);
        repository.add(task2);
        repository.deleteAll();
        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void printTasksReturnsFormattedString() {
        Task task = new Task("Print Task", "Task Description", false);
        repository.add(task);
        String output = repository.printTasks();
        assertTrue(output.contains("Print Task"));
        assertTrue(output.contains("Task Description"));
    }
}