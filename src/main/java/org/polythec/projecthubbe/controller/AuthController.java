package org.polythec.projecthubbe.controller;

import jakarta.validation.Valid;
import org.polythec.projecthubbe.dto.UserDTO;
import org.polythec.projecthubbe.repository.CloudinaryUploadResult;
import org.polythec.projecthubbe.security.JwtUtil;
import org.polythec.projecthubbe.exception.*;
import org.polythec.projecthubbe.entity.User;
import org.polythec.projecthubbe.service.CloudinaryService;
import org.polythec.projecthubbe.service.UserService;
import org.polythec.projecthubbe.service.impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.Map;

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

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser() {

        User user = userServiceImpl.getCurrentlyAuthenticatedUser();

        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId().toString()); // Assuming user.getId() is a Long
        userDTO.setEmail(user.getEmail());
        userDTO.setFirstName(user.getFirstName());
        userDTO.setLastName(user.getLastName());
        userDTO.setProfilePicture(user.getProfilePicture());
        userDTO.setCloudinaryPublicId(user.getCloudinaryPublicId()); // Add this line

        return ResponseEntity.ok(userDTO);
    }
    @Autowired
    private CloudinaryService cloudinaryService;

    @CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true", methods = {RequestMethod.PUT})
    @PutMapping("/update-profile-image")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateProfileImage(
            @RequestParam("image") MultipartFile image,
            Principal principal
    ) {
        try {
            CloudinaryUploadResult uploadResult = cloudinaryService.uploadFile(image);
            String publicId = uploadResult.getPublicId();
            String secureUrl = uploadResult.getSecureUrl();

            // Update BOTH fields in the database
            userService.updateProfilePictureAndPublicId(
                    principal.getName(),
                    secureUrl,
                    publicId
            );

            return ResponseEntity.ok().body(Map.of(
                    "message", "Profile picture updated",
                    "url", secureUrl,
                    "publicId", publicId
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Image upload failed: " + e.getMessage());
        }
    }
    @PreAuthorize("isAuthenticated()") // Add this annotation
    @PutMapping("/update")
    public ResponseEntity<?> updateUser(@RequestBody User updatedUser, Principal principal) {
        try {
            // Get currently authenticated user's ID using their email from the JWT
            User currentUser = userServiceImpl.getCurrentlyAuthenticatedUser();
            User updated = userService.updateUser(currentUser.getId(), updatedUser);

            return ResponseEntity.ok(updated);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
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