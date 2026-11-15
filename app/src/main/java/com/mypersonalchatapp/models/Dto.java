package com.mypersonalchatapp.models;

public class Dto {

    // Not "= true" inline: the compiler would hard-code it into isSuccess() and ignore the JSON value.
    private final boolean success;
    private final String message;
    private final Object data;
    private final int statusCode;
    private final Object extraData;

    public Dto(Object data, String message, int statusCode, Object extraData) {
        this.success = true;
        this.data = data;
        this.message = message;
        this.statusCode = statusCode;
        this.extraData = extraData;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Object getData() {
        return data;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public Object getExtraData() {
        return extraData;
    }
}
