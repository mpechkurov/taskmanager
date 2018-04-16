package it.fds.taskmanager.rest.endpoints;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import it.fds.taskmanager.dto.TaskDTO;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

public class TasksControllerTest {
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
    private static final String CONFIG_FILE = "src/main/resources/application.properties";
    private static final String BASE_URL = "http://127.0.0.1:8080/task";
    private HttpClient httpClient;
    private Connection c;
    private Statement stmt;
    private Properties property;

    @Before
    public void setUp() {
        httpClient = HttpClients.createDefault();
        property = new Properties();
        try {
            property.load(new FileInputStream(CONFIG_FILE));
            c = DriverManager.getConnection(
                    property.getProperty("spring.datasource.url"),
                    property.getProperty("spring.datasource.username"),
                    property.getProperty("spring.datasource.password"));
            c.setAutoCommit(false);
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void list() {
        assertEquals(getRequestResult(), getDataBaseQueryResult());
    }

    @After
    public void tearDown() {
        try {
            stmt.close();
            c.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private List<TaskDTO> parseJsonStringToTaskDTO(String jsonArray) throws Exception {
        List<TaskDTO> splittedJsonElements = new ArrayList<>();
        ObjectMapper jsonMapper = new ObjectMapper();
        JsonNode jsonNode = jsonMapper.readTree(jsonArray);
        if (jsonNode.isArray()) {
            ArrayNode arrayNode = (ArrayNode) jsonNode;
            for (int i = 0; i < arrayNode.size(); i++) {
                JsonNode individualElement = arrayNode.get(i);
                splittedJsonElements.add(jsonMapper.readValue(individualElement.toString(), TaskDTO.class));
            }
        }
        return splittedJsonElements;
    }

    private List<String> prepareForCompare(List<TaskDTO> tasks) {
        List<String> result = new ArrayList<>();
        for (TaskDTO task : tasks) {
            result.add(
                    "\\x" + task.getUuid().toString().replace("-", "") + " " +
                            parseTimeToDbFormat(task.getCreatedat()) + " " +
                            parseTimeToDbFormat(task.getUpdatedat()) + " " +
                            parseTimeToDbFormat(task.getDuedate()) + " " +
                            parseTimeToDbFormat(task.getResolvedat()) + " " +
                            parseTimeToDbFormat(task.getPostponedat()) + " " +
                            task.getPostponedtime() + " " +
                            task.getTitle() + " " +
                            task.getDescription() + " " +
                            task.getPriority() + " " +
                            task.getStatus()
            );
        }
        return result;
    }

    private String parseTimeToDbFormat(Calendar calendar) {
        if (calendar != null) {
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
            return sdf.format(calendar.getTime());
        }
        return "null";
    }

    private List<String> getRequestResult() {
        List<String> requestResult = new ArrayList<>();
        HttpUriRequest request = RequestBuilder.get(BASE_URL + "/list")
                .addParameter("Cache-Control", "no-cache")
                .addParameter("Content-Type", "application/x-www-form-urlencoded; charset=utf-8").build();
        try {
            HttpResponse response = httpClient.execute(request);
            String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
            requestResult = prepareForCompare(parseJsonStringToTaskDTO(responseString));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return requestResult;
    }

    private List<String> getDataBaseQueryResult() {
        List<String> queryResult = new ArrayList<>();
        try {
            Class.forName("org.postgresql.Driver");
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM task;");
            while (rs.next()) {
                queryResult.add(rs.getString("uuid") + " " +
                        rs.getString("createdat") + " " +
                        rs.getString("updatedat") + " " +
                        rs.getString("duedate") + " " +
                        rs.getString("resolvedat") + " " +
                        rs.getString("postponedat") + " " +
                        rs.getString("postponedtime") + " " +
                        rs.getString("title") + " " +
                        rs.getString("description") + " " +
                        rs.getString("priority") + " " +
                        rs.getString("status"));
            }
            rs.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        return queryResult;
    }
}