package com.smartshop.service;

import com.smartshop.entity.User;
import com.smartshop.entity.UserRole;
import com.smartshop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import jakarta.servlet.http.HttpSession;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;

    public boolean login(String username, String password, HttpSession session) {
        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isPresent() && userOpt.get().getPassword().equals(password)) {
            session.setAttribute("user", userOpt.get());
            session.setAttribute("role", userOpt.get().getRole());
            session.setAttribute("userId", userOpt.get().getId());
            return true;
        }
        return false;
    }

    public void logout(HttpSession session) {
        session.invalidate();
    }

    public boolean isAdmin(HttpSession session) {
        return UserRole.ADMIN.equals(session.getAttribute("role"));
    }

    public boolean isClient(HttpSession session) {
        return UserRole.CLIENT.equals(session.getAttribute("role"));
    }

    public Long getCurrentUserId(HttpSession session) {
        return (Long) session.getAttribute("userId");
    }
}
