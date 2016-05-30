package com.petfinder.service;

import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.petfinder.dao.UserRepository;
import com.petfinder.domain.User;
import com.petfinder.exception.EmailExistsException;
import com.petfinder.exception.InvalidEmailException;
import com.petfinder.exception.LoginExistsException;
import com.petfinder.exception.PasswordsDoesNotMatchException;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public void register(String login, String password, String repeatPassword,
            String email)
            throws LoginExistsException, EmailExistsException,
            InvalidEmailException, PasswordsDoesNotMatchException {
        if (isEmailAddressValid(email)) {
            if (verifyLogin(login) && verifyEmail(email)) {
                if (password.equals(repeatPassword)) {
                    String passwordHash = hashPassword(password);
                    User user = new User(login, email, passwordHash);
                    userRepository.save(user);
                } else {
                    throw new PasswordsDoesNotMatchException(
                            "Passwords does not match."
                    );
                }
            }
        }
    }

    public boolean checkIfUserIsLogged() {
        if (SecurityContextHolder.getContext().getAuthentication() instanceof AnonymousAuthenticationToken) {
            return false;
        }
        return true;
    }

    public String getLoggedUserName() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof String) {
            return null;
        } else {
            org.springframework.security.core.userdetails.User loggedUser = (org.springframework.security.core.userdetails.User) principal;
            return loggedUser.getUsername();
        }
    }

    private String hashPassword(String password) {
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.encode(password);
    }

    private boolean verifyLogin(String login) throws LoginExistsException {
        Boolean exists = userRepository.existsByLogin(login);
        if (!exists) {
            return true;
        }
        throw new LoginExistsException(
                String.format("User '%s' already exists.", login)
        );
    }

    private boolean verifyEmail(String email) throws EmailExistsException {
        Boolean exists = userRepository.existsByEmail(email);
        if (!exists) {
            return true;
        }
        throw new EmailExistsException(
                String.format("Email '%s' is already used.", email)
        );
    }

    private boolean isEmailAddressValid(String email) throws InvalidEmailException {
        boolean valid = EmailValidator.getInstance().isValid(email);
        if (valid) {
            return true;
        } else {
            throw new InvalidEmailException(
                    "Given email address is not valid."
            );
        }
    }
}
