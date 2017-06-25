package com.zms.zpc.support;

/**
 * Created by 张小美 on 17/六月/24.
 * Copyright 2002-2016
 */
public class NotImplException extends RuntimeException {

    public NotImplException() {
        super();
    }

    public NotImplException(String message) {
        super(message);
    }

    public NotImplException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotImplException(Throwable cause) {
        super(cause);
    }

    protected NotImplException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
