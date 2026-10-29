package ci.univ.soutenances.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import ci.univ.soutenances.config.AdminAuthorizationFilter;
import ci.univ.soutenances.config.EncadreurAuthorizationFilter;
import ci.univ.soutenances.config.EtudiantAuthorizationFilter;
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

        // Session expirée : pas d'erreur CSRF, on redirige simplement vers login
        if (session == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String submittedToken = request.getParameter("csrfToken");

        // Accepter le token de n'importe quel rôle (admin, encadreur, étudiant)
        boolean validToken = matches(session, AdminAuthorizationFilter.SESSION_CSRF_ATTRIBUTE, submittedToken)
                || matches(session, EncadreurAuthorizationFilter.SESSION_CSRF_ATTRIBUTE, submittedToken)
                || matches(session, EtudiantAuthorizationFilter.SESSION_CSRF_ATTRIBUTE, submittedToken);

        if (!validToken) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Jeton de formulaire invalide.");
            return;
        }

        session.invalidate();
        response.sendRedirect(request.getContextPath() + "/login");
    }

    private static boolean matches(HttpSession session, String attribute, String submittedToken) {
        String expectedToken = (String) session.getAttribute(attribute);
        return expectedToken != null && submittedToken != null && MessageDigest.isEqual(
                expectedToken.getBytes(StandardCharsets.UTF_8),
                submittedToken.getBytes(StandardCharsets.UTF_8));
    }
}
