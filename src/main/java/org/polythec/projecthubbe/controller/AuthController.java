package org.polythec.projecthubbe.controller;

import jakarta.validation.Valid;
import org.polythec.projecthubbe.security.JwtUtil;
import org.polythec.projecthubbe.exception.*;
import org.polythec.projecthubbe.entity.User;
import org.polythec.projecthubbe.service.UserService;
import org.polythec.projecthubbe.service.impl.UserServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final UserServiceImpl userServiceImpl;


    public AuthController(AuthenticationManager authenticationManager,
                          UserService userService,
                          JwtUtil jwtUtil, UserServiceImpl userServiceImpl) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.userServiceImpl = userServiceImpl;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody User user) {
        try {
            User createdUser = userService.createUser(user);
            UserDetails userDetails = userService.loadUserByUsername(createdUser.getEmail());
            String token = jwtUtil.generateToken(userDetails);
            System.out.println(user.toString());
            return ResponseEntity.ok(new AuthResponse(token));

        } catch (EmailAlreadyExistsException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody AuthRequest authRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authRequest.getEmail(),
                        authRequest.getPassword()
                )
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtUtil.generateToken(userDetails);
        System.out.println("token : "+token);
        userService.updateLastLogin(userDetails.getUsername());
        return ResponseEntity.ok(new AuthResponse(token));
    }

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser() {
        User user = userServiceImpl.getCurrentlyAuthenticatedUser();
        return ResponseEntity.ok(user);
    }
    // DTO Classes
    private static class AuthRequest {
        private String email;
        private String password;
        // Getters and setters

        public String getEmail() {
            return email;
        }

        public String getPassword() {
            return password;
        }
    }

    private static class AuthResponse {
        private final String token;
        public AuthResponse(String token) { this.token = token; }
        public String getToken() { return token; }
    }
}