package org.polythec.projecthubbe.controller;
import jakarta.validation.Valid;
import org.polythec.projecthubbe.entity.Task;
import org.polythec.projecthubbe.repository.UserRepository;
import org.polythec.projecthubbe.security.JwtUtil;
import org.polythec.projecthubbe.exception.*;
import org.polythec.projecthubbe.entity.User;
import org.polythec.projecthubbe.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@RestController
@RequestMapping("/api/user")
public class UserController {
    private UserService userService;
    public UserController(UserService userService) {
        this.userService = userService;
    }
    @GetMapping("/search")
    public ResponseEntity<List<User>> searchUsers(@RequestParam String keyword) {
        List<User> users = userService.searchUsersByEmailOrName(keyword);
        return ResponseEntity.ok(users);
    }

}
