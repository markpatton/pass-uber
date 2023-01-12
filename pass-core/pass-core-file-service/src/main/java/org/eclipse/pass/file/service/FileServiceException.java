package org.eclipse.pass.file.service;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;

public class FileServiceException extends Exception {

    //private int code = 0;
    private HttpStatus code = null;
    private Map<String, List<String>> responseHeaders = null;
    private String responseBody = null;

    public FileServiceException() {}

    public FileServiceException(String message) {
        super(message);
    }

    public FileServiceException(String message, Throwable throwable, HttpStatus code, Map<String,
            List<String>> responseHeaders, String responseBody) {
        super(message, throwable);
        this.code = code;
        this.responseHeaders = responseHeaders;
        this.responseBody = responseBody;
    }

    public FileServiceException(String message, HttpStatus code, Map<String, List<String>> responseHeaders,
        String responseBody) {
        this(message, (Throwable) null, code, responseHeaders, responseBody);
    }

    public FileServiceException(String message, Throwable throwable, HttpStatus code, Map<String,
        List<String>> responseHeaders) {
        this(message, throwable, code, responseHeaders, null);
    }

    public FileServiceException(HttpStatus code, Map<String, List<String>> responseHeaders, String responseBody) {
        this((String) null, (Throwable) null, code, responseHeaders, responseBody);
    }

    public FileServiceException(HttpStatus code, String message) {
        super(message);
        this.code = code;
    }

    public FileServiceException(HttpStatus code, String message, Map<String, List<String>> responseHeaders,
                                String responseBody) {
        this(code, message);
        this.responseHeaders = responseHeaders;
        this.responseBody = responseBody;
    }

    /**
     * Get the HTTP status code.
     *
     * @return HTTP status code
     */
    public HttpStatus getCode() {
        return code;
    }

    /**
     * Get the HTTP response headers.
     *
     * @return A map of list of string
     */
    public Map<String, List<String>> getResponseHeaders() {
        return responseHeaders;
    }

    /**
     * Get the HTTP response body.
     *
     * @return Response body in the form of string
     */
    public String getResponseBody() {
        return responseBody;
    }

}
