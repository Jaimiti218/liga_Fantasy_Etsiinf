package com.ligainternaetsiinf.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ligainternaetsiinf.dto.LoginRequest;
import com.ligainternaetsiinf.dto.LoginResponse;
import com.ligainternaetsiinf.dto.RegisterRequest;
import com.ligainternaetsiinf.dto.UserResponse;
import com.ligainternaetsiinf.model.User;
import com.ligainternaetsiinf.repository.UserRepository;
import com.ligainternaetsiinf.security.CustomUserDetails;
import com.ligainternaetsiinf.service.UserService;

import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public UserResponse register(@RequestBody RegisterRequest user){
        
        return userService.registerUser(user.getEmail(),user.getUsername(), user.getPassword());
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest login, HttpServletRequest request){
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                login.getEmail(),
                login.getPassword()
            )
        );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        // Ensure the session is created and the security context is stored in it
        request.getSession(true).setAttribute(
            HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
            context
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        return new LoginResponse(
            userDetails.getId(),
            userDetails.getUsername(),
            userDetails.getAuthorities().iterator().next().getAuthority()
        );
    }


    @GetMapping("/me")
    public ResponseEntity<?> getUsuarioActual(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("No autenticado");
        }
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        // Necesitas el username real, no el email , asi que buscamos el usuario
        User user = userRepository.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return ResponseEntity.ok(new UserResponse(user.getId(), user.getUsername(), user.getRole()));
    }
}
