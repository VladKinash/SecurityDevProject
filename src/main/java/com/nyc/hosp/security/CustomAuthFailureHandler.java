package com.nyc.hosp.security;

import com.nyc.hosp.util.PasswordExpiredException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException ex) throws IOException {
        String username = request.getParameter("username");
        request.getSession().setAttribute("authUsername", username);

        if (ex instanceof CredentialsExpiredException) {
            response.sendRedirect("/auth-failure?reason=expired");
        } else if (ex.getMessage().toLowerCase().contains("locked")) {
            response.sendRedirect("/auth-failure?reason=locked");
        } else {
            response.sendRedirect("/auth-failure?reason=bad_credentials");
        }
    }
}


