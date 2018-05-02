package it.fds.taskmanager;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.fds.taskmanager.repository.TasksRepository;

@Service
public class ReportServiceJPA implements ReportService{

    private final Logger LOGGER = Logger.getLogger(ReportServiceJPA.class);
    
    @Autowired
    private TasksRepository tasksRepo;
    
    @Override
    public String getNumberOfTaskWithStatus(String state) {
        return tasksRepo.getNumberOfTaskWithStatus(state);
    }

    @Override
    public String getNumberOfTasksWithPriority(String priority) {
        return tasksRepo.getNumberOfTaskWithPriority( priority);
    }

}
