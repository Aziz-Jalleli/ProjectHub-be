package org.polythec.projecthubbe.controller;

import jakarta.validation.Valid;
import org.polythec.projecthubbe.dto.UserDTO;
import org.polythec.projecthubbe.entity.User;
import org.polythec.projecthubbe.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping(value = "/search", produces = "application/json")
    public ResponseEntity<List<UserDTO>> searchUsers(@RequestParam String keyword) {
        List<User> users = userService.searchUsersByEmailOrName(keyword);
        List<UserDTO> userDTOs = users.stream().map(user -> {
            UserDTO dto = new UserDTO();
            dto.setId(user.getId());
            dto.setEmail(user.getEmail());
            dto.setFirstName(user.getFirstName());
            dto.setLastName(user.getLastName());
            dto.setProfilePicture(user.getProfilePicture());
            return dto;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(userDTOs);
    }
}