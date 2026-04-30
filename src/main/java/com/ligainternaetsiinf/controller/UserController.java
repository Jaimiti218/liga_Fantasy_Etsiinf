package com.ligainternaetsiinf.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ligainternaetsiinf.dto.LoginRequest;
import com.ligainternaetsiinf.dto.LoginResponse;
import com.ligainternaetsiinf.dto.RegisterRequest;
import com.ligainternaetsiinf.dto.UserResponse;
import com.ligainternaetsiinf.model.User;
import com.ligainternaetsiinf.service.UserService;

@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public UserResponse register(@RequestBody RegisterRequest user){
        
        return userService.registerUser(user.getEmail(),user.getUsername(), user.getPassword());
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest login){
        return userService.loginUser(login.getEmail(), login.getPassword());
    }
}
