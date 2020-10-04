package com.couchbase.mobile.mfd.util;

import android.util.Log;

/**
 * A generic class that holds a result success w/ data or an error exception.
 */
public class Result<T> {
    public static String LOG_TAG = "Result";

    // hide the private constructor to limit subclass types (Success, Error)
    private Result() {
    }

    @Override
    public String toString() {
        if (this instanceof Result.Success) {
            Result.Success success = (Result.Success) this;
            return "Success[data=" + success.getData().toString() + "]";
        } else if (this instanceof Result.Error) {
            Result.Error error = (Result.Error) this;
            if (error.getException() == null) {
                return "Error[message=" + ResourceLocalizer.getLocalizedString(error.messageId) + "]";
            } else {
                return "Error[message=" + ResourceLocalizer.getLocalizedString(error.messageId) + " exception=" + error.getException().toString() + "]";
            }
        }
        return "Unknown result type";
    }


    public T render(OnSuccess<T> onSuccess, OnError<T> onError) {
        Log.e(LOG_TAG, "Unexpected execution of render in com.couchbase.mobile.mfd.util.Result superclass.");
        return null;
    }


    // Success sub-class
    public final static class Success<T> extends Result {
        private T data;

        public Success(T data) {
            this.data = data;
        }

        public T getData() {
            return this.data;
        }

        @Override
        @SuppressWarnings("unchecked")
        public T render(OnSuccess onSuccess, OnError onError) {
            onSuccess.process(getData());
            return getData();
        }

    }

    // Error sub-class
    public final static class Error<T> extends Result {
        private Exception error;
        private int messageId;

        public Error(int messageId) {
            this.messageId = messageId;
        }

        public Error(int messageId, Exception error) {
            this.messageId = messageId;
            this.error = error;
        }

        public Exception getException() {
            return this.error;
        }

        public String getMessage() {

            return ResourceLocalizer.getLocalizedString(messageId);
        }

        public int getMessageId() {
            return messageId;
        };

        @Override
        @SuppressWarnings("unchecked")
        public T render(OnSuccess onSuccess, OnError onError) {
            onError.process(ResourceLocalizer.getLocalizedString(messageId), error);
            return null;
        }

    }



    @FunctionalInterface
    public interface OnSuccess<T> {
        public T process(T result);
    }

    @FunctionalInterface
    public interface OnError<T> {
        public T process(String message, Exception e);
    }
 }