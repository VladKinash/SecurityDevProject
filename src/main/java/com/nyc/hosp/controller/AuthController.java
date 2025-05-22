package com.nyc.hosp.controller;

import com.nyc.hosp.domain.Hospuser;
import com.nyc.hosp.model.HospuserDTO;
import com.nyc.hosp.service.HospuserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    @Autowired
    private HospuserService hospuserService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private boolean isPasswordComplex(String password) {
        if (password == null) return true;
        return !password.matches("^(?=.*[0-9])(?=.*[!@#$%^&*()_+\\-={}:;'<>,.?/]).{9,}$");
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("hospuser") HospuserDTO hospuserDTO, Model model) {
        String password = hospuserDTO.getUserpassword();
        String repeatPassword = hospuserDTO.getUserpassword2();

        if (!password.equals(repeatPassword)) {
            model.addAttribute("error", "Passwords do not match");
            return "register/register";
        }

        if (isPasswordComplex(password)) {
            model.addAttribute("error", "Password must be at least 9 characters long and include a number and a special symbol.");
            return "register/register";
        }

        hospuserDTO.setUserpassword(passwordEncoder.encode(password));
        hospuserDTO.setRole(3);

        hospuserService.create(hospuserDTO);
        return "redirect:/login?registered";
    }

    @GetMapping("/auth-failure")
    public String authFailure(@RequestParam String reason, HttpServletRequest request, Model model) {
        String username = (String) request.getSession().getAttribute("authUsername");
        model.addAttribute("username", username);

        if ("expired".equals(reason)) {
            model.addAttribute("expired", true);
            return "login/forcePasswordChange";
        }

        model.addAttribute("error", switch (reason) {
            case "locked" -> "Your account is locked.";
            case "bad_credentials" -> "Invalid username or password.";
            default -> "Authentication failed.";
        });

        return "login/login";
    }

    @PostMapping("/auth-failure/change-password")
    public String updatePassword(@RequestParam String username,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 Model model) {
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("username", username);
            model.addAttribute("expired", true);
            model.addAttribute("error", "Passwords do not match");
            return "login/forcePasswordChange";
        }

        if (isPasswordComplex(newPassword)) {
            model.addAttribute("username", username);
            model.addAttribute("expired", true);
            model.addAttribute("error", "Password must be at least 9 characters long and include a number and a special symbol.");
            return "login/forcePasswordChange";
        }

        Hospuser user = hospuserService.findEntityByUsername(username);
        HospuserDTO dto = hospuserService.mapToDTO(user);
        dto.setUserpassword(passwordEncoder.encode(newPassword));
        dto.setLastchangepassword(java.time.OffsetDateTime.now());
        hospuserService.update(user.getUserId(), dto);

        return "redirect:/login?passwordUpdated";
    }
}

