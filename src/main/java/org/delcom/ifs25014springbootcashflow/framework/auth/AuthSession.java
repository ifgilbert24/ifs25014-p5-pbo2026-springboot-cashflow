package org.delcom.ifs25014springbootcashflow.framework.auth;

import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.VaadinSession;
import jakarta.servlet.http.Cookie;
import org.delcom.ifs25014springbootcashflow.domain.entity.User;
import org.delcom.ifs25014springbootcashflow.domain.repository.IUserRepository;

import java.util.Optional;

public final class AuthSession {

    public static final String USER_KEY = "auth.user";
    public static final String LOGGED_OUT_KEY = "auth.loggedOut";
    public static final String USER_ID_COOKIE = "cashflow_uid";
    private static final int COOKIE_MAX_AGE_SECONDS = 8 * 60 * 60;

    private AuthSession() {
    }

    public static void login(User user) {
        VaadinSession session = VaadinSession.getCurrent();
        if (session != null) {
            session.setAttribute(USER_KEY, user);
            session.setAttribute(LOGGED_OUT_KEY, null);
        }
        writeUserIdCookie(String.valueOf(user.getId()), COOKIE_MAX_AGE_SECONDS);
    }

    public static void logout() {
        VaadinSession session = VaadinSession.getCurrent();
        if (session != null) {
            session.setAttribute(USER_KEY, null);
            session.setAttribute(LOGGED_OUT_KEY, Boolean.TRUE);
        }
        writeUserIdCookie("", 0);
    }

    public static Optional<User> getUser() {
        VaadinSession session = VaadinSession.getCurrent();
        if (session == null) {
            return Optional.empty();
        }
        return Optional.ofNullable((User) session.getAttribute(USER_KEY));
    }

    public static Optional<User> resolveUser(IUserRepository userRepository) {
        VaadinSession session = VaadinSession.getCurrent();
        if (session != null && Boolean.TRUE.equals(session.getAttribute(LOGGED_OUT_KEY))) {
            return Optional.empty();
        }
        Optional<User> current = getUser();
        if (current.isPresent()) {
            return current;
        }
        Optional<Integer> cookieUserId = readUserIdCookie();
        if (cookieUserId.isEmpty()) {
            return Optional.empty();
        }
        Optional<User> restored = userRepository.findById(cookieUserId.get());
        restored.ifPresent(user -> {
            if (session != null) {
                session.setAttribute(USER_KEY, user);
            }
        });
        return restored;
    }

    private static void writeUserIdCookie(String value, int maxAgeSeconds) {
        VaadinResponse response = VaadinResponse.getCurrent();
        if (response == null) {
            return;
        }
        Cookie cookie = new Cookie(USER_ID_COOKIE, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(maxAgeSeconds);
        response.addCookie(cookie);
    }

    private static Optional<Integer> readUserIdCookie() {
        VaadinRequest request = VaadinRequest.getCurrent();
        if (request == null || request.getCookies() == null) {
            return Optional.empty();
        }
        for (Cookie cookie : request.getCookies()) {
            if (USER_ID_COOKIE.equals(cookie.getName())) {
                try {
                    return Optional.of(Integer.parseInt(cookie.getValue()));
                } catch (NumberFormatException ignored) {
                    return Optional.empty();
                }
            }
        }
        return Optional.empty();
    }
}

