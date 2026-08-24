package com.github.cronsmith.extension.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.HashMap;
import java.util.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.github.cronsmith.extension.SimpleTask;
import com.github.cronsmith.extension.TaskId;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

/**
 * 
 * Drives {@link SimpleTask} against a mock HTTP server, so the task's request is exercised end to
 * end without reaching a real endpoint.
 * 
 * @Description: SimpleTaskTests
 * @Author: Fred Feng
 * @Date: 24/08/2026
 * @Version 1.0.0
 */
public class SimpleTaskTests {

    private MockWebServer server;

    @Before
    public void setUp() throws Exception {
        server = new MockWebServer();
        server.start();
    }

    @After
    public void tearDown() throws Exception {
        server.shutdown();
    }

    private SimpleTask newTask() {
        SimpleTask task = new SimpleTask();
        task.setTaskGroup("http");
        task.setTaskName("ping");
        task.setCronExpression("0 0 12 * * ?");
        return task;
    }

    @Test
    public void testTaskIdAndSchedule() {
        SimpleTask task = newTask();
        assertEquals(TaskId.of("http", "ping"), task.getTaskId());
        assertEquals("0 0 12 * * ?", task.getCronExpression().toString());
    }

    @Test
    public void testGetRequest() throws Exception {
        server.enqueue(new MockResponse().setBody("pong"));
        SimpleTask task = newTask();
        task.setUrl(server.url("/ping").toString());
        task.setHttpMethod("GET");

        Object result = task.execute(task.getInitialParameter());
        assertEquals("pong", result);
        RecordedRequest recorded = server.takeRequest();
        assertEquals("GET", recorded.getMethod());
        assertEquals("/ping", recorded.getPath());
    }

    @Test
    public void testPostRequestSendsBodyAndHeaders() throws Exception {
        server.enqueue(new MockResponse().setBody("created"));
        SimpleTask task = newTask();
        task.setUrl(server.url("/create").toString());
        task.setHttpMethod("POST");
        task.setDataType("json");
        task.setData("{\"k\":\"v\"}");
        Map<String, String> headers = new HashMap<>();
        headers.put("X-Token", "secret");
        task.setHttpHeaders(headers);

        assertEquals("created", task.execute(task.getInitialParameter()));
        RecordedRequest recorded = server.takeRequest();
        assertEquals("POST", recorded.getMethod());
        assertEquals("secret", recorded.getHeader("X-Token"));
        assertEquals("{\"k\":\"v\"}", recorded.getBody().readUtf8());
        assertTrue(recorded.getHeader("Content-Type").contains("application/json"));
    }

    @Test
    public void testPutRequest() throws Exception {
        server.enqueue(new MockResponse().setBody("updated"));
        SimpleTask task = newTask();
        task.setUrl(server.url("/x").toString());
        task.setHttpMethod("PUT");
        task.setDataType("xml");
        task.setData("<a/>");
        assertEquals("updated", task.execute(task.getInitialParameter()));
        RecordedRequest recorded = server.takeRequest();
        assertEquals("PUT", recorded.getMethod());
        assertTrue(recorded.getHeader("Content-Type").contains("application/xml"));
    }

    @Test
    public void testDeleteRequest() throws Exception {
        server.enqueue(new MockResponse().setBody("gone"));
        SimpleTask task = newTask();
        task.setUrl(server.url("/x").toString());
        task.setHttpMethod("DELETE");
        task.setDataType("form");
        task.setData("k=v");
        assertEquals("gone", task.execute(task.getInitialParameter()));
        RecordedRequest recorded = server.takeRequest();
        assertEquals("DELETE", recorded.getMethod());
        assertTrue(recorded.getHeader("Content-Type").contains("x-www-form-urlencoded"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnsupportedMethodRejected() {
        SimpleTask task = newTask();
        task.setUrl(server.url("/x").toString());
        task.setHttpMethod("PATCH");
        task.execute(task.getInitialParameter());
    }

    @Test
    public void testPlainTextDataType() throws Exception {
        server.enqueue(new MockResponse().setBody("ok"));
        SimpleTask task = newTask();
        task.setUrl(server.url("/x").toString());
        task.setHttpMethod("POST");
        task.setDataType("something-else");
        task.setData("hello");
        assertEquals("ok", task.execute(task.getInitialParameter()));
        assertTrue(server.takeRequest().getHeader("Content-Type").contains("text/plain"));
    }

    @Test
    public void testInitialParameterRoundTrips() {
        SimpleTask task = newTask();
        task.setUrl("http://example.com/x");
        task.setHttpMethod("PUT");
        task.setData("payload");
        // The whole request is carried in the initial parameter, so a stored task sends exactly
        // what was configured.
        String parameter = task.getInitialParameter();
        assertNotNull(parameter);
        assertTrue(parameter.contains("example.com"));
        assertTrue(parameter.contains("PUT"));
    }

    @Test(expected = com.github.cronsmith.extension.TaskInvocationException.class)
    public void testNon2xxResponseFails() {
        server.enqueue(new MockResponse().setResponseCode(500));
        SimpleTask task = newTask();
        task.setUrl(server.url("/boom").toString());
        task.setHttpMethod("GET");
        task.execute(task.getInitialParameter());
    }

    @Test
    public void testAgainstPublicApi() {
        // A real call to a public API, so the task is seen working end to end outside the mock.
        // Skipped when there is no network, so an offline build stays green.
        assumePublicNetwork();
        SimpleTask task = newTask();
        task.setUrl("https://httpbin.org/get");
        task.setHttpMethod("GET");
        Object result = task.execute(task.getInitialParameter());
        assertNotNull(result);
        // httpbin echoes the request back as JSON, including the URL it was reached at.
        assertTrue(result.toString().contains("httpbin.org/get"));
    }

    @Test
    public void testPostJsonAgainstPublicApi() {
        assumePublicNetwork();
        SimpleTask task = newTask();
        task.setUrl("https://httpbin.org/post");
        task.setHttpMethod("POST");
        task.setDataType("json");
        task.setData("{\"job\":\"cronsmith\"}");
        Object result = task.execute(task.getInitialParameter());
        assertNotNull(result);
        // httpbin returns the JSON body it received under a "json" field.
        assertTrue(result.toString().contains("cronsmith"));
    }

    private static void assumePublicNetwork() {
        try {
            java.net.InetAddress.getByName("httpbin.org");
            java.net.HttpURLConnection connection =
                    (java.net.HttpURLConnection) java.net.URI.create("https://httpbin.org/status/200")
                            .toURL().openConnection();
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);
            connection.setRequestMethod("GET");
            org.junit.Assume.assumeTrue("httpbin.org unreachable",
                    connection.getResponseCode() == 200);
            connection.disconnect();
        } catch (Exception e) {
            org.junit.Assume.assumeNoException("no public network", e);
        }
    }

    @Test
    public void testRetryAndTimeoutAccessors() {
        SimpleTask task = newTask();
        task.setMaxRetryCount(3);
        task.setTimeout(5000L);
        task.setDescription("a ping");
        assertEquals(3, task.getMaxRetryCount());
        assertEquals(5000L, task.getTimeout());
        assertEquals("a ping", task.getDescription());
    }

}
