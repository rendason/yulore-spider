package org.tafia.spider.component;

/**
 * Created by Dason on 2017/6/17.
 */
public class ApiResponse {

    private int status;

    private String message;

    private Object data;

    private ApiResponse() {
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public Object getData() {
        return data;
    }

    public static ApiResponse custom(int status, String message, Object data) {
        ApiResponse response = new ApiResponse();
        response.status = status;
        response.message = message;
        response.data = data;
        return response;
    }

    public static ApiResponse custom(int status, String message) {
        return custom(status, message, null);
    }

}
