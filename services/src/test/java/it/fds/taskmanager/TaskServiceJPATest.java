package it.fds.taskmanager;

import it.fds.taskmanager.dto.TaskDTO;
import it.fds.taskmanager.repository.TasksRepository;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import static it.fds.taskmanager.TaskState.*;
import static java.util.Calendar.MINUTE;
import static java.util.Calendar.getInstance;

/**
 * Basic test suite to test the service layer, it uses an in-memory H2 database.
 *
 * TODO Add more and meaningful tests! :)
 *
 * @author <a href="mailto:damiano@searchink.com">Damiano Giampaoli</a>
 * @since 10 Jan. 2018
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class TaskServiceJPATest extends Assert{

    @Autowired
    private TaskService taskService;
    @Autowired
    private TasksRepository tasksRepository;
    private TaskDTO t;
    private static int MINUTES_TO_POSTPONE = 10;

    @Before
    public void setUp() {
        t = new TaskDTO();
    }

    @Test
    public void writeAndReadOnDB() {
        t.setTitle("Test task1");
        t.setStatus(NEW.toString().toUpperCase());
        TaskDTO t1 = taskService.saveTask(t);
        TaskDTO tOut = taskService.findOne(t1.getUuid());
        assertEquals("Test task1", tOut.getTitle());
        List<TaskDTO> list = taskService.showList();
        assertEquals(1, list.size());
    }

    @Test
    public void showList_ShouldReturnAllTaskExceptPostponed() {
        t.setTitle("Test new");
        t.setStatus(NEW.toString().toUpperCase());
        taskService.saveTask(t);
        t.setTitle("Test");
        t.setStatus(POSTPONED.toString().toUpperCase());
        taskService.saveTask(t);
        t.setTitle("Test Resolved");
        t.setStatus(RESOLVED.toString().toUpperCase());
        taskService.saveTask(t);
        t.setTitle("Test Restored");
        t.setStatus(RESTORED.toString().toUpperCase());
        taskService.saveTask(t);
        List<TaskDTO> resultListOfTasks = taskService.showList();
        assertEquals(3, resultListOfTasks.size());
        for (TaskDTO task : resultListOfTasks) {
            assertFalse(task.getStatus().equals(POSTPONED.toString().toUpperCase()));
        }
    }

    @Test
    public void findOne_ShouldReturnTaskByUuid() {
        t.setTitle("FindOneTest");
        t.setStatus(NEW.toString().toUpperCase());
        TaskDTO actual = taskService.saveTask(t);
        assertEquals(t.getTitle(), taskService.findOne(actual.getUuid()).getTitle());
        //I would like use this if we can skip changing Uuid during taskService.saveTask()
        //assertTrue(EqualsBuilder.reflectionEquals(t, taskService.findOne(actual.getUuid())));
    }

    @Test
    public void saveTask_ShouldReturnTaskDTOWithRandomUuid() {
        t.setTitle("saveTask");
        t.setStatus(NEW.toString().toUpperCase());
        t.setUuid(UUID.fromString("12345-12345-12345-12345-12345"));
        TaskDTO actualResult = taskService.saveTask(t);
        assertEquals("Name should be the same", t.getTitle(), actualResult.getTitle());
        assertNotEquals("Uuid should be different", t.getTitle(), actualResult.getUuid());
    }

    @Test
    public void saveTask_ShouldSaveTaskWithRandomUuid() {
        t.setTitle("saveTask");
        t.setStatus(NEW.toString().toUpperCase());
        t.setUuid(UUID.fromString("12345-12345-12345-12345-12345"));
        TaskDTO savedTask = taskService.saveTask(t);
        TaskDTO actualResult = taskService.findOne(savedTask.getUuid());
        //Test assume that this is correct behaviour.
        //But I have create issues for this - https://github.com/mpechkurov/taskmanager/issues/1
        assertEquals("Names should be equal. ", t.getTitle(), actualResult.getTitle());
        assertNotEquals("Uuid should be different. ", t.getUuid(), actualResult.getUuid());
    }

    @Test
    public void updateTask_shouldUpdateDate() {
        t.setTitle("UpdateTask");
        t.setStatus(NEW.toString().toUpperCase());
        t.setDescription("It's never enough");
        t.setUpdatedat(getInstance());
        TaskDTO beforeUpdate = taskService.saveTask(t);
        assertNotEquals("Date should be different ", beforeUpdate.getUpdatedat(), taskService.updateTask(beforeUpdate).getUpdatedat());
    }


    @Test
    public void resolveTask_ShouldChangeStatusToResolved() {
        t.setTitle("ResolveTask");
        t.setStatus(NEW.toString().toUpperCase());
        t.setUpdatedat(getInstance());
        TaskDTO actualResult = taskService.saveTask(t);
        taskService.resolveTask(actualResult.getUuid());
        assertEquals(RESOLVED.toString().toUpperCase(), taskService.findOne(actualResult.getUuid()).getStatus());
    }

    @Test
    public void postponeTask_ShouldChangeStatusToPostpone() {
        t.setTitle("PostponedTask");
        t.setStatus(NEW.toString().toUpperCase());
        t.setPostponedat(getInstance());
        TaskDTO actualResult = taskService.saveTask(t);
        taskService.postponeTask(actualResult.getUuid(), MINUTES_TO_POSTPONE);
        assertEquals(POSTPONED.toString().toUpperCase(), taskService.findOne(actualResult.getUuid()).getStatus());
    }

    @Test
    public void postponeTask_ShouldIncreaseMinutes() {
        t.setTitle("PostponedTask");
        t.setStatus(NEW.toString().toUpperCase());
        t.setPostponedat(getInstance());
        TaskDTO actualResult = taskService.saveTask(t);
        taskService.postponeTask(actualResult.getUuid(), MINUTES_TO_POSTPONE);
        assertEquals(actualResult.getPostponedat().get(MINUTE) + MINUTES_TO_POSTPONE, taskService.findOne(actualResult.getUuid()).getPostponedat().get(MINUTE));
    }

    @Test
    public void unmarkPostoned_shouldChangeAllPostponeToRestore() {
        t.setTitle("PostponedRestore1");
        t.setStatus(POSTPONED.toString().toUpperCase());
        t.setPostponedat(getInstance());
        TaskDTO actualResult = taskService.saveTask(t);
        taskService.unmarkPostoned();
        TaskDTO result = taskService.findOne(actualResult.getUuid());
        assertEquals(RESTORED.toString().toUpperCase(), result.getStatus());
        assertNull(result.getPostponedat());
    }


    @Test
    public void unmarkPostoned_shouldNotChangeWithPostponeDateMoreThanNow() {
        t.setTitle("PostponedRestore1");
        t.setStatus(POSTPONED.toString().toUpperCase());
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MINUTE, MINUTES_TO_POSTPONE);
        t.setPostponedat(c);
        TaskDTO actualResult = taskService.saveTask(t);
        taskService.unmarkPostoned();
        assertEquals(POSTPONED.toString().toUpperCase(), taskService.findOne(actualResult.getUuid()).getStatus());
    }

    @Test
    public void unmarkPostponed_shuouldNotChangeStatusForNotPostponed() {
        t.setTitle("Test new");
        t.setStatus(NEW.toString().toUpperCase());
        taskService.saveTask(t);
        t.setTitle("Test Resolved");
        t.setStatus(RESOLVED.toString().toUpperCase());
        taskService.saveTask(t);
        t.setTitle("Test Restored");
        t.setStatus(RESTORED.toString().toUpperCase());
        taskService.saveTask(t);
        taskService.unmarkPostoned();
        List<TaskDTO> resultListOfTasks = taskService.showList();
        for (TaskDTO task : resultListOfTasks) {
            assertFalse(task.getStatus().equals(POSTPONED.toString().toUpperCase()));
        }
    }


    @After
    public void clearData() {
        tasksRepository.deleteAll();
    }

    @EnableJpaRepositories
    @Configuration
    @SpringBootApplication
    public static class EndpointsMain{}
}
