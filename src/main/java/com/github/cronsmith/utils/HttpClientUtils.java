package com.github.cronsmith.utils;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import okhttp3.ConnectionPool;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 
 * A thin OkHttp wrapper used by tasks that do their work by calling an HTTP endpoint. The client is
 * shared: OkHttp is designed to be created once and reused, and a per-call client would leak both
 * its connection pool and its dispatcher threads.
 * 
 * <p>
 * This class is only reachable when OkHttp is on the classpath, which is an optional dependency of
 * this library.
 * 
 * @Description: HttpClientUtils
 * @Author: Fred Feng
 * @Date: 30/04/2025
 * @Version 1.0.0
 */
public abstract class HttpClientUtils {

    private static final ConnectionPool CONNECTION_POOL =
            new ConnectionPool(200, 10, TimeUnit.MINUTES);

    private static final OkHttpClient CLIENT =
            new OkHttpClient.Builder().connectionPool(CONNECTION_POOL)
                    .connectTimeout(10, TimeUnit.SECONDS).readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS).retryOnConnectionFailure(true).build();

    /**
     * Sends a request and returns the response body.
     * 
     * @param dataType one of json, xml, form; anything else is sent as plain text
     * @throws IOException if the call fails or the response status is not successful
     */
    public static String sendRequest(String url, String httpMethod, Map<String, String> httpHeaders,
            String dataType, String data) throws IOException {
        if (httpMethod == null) {
            throw new IllegalArgumentException("HTTP method is required");
        }
        RequestBody body = null;
        String method = httpMethod.toUpperCase();
        if ("POST".equals(method) || "PUT".equals(method) || "DELETE".equals(method)) {
            body = RequestBody.create(data != null ? data : "", getMediaType(dataType));
        }
        Request.Builder requestBuilder = new Request.Builder().url(url);
        if (httpHeaders != null) {
            httpHeaders.forEach(requestBuilder::addHeader);
        }
        switch (method) {
            case "GET":
                requestBuilder.get();
                break;
            case "POST":
                requestBuilder.post(body);
                break;
            case "PUT":
                requestBuilder.put(body);
                break;
            case "DELETE":
                requestBuilder.delete(body);
                break;
            default:
                throw new IllegalArgumentException("Unsupported HTTP method: " + httpMethod);
        }
        return execute(requestBuilder.build());
    }

    /**
     * Uploads a file as multipart form data, optionally alongside a second part carrying
     * {@code data}.
     */
    public static String uploadFile(String url, File file, String data, String dataType,
            Map<String, String> headers) throws IOException {
        MediaType fileMediaType = MediaType.parse("application/octet-stream");
        MultipartBody.Builder multipartBuilder =
                new MultipartBody.Builder().setType(MultipartBody.FORM);
        multipartBuilder.addFormDataPart("file", file.getName(),
                RequestBody.create(file, fileMediaType));
        if (data != null) {
            multipartBuilder.addFormDataPart("data", null,
                    RequestBody.create(data, getMediaType(dataType)));
        }
        Request.Builder requestBuilder =
                new Request.Builder().url(url).post(multipartBuilder.build());
        if (headers != null) {
            headers.forEach(requestBuilder::addHeader);
        }
        return execute(requestBuilder.build());
    }

    private static String execute(Request request) throws IOException {
        try (Response response = CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected HTTP code: " + response.code());
            }
            return response.body() != null ? response.body().string() : null;
        }
    }

    private static MediaType getMediaType(String dataType) {
        if ("json".equalsIgnoreCase(dataType)) {
            return MediaType.parse("application/json; charset=utf-8");
        } else if ("xml".equalsIgnoreCase(dataType)) {
            return MediaType.parse("application/xml; charset=utf-8");
        } else if ("form".equalsIgnoreCase(dataType)) {
            return MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");
        } else {
            return MediaType.parse("text/plain; charset=utf-8");
        }
    }

}
