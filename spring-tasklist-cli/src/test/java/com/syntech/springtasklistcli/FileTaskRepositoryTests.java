package com.syntech.springtasklistcli;

import com.syntech.springtasklistcli.domain.Task;
import com.syntech.springtasklistcli.repo.FileTaskRepository;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FileTaskRepositoryTests {
    
    @Test
    void findByIdReturnsCorrectTask() throws IOException {
        FileTaskRepository repository = new FileTaskRepository();
        Task task = new Task("Test Task", "Description", false);
        repository.add(task);
        Task foundTask = repository.findById(task.getId());
        assertNotNull(foundTask);
        assertEquals(task.getId(), foundTask.getId());
    }

    @Test
    void findByIdReturnsNullForNonExistentTask() throws IOException {
        FileTaskRepository repository = new FileTaskRepository();
        Task foundTask = repository.findById(999L);
        assertNull(foundTask);
    }

    @Test
    void deleteByIdRemovesTask() throws IOException {
        FileTaskRepository repository = new FileTaskRepository();
        Task task = new Task("Test Task", "Description", false);
        repository.add(task);
        repository.deleteById(task.getId());
        assertNull(repository.findById(task.getId()));
    }

    @Test
    void deleteByIdDoesNothingForNonExistentTask() throws IOException {
        FileTaskRepository repository = new FileTaskRepository();
        assertDoesNotThrow(() -> repository.deleteById(999L));
    }

    @Test
    void updateTaskUpdatesFields() throws IOException {
        FileTaskRepository repository = new FileTaskRepository();
        Task task = new Task("Test Task", "Description", false);
        repository.add(task);
        repository.update(task.getId(), "Updated Task", "Updated Description", true);
        Task updatedTask = repository.findById(task.getId());
        assertNotNull(updatedTask);
        assertEquals("Updated Task", updatedTask.getTaskName());
        assertEquals("Updated Description", updatedTask.getDescription());
        assertTrue(updatedTask.isCompleted());
    }

    @Test
    void completeMarksTaskAsCompleted() throws IOException {
        FileTaskRepository repository = new FileTaskRepository();
        Task task = new Task("Test Task", "Description", false);
        repository.add(task);
        repository.complete(task.getId());
        Task completedTask = repository.findById(task.getId());
        assertNotNull(completedTask);
        assertTrue(completedTask.isCompleted());
    }

    @Test
    void findAllReturnsAllTasks() throws IOException {
        FileTaskRepository repository = new FileTaskRepository();
        Task task1 = new Task("Task 1", "Description 1", false);
        Task task2 = new Task("Task 2", "Description 2", true);
        repository.add(task1);
        repository.add(task2);
        List<Task> tasks = repository.findAll();
        assertEquals(2, tasks.size());
    }

    @Test
    void deleteAllClearsAllTasks() throws IOException {
        FileTaskRepository repository = new FileTaskRepository();
        Task task1 = new Task("Task 1", "Description 1", false);
        Task task2 = new Task("Task 2", "Description 2", true);
        repository.add(task1);
        repository.add(task2);
        repository.deleteAll();
        assertTrue(repository.findAll().isEmpty());
    }
}