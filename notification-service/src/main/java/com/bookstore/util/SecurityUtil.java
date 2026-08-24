package com.bookstore.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtil {

    /**
     * Retrieves the username or subject of the currently authenticated user.
     */
    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            return authentication.getName();
        }
        return null;
    }

    /**
     * Retrieves the database member ID attached during JWT authentication.
     * Ensure your JwtAuthFilter sets the member ID into authentication.setDetails(...) or principal.
     */
    public Long getCurrentMemberId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            // 1. Check if the member ID was stored in authentication details (recommended)
            if (authentication.getDetails() instanceof Long) {
                return (Long) authentication.getDetails();
            }
            if (authentication.getDetails() instanceof Integer) {
                return ((Integer) authentication.getDetails()).longValue();
            }

            // 2. Fallback: If your principal object happens to be a custom wrapper containing the ID,
            // you can cast it here (e.g., if (authentication.getPrincipal() instanceof CustomUserDetails))
        }

        // Return null instead of a silent hardcoded fallback so controllers can handle it properly
        return null;
    }
}