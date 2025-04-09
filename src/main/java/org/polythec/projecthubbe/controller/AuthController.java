package org.polythec.projecthubbe.controller;

import org.polythec.projecthubbe.security.JwtUtil;
import org.polythec.projecthubbe.exception.*;
import org.polythec.projecthubbe.entity.User;
import org.polythec.projecthubbe.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    public AuthController(AuthenticationManager authenticationManager,
                          UserService userService,
                          JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
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
    public User getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        User currentUser = userService.getUserByEmail(email);
        return currentUser;
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