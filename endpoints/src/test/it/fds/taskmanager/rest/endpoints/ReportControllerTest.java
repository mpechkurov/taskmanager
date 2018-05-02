package it.fds.taskmanager.rest.endpoints;

import it.fds.taskmanager.*;
import it.fds.taskmanager.dto.TaskDTO;
import it.fds.taskmanager.repository.TasksRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.util.NestedServletException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@ContextConfiguration(classes = {EndpointsMain.class})
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
public class ReportControllerTest {

    @Autowired
    private TaskService taskService;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private TasksRepository tasksRepository;

    @Rule
    public ExpectedException exception = ExpectedException.none();
    private MockMvc mockMvc;
    private TaskDTO task;

    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
        task = new TaskDTO();
    }

    @Test
    public void getNumberOFTasksByPriority_correctResult() throws Exception {
        task.setTitle("Test task1");
        task.setPriority("1");
        taskService.saveTask(task);
        mockMvc.perform(get("/report/priority/1")).andExpect(status().isOk()).andExpect(content().string("1"));
    }

    @Test
    public void getNumberOFTasksByPriority_zeroResult() throws Exception {
        task.setTitle("Test task1");
        task.setPriority("1");
        taskService.saveTask(task);
        mockMvc.perform(get("/report/priority/2")).andExpect(status().isOk()).andExpect(content().string("0"));
    }

    @Test
    public void getNumberOfTasksByStatus_correctResult() throws Exception {
        task.setTitle("Test task1");
        task.setStatus(TaskState.NEW.toString().toUpperCase());
        taskService.saveTask(task);
        mockMvc.perform(get("/report/status/NEW")).andExpect(status().isOk()).andExpect(content().string("1"));
    }

    @Test
    public void getNumberOfTasksByStatus_zeroResult() throws Exception {
        task.setTitle("Test task1");
        task.setStatus(TaskState.NEW.toString().toUpperCase());
        taskService.saveTask(task);
        mockMvc.perform(get("/report/status/POSTPONED")).andExpect(status().isOk()).andExpect(content().string("0"));
    }

    @Test(expected = NestedServletException.class)
    public void getNumberOfTasksByStatus_errorHandling() throws Exception {
        mockMvc.perform(get("/report/status/WRONG")).andExpect(content().string("WRONG is not valid"));
    }

    @After
    public void clearData() {
        tasksRepository.deleteAll();
    }

    @EnableJpaRepositories
    @Configuration
    @SpringBootApplication
    public static class EndpointsMain {
    }
}