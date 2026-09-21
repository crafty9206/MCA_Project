package com.maatricare.tracking;

import java.util.List;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class TaskDetailSeeder implements ApplicationRunner {

    private static final List<String> DEFAULT_TASKS = List.of(
            "Take prenatal vitamins",
            "Drink 8 glasses of water",
            "Take a 20 minute walk",
            "Get enough rest");

    private final TaskDetailRepository taskDetailRepository;

    public TaskDetailSeeder(TaskDetailRepository taskDetailRepository) {
        this.taskDetailRepository = taskDetailRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        DEFAULT_TASKS.forEach(title -> taskDetailRepository.findByTitleIgnoreCase(title)
                .orElseGet(() -> taskDetailRepository.save(new TaskDetail(title))));
    }
}