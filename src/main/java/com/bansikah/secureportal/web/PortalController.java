package com.bansikah.secureportal.web;

import com.bansikah.secureportal.user.domain.Permission;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PortalController {

    @GetMapping("/")
    String home() {
        return "home";
    }

    @GetMapping("/login")
    String login() {
        return "auth/login";
    }

    @GetMapping("/access-denied")
    String accessDenied() {
        return "auth/access-denied";
    }

    @GetMapping("/dashboard")
    String dashboard(Authentication authentication, Model model) {
        model.addAttribute("username", authentication.getName());
        model.addAttribute("authorityCount", authentication.getAuthorities().size());
        return "dashboard/dashboard";
    }

    @GetMapping("/profile")
    String profile(Authentication authentication, Model model) {
        model.addAttribute("username", authentication.getName());
        return "profile/profile";
    }

    @GetMapping("/security")
    String security(Authentication authentication, Model model) {
        model.addAttribute("username", authentication.getName());
        model.addAttribute("authenticationType", authentication.getClass().getSimpleName());
        model.addAttribute("roles", authentication.getAuthorities().stream()
            .map(Object::toString).filter(authority -> authority.startsWith("ROLE_"))
            .map(authority -> authority.substring("ROLE_".length())).sorted().toList());
        model.addAttribute("permissions", authentication.getAuthorities().stream()
                .map(Object::toString).filter(Permission::isDefined)
            .sorted().toList());
        return "security/security-dashboard";
    }

    @GetMapping("/security-lab")
    String securityLab() {
        return "security/security-lab";
    }

    @GetMapping("/admin")
    String admin() {
        return "admin/admin";
    }
}