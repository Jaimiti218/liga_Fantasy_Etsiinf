package com.ligainternaetsiinf.service;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ligainternaetsiinf.dto.LoginResponse;
import com.ligainternaetsiinf.dto.UserResponse;
import com.ligainternaetsiinf.model.User;
import com.ligainternaetsiinf.repository.UserRepository;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserResponse registerUser(String email, String username, String password){
        // comprobar si el email ya existe
        if(userRepository.findByEmail(email).isPresent()){
            throw new RuntimeException("Ya existe una cuenta con este correo!");
        }

        User user = new User(email, passwordEncoder.encode(password), username);
        userRepository.save(user);
        return (new UserResponse(user.getId(), user.getUsername(), user.getRole(), user.getFotoPerfil()));
    }

    public LoginResponse loginUser(String email){
        Optional<User> aux = userRepository.findByEmail(email);
        if(!aux.isPresent()){
            
            throw new RuntimeException("El correo o la contraseña son incorrectos"); 
        }

        return (new LoginResponse(aux.get().getId(), aux.get().getUsername(), aux.get().getRole()));
    }

    public void actualizarFotoPerfil(Integer userId, String foto) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        user.setFotoPerfil(foto);
        userRepository.save(user);
    }
}
