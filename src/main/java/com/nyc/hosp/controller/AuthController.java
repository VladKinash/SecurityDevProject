package com.nyc.hosp.controller;

import com.nyc.hosp.domain.Hospuser;
import com.nyc.hosp.model.HospuserDTO;
import com.nyc.hosp.service.HospuserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@Controller
public class AuthController {

    @Autowired
    private HospuserService hospuserService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private boolean isPasswordComplex(String password) {
        if (password == null) return false;
        return password.matches("^(?=.*[0-9])(?=.*[!@#$%^&*()_+\\-={}:;'<>,.?/]).{9,}$");
    }


    @PostMapping("/register")
    public String registerUser(@ModelAttribute("hospuser") HospuserDTO hospuserDTO, Model model) {
        String password = hospuserDTO.getUserpassword();
        String repeatPassword = hospuserDTO.getUserpassword2();

        if(!password.equals(repeatPassword)) {
            model.addAttribute("error", "Passwords do not match");
            return "register/register";
        }

        if (!isPasswordComplex(password)) {
            model.addAttribute("error", "Password must be at least 9 characters long and include a number and a special symbol.");
            return "register/register";
        }

        hospuserDTO.setUserpassword(passwordEncoder.encode(hospuserDTO.getUserpassword()));

        hospuserDTO.setRole(3);

        System.out.println("Registering user: " + hospuserDTO.getUsername() + " with role ID: " + hospuserDTO.getRole());

        Integer newId = hospuserService.create(hospuserDTO);
        System.out.println("Created user with ID: " + newId);

        return "redirect:/login?registered";
    }


}
