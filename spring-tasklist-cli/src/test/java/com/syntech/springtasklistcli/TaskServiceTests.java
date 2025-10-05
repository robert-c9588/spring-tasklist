package com.syntech.springtasklistcli.service;

import com.syntech.springtasklistcli.domain.Task;
import com.syntech.springtasklistcli.repo.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TaskServiceTests {

    private TaskRepository taskRepository;
    private TaskService taskService;

    @BeforeEach
    void setUp() {
        taskRepository = mock(TaskRepository.class);
        taskService = new TaskService(taskRepository);
    }

    @Test
    void addTaskDelegatesToRepository() {
        taskService.add("Task Name", "Task Description", false);
        verify(taskRepository, times(1)).add(any(Task.class));
    }

    @Test
    void completeTaskDelegatesToRepository() {
        taskService.complete(1L);
        verify(taskRepository, times(1)).complete(1L);
    }

    @Test
    void updateTaskDelegatesToRepository() {
        taskService.update(1L, "Updated Name", "Updated Description", true);
        verify(taskRepository, times(1)).update(1L, "Updated Name", "Updated Description", true);
    }

    @Test
    void deleteTaskDelegatesToRepository() {
        taskService.delete(1L);
        verify(taskRepository, times(1)).deleteById(1L);
    }

    @Test
    void getAllReturnsAllTasks() {
        List<Task> tasks = Arrays.asList(
                new Task(1L, "Task 1", "Description 1", false),
                new Task(2L, "Task 2", "Description 2", true)
        );
        when(taskRepository.findAll()).thenReturn(tasks);
        List<Task> result = taskService.getAll();
        assertEquals(2, result.size());
        assertEquals("Task 1", result.get(0).getTaskName());
    }

    @Test
    void getByIdReturnsCorrectTask() {
        Task task = new Task(1L, "Task Name", "Task Description", false);
        when(taskRepository.findById(1L)).thenReturn(task);
        Task result = taskService.getById(1L);
        assertNotNull(result);
        assertEquals("Task Name", result.getTaskName());
    }

    @Test
    void getByIdReturnsNullForNonExistentTask() {
        when(taskRepository.findById(999L)).thenReturn(null);
        Task result = taskService.getById(999L);
        assertNull(result);
    }

    @Test
    void deleteAllDelegatesToRepository() {
        taskService.deleteAll();
        verify(taskRepository, times(1)).deleteAll();
    }

    @Test
    void printTableReturnsFormattedString() {
        when(taskRepository.printTasks()).thenReturn("Formatted Task Table");
        String result = taskService.printTable();
        assertEquals("Formatted Task Table", result);
    }

    @Test
    void clearAllDelegatesToDeleteAll() {
        taskService.clearAll();
        verify(taskRepository, times(1)).deleteAll();
    }
}