package org.polythec.projecthubbe.service;
import org.polythec.projecthubbe.entity.User;
import org.polythec.projecthubbe.exception.EmailAlreadyExistsException;
import org.polythec.projecthubbe.exception.UserNotFoundException;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

public interface UserService {
    User createUser(User user) throws EmailAlreadyExistsException;
    User getUserById(String id) throws UserNotFoundException;
    User getUserByEmail(String email) throws UserNotFoundException;
    List<User> searchUsersByEmailOrName(String keyword);
    User updateUser(String userId, User userDetails) throws UserNotFoundException;
    void deleteUser(String userId) throws UserNotFoundException;
    void updateLastLogin(String email) throws UserNotFoundException;
    boolean isEmailTaken(String email);
    void verifyUser(String userId) throws UserNotFoundException;
    void changeUserStatus(String userId, String status) throws UserNotFoundException, IllegalArgumentException;
    List<User> getUsersByStatus(String status);
    List<User> getVerifiedUsers(boolean isVerified);
    UserDetails loadUserByUsername(String email);
    User getCurrentlyAuthenticatedUser();

    void updateProfilePicture(String email, String imageUrl);
}