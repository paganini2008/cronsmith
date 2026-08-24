package com.github.cronsmith.extension;

import java.io.IOException;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.cronsmith.CRON;
import com.github.cronsmith.cron.CronExpression;
import com.github.cronsmith.utils.HttpClientUtils;
/**
 * 
 * A task whose work is a single HTTP request. Everything it needs is a value, so it can be built
 * from a form or a configuration file without any code being written for it.
 * 
 * <p>
 * The request is carried in the initial parameter rather than read from the fields at run time.
 * That way a task restored from storage sends exactly the request that was registered, even though
 * the object it was configured on is long gone.
 * 
 * <p>
 * Requires Jackson and OkHttp, both optional dependencies of this library.
 * 
 * @Description: SimpleTask
 * @Author: Fred Feng
 * @Date: 30/04/2025
 * @Version 1.0.0
 */
public class SimpleTask implements Task {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private String taskGroup = TaskId.DEFAULT_GROUP;
    private String taskName;
    private String url;
    private String httpMethod = "GET";
    private Map<String, String> httpHeaders;
    private String dataType;
    private String data;
    private String description;
    private String cronExpression;
    private int maxRetryCount;
    private long timeout = -1L;

    public String getTaskGroup() {
        return taskGroup;
    }

    public void setTaskGroup(String taskGroup) {
        this.taskGroup = taskGroup;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public Map<String, String> getHttpHeaders() {
        return httpHeaders;
    }

    public void setHttpHeaders(Map<String, String> httpHeaders) {
        this.httpHeaders = httpHeaders;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getCronExpressionText() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    @Override
    public int getMaxRetryCount() {
        return maxRetryCount;
    }

    public void setMaxRetryCount(int maxRetryCount) {
        this.maxRetryCount = maxRetryCount;
    }

    @Override
    public TaskId getTaskId() {
        return TaskId.of(taskGroup, taskName);
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public CronExpression getCronExpression() {
        return CRON.parse(cronExpression);
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    @Override
    public long getTimeout() {
        return timeout;
    }

    @Override
    public String getInitialParameter() {
        HttpRequestSpec spec = new HttpRequestSpec();
        spec.setUrl(this.url);
        spec.setHttpMethod(this.httpMethod);
        spec.setHttpHeaders(this.httpHeaders);
        spec.setDataType(this.dataType);
        spec.setData(this.data);
        try {
            return OBJECT_MAPPER.writeValueAsString(spec);
        } catch (IOException e) {
            throw new TaskException("Cannot serialize the request for task " + getTaskId(), e);
        }
    }

    @Override
    public Object execute(String initialParameter) {
        HttpRequestSpec spec;
        try {
            spec = OBJECT_MAPPER.readValue(initialParameter, HttpRequestSpec.class);
        } catch (IOException e) {
            throw new TaskException("Cannot read the request for task " + getTaskId(), e);
        }
        try {
            return HttpClientUtils.sendRequest(spec.getUrl(), spec.getHttpMethod(),
                    spec.getHttpHeaders(), spec.getDataType(), spec.getData());
        } catch (IOException e) {
            throw new TaskInvocationException(e.getMessage(), e);
        }
    }

    /**
     * 
     * The request, in the form it is stored in.
     * 
     * @Description: HttpRequestSpec
     * @Author: Fred Feng
     * @Date: 30/04/2025
     * @Version 1.0.0
     */
    public static class HttpRequestSpec {

        private String url;
        private String httpMethod;
        private Map<String, String> httpHeaders;
        private String dataType;
        private String data;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getHttpMethod() {
            return httpMethod;
        }

        public void setHttpMethod(String httpMethod) {
            this.httpMethod = httpMethod;
        }

        public Map<String, String> getHttpHeaders() {
            return httpHeaders;
        }

        public void setHttpHeaders(Map<String, String> httpHeaders) {
            this.httpHeaders = httpHeaders;
        }

        public String getDataType() {
            return dataType;
        }

        public void setDataType(String dataType) {
            this.dataType = dataType;
        }

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }

    }

}
