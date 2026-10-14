package ci.univ.soutenances.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import ci.univ.soutenances.config.AdminAuthorizationFilter;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        String submittedToken = request.getParameter("csrfToken");
        if (session == null || !matches(session, AdminAuthorizationFilter.SESSION_CSRF_ATTRIBUTE, submittedToken)
                && !matches(session, "logoutCsrfToken", submittedToken)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Jeton de formulaire invalide.");
            return;
        }
        if (session != null) {
            session.invalidate();
        }
        response.sendRedirect(request.getContextPath() + "/login");
    }

    private static boolean matches(HttpSession session, String attribute, String submittedToken) {
        String expectedToken = (String) session.getAttribute(attribute);
        return expectedToken != null && submittedToken != null && MessageDigest.isEqual(
                expectedToken.getBytes(StandardCharsets.UTF_8),
                submittedToken.getBytes(StandardCharsets.UTF_8));
    }
}
