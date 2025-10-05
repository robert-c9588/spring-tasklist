package com.syntech.springtasklistcli.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TaskTests {

    @Test
    void constructorWithoutIdInitializesFieldsCorrectly() {
        Task task = new Task("Task Name", "Task Description", false);
        assertEquals(0, task.getId());
        assertEquals("Task Name", task.getTaskName());
        assertEquals("Task Description", task.getDescription());
        assertFalse(task.isCompleted());
    }

    @Test
    void constructorWithIdInitializesFieldsCorrectly() {
        Task task = new Task(1L, "Task Name", "Task Description", true);
        assertEquals(1L, task.getId());
        assertEquals("Task Name", task.getTaskName());
        assertEquals("Task Description", task.getDescription());
        assertTrue(task.isCompleted());
    }

    @Test
    void setIdUpdatesId() {
        Task task = new Task();
        task.setId(5L);
        assertEquals(5L, task.getId());
    }

    @Test
    void setTaskNameUpdatesName() {
        Task task = new Task();
        task.setTaskName("Updated Name");
        assertEquals("Updated Name", task.getTaskName());
    }

    @Test
    void setDescriptionUpdatesDescription() {
        Task task = new Task();
        task.setDescription("Updated Description");
        assertEquals("Updated Description", task.getDescription());
    }

    @Test
    void setCompletedUpdatesCompletionStatus() {
        Task task = new Task();
        task.setCompleted(true);
        assertTrue(task.isCompleted());
    }

    @Test
    void equalsReturnsTrueForEqualTasks() {
        Task task1 = new Task(1L, "Task Name", "Task Description", false);
        Task task2 = new Task(1L, "Task Name", "Task Description", false);
        assertEquals(task1, task2);
    }

    @Test
    void equalsReturnsFalseForDifferentTasks() {
        Task task1 = new Task(1L, "Task Name", "Task Description", false);
        Task task2 = new Task(2L, "Different Name", "Different Description", true);
        assertNotEquals(task1, task2);
    }

    @Test
    void hashCodeIsConsistentWithEquals() {
        Task task1 = new Task(1L, "Task Name", "Task Description", false);
        Task task2 = new Task(1L, "Task Name", "Task Description", false);
        assertEquals(task1.hashCode(), task2.hashCode());
    }

    @Test
    void toStringReturnsExpectedFormat() {
        Task task = new Task(1L, "Task Name", "Task Description", true);
        String expected = "Task{id=1, taskName='Task Name', description='Task Description', completed=true}";
        assertEquals(expected, task.toString());
    }
}