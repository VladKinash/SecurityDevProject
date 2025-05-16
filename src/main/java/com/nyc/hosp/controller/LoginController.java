package com.nyc.hosp.controller;

import com.nyc.hosp.model.LoginFormDTO;
import org.springframework.security.web.WebAttributes;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("loginForm", new LoginFormDTO());
        return "login/login";
    }

    @GetMapping("/login-error")
    public String loginError(HttpServletRequest request, Model model) {
        Exception ex = (Exception) request.getSession()
                .getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
        String errorMessage = (ex != null ? ex.getMessage() : "Authentication failed");

        model.addAttribute("loginForm", new LoginFormDTO());
        model.addAttribute("loginError", true);
        model.addAttribute("errorMessage", errorMessage);

        return "login/login";
    }
}
