package it.fds.taskmanager.rest.endpoints;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import it.fds.taskmanager.ReportService;
import it.fds.taskmanager.TaskState;

@RestController
@RequestMapping(value = "/report")
public class ReportController {

    private final Logger LOGGER = Logger.getLogger(ReportController.class);

    @Autowired
    private ReportService reportService;

    // DUMMY IMPLEMENTATION, JUST AN EXAMPLE TO SHOW WHAT TO USE
    @RequestMapping("/stats/{msg}")
    public String statistics(@PathVariable String msg) {
        LOGGER.info("Serving GET /report/" + msg + "endpoint...");

        try {
            String p = reportService.getNumberOfTasksWithPriority("3");
            String s = reportService.getNumberOfTaskWithStatus(TaskState.POSTPONED.toString());
        } catch (Exception e) {
            //VERY BAD EXCEPTION HANDLING
            LOGGER.error("Serving GET /report/{msg}... Error!");
        }

        LOGGER.info("Serving GET /report/{msg}... DONE!");
        return "Provided input message is '" + msg + "'";
    }


    @RequestMapping("/priority/{priority}")
    public String getNumberOFTasksByProirity(@PathVariable String priority) {
        String result;
        LOGGER.info("Service GET /report/priority/" + priority + " endpoints.");

        result = reportService.getNumberOfTasksWithPriority(priority);

        return result;
    }

    @RequestMapping("/status/{status}")
    public String getNumberOfTasksByStatus(@PathVariable String status) {
        String result;
        LOGGER.info("Service GET /report/status/" + status + " endpoints.");
        try {
            TaskState.valueOf(status);
            result = reportService.getNumberOfTaskWithStatus(status);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(status + " is not valid");
        }
        return result;
    }
}
