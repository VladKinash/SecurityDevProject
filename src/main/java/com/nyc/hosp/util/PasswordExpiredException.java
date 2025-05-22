package com.nyc.hosp.util;

import org.springframework.security.core.AuthenticationException;


public class PasswordExpiredException extends AuthenticationException {
    public PasswordExpiredException(String msg) {
        super(msg);
    }
}
