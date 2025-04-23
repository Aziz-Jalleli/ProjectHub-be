package org.polythec.projecthubbe.service.impl;

import org.polythec.projecthubbe.exception.EmailAlreadyExistsException;
import org.polythec.projecthubbe.exception.UserNotFoundException;
import org.polythec.projecthubbe.entity.User;
import org.polythec.projecthubbe.repository.UserRepository;
import org.polythec.projecthubbe.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User createUser(User user) throws EmailAlreadyExistsException {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new EmailAlreadyExistsException("Email " + user.getEmail() + " already exists");
        }
        user.setEmail(user.getEmail());

        /*user.setFirstName(user.getFirstName().trim());
        user.setLastName(user.getLastName().trim());*/

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getStatus() == null) {
            user.setStatus("active");
        }
        if (user.getTimezone() == null) {
            user.setTimezone("UTC");
        }
        if (user.getIsVerified() == null) {
            user.setIsVerified(false);
        }
        if (user.getFirstName() != null) {
            user.setFirstName(user.getFirstName().trim());
        }

        if (user.getLastName() != null) {
            user.setLastName(user.getLastName().trim());
        }

        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserById(String id) throws UserNotFoundException {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserByEmail(String email) throws UserNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
    }
    @Override
    public List<User> searchUsersByEmailOrName(String keyword) {
        return userRepository.findByEmailContainingIgnoreCaseOrNameContainingIgnoreCase(keyword, keyword);
    }


    @Override
    public User updateUser(String userId, User userDetails) throws UserNotFoundException {
        User user = getUserById(userId);

        if (userDetails.getFirstName() != null) {
            user.setFirstName(userDetails.getFirstName());
        }
        if (userDetails.getLastName() != null) {
            user.setLastName(userDetails.getLastName());
        }
        if (userDetails.getProfilePicture() != null) {
            user.setProfilePicture(userDetails.getProfilePicture());
        }
        if (userDetails.getTimezone() != null) {
            user.setTimezone(userDetails.getTimezone());
        }

        return userRepository.save(user);
    }

    @Override
    public void deleteUser(String userId) throws UserNotFoundException {
        User user = getUserById(userId);
        userRepository.delete(user);
    }

    @Override
    public void updateLastLogin(String email) throws UserNotFoundException {
        User user = getUserByEmail(email);
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
    }

    @Override
    public boolean isEmailTaken(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public void verifyUser(String userId) throws UserNotFoundException {
        User user = getUserById(userId);
        user.setIsVerified(true);
        userRepository.save(user);
    }

    @Override
    public void changeUserStatus(String userId, String status) throws UserNotFoundException, IllegalArgumentException {
        if (!List.of("active", "inactive", "banned").contains(status)) {
            throw new IllegalArgumentException("Invalid status value: " + status);
        }

        User user = getUserById(userId);
        user.setStatus(status);
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getUsersByStatus(String status) {
        return userRepository.findByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getVerifiedUsers(boolean isVerified) {
        return userRepository.findByIsVerified(isVerified);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    @Override
    public User getCurrentlyAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }
        String email = authentication.getName(); // Assuming email is the principal
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }
    @Override
    public void updateProfilePicture(String email, String publicId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        user.setCloudinaryPublicId(publicId);
        userRepository.save(user);
    }


}
