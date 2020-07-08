package com.android.blendershape3;

interface ErrorHandler {
    enum ErrorType {
        BUFFER_CREATION_ERROR
    }

    void handleError(ErrorType errorType, String cause);
}