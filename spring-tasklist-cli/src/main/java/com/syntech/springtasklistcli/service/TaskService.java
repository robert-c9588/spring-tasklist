package com.syntech.springtasklistcli.service;

import com.syntech.springtasklistcli.domain.Task;
import com.syntech.springtasklistcli.repo.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class TaskService {
    @Autowired
    private TaskRepository taskRepository;

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TaskService.class);


    public TaskService(TaskRepository taskRepository) {

        this.taskRepository = taskRepository;
        log.info("Using repository: {}", taskRepository.getClass().getSimpleName());
        System.out.println("Using repository: " + taskRepository.getClass().getSimpleName());

    }

    public void add(String taskName, String description, Boolean completed) {
        this.taskRepository.add(new Task(taskName, description, completed));
    }

    public void complete(Long id) {
        this.taskRepository.complete(id);
    }

    public void update(Long id, String taskName, String description, Boolean completed) {
        this.taskRepository.update(id, taskName, description, completed);
    }

    public void delete(Long id) {
        this.taskRepository.deleteById(id);
    }

    public List<Task> getAll() {
        return this.taskRepository.findAll();
    }

    public Task getById(Long id) {
        return this.taskRepository.findById(id);
    }

    public void deleteAll() {
        this.taskRepository.deleteAll();
    }

    public String printTable() {
        return this.taskRepository.printTasks();
    }
}
