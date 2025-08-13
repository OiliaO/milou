package com.milou.service;

import com.milou.framework.SingletonSessionFactory;
import com.milou.model.User;
import org.hibernate.Session;
import java.util.Optional;

public class UserService {

    public Optional<User> signUp(String name, String email, String password) {
        if (password.length() < 8) {
            System.err.println("Error: Password must be at least 8 characters long.");
            return Optional.empty();
        }

        if (findUserByEmail(email).isPresent()) {
            System.err.println("Error: A user with this email already exists.");
            return Optional.empty();
        }

        User newUser = new User(name, email, password);

        SingletonSessionFactory.get().inTransaction(session -> {
            session.persist(newUser);
        });

        return Optional.of(newUser);
    }

    public Optional<User> login(String email, String password) {
        Optional<User> userOptional = findUserByEmail(email);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (user.getPassword().equals(password)) {
                return Optional.of(user);
            }
        }

        return Optional.empty();
    }

    public Optional<User> findUserByEmail(String email) {
        User user = SingletonSessionFactory.get().fromTransaction(session -> {
            return session.createNativeQuery("SELECT * FROM users WHERE email = :email", User.class)
                    .setParameter("email", email)
                    .getSingleResultOrNull();
        });
        return Optional.ofNullable(user);
    }
}