package ci.univ.soutenances.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;

import ci.univ.soutenances.config.AdminAuthorizationFilter;
import ci.univ.soutenances.model.SessionUser;
import ci.univ.soutenances.model.Utilisateur;
import ci.univ.soutenances.service.AuthService;
import jakarta.ejb.EJB;
import jakarta.ejb.EJBException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private static final String CSRF_SESSION_ATTRIBUTE = "authCsrfToken";

    @EJB
    private AuthService authService;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(true);
        if (session.getAttribute(AdminAuthorizationFilter.SESSION_USER_ATTRIBUTE) instanceof SessionUser) {
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }
        request.setAttribute("csrfToken", csrfToken(session));
        request.setAttribute("message", request.getParameter("message"));
        request.setAttribute("erreur", request.getParameter("erreur"));
        try {
            request.setAttribute("firstAdminSetupRequired", !authService.hasAdministrator());
        } catch (EJBException exception) {
            getServletContext().log("Impossible de vérifier la disponibilité du premier compte administrateur.", exception);
            request.setAttribute("erreur", "Le service de connexion est temporairement indisponible.");
        }
        request.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession(false);
        if (session == null || !validCsrf(request, session)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Jeton de formulaire invalide.");
            return;
        }

        String login = request.getParameter("login");
        String password = request.getParameter("password");
        try {
            Utilisateur utilisateur = authService.authenticate(login, password).orElse(null);
            if (utilisateur == null) {
                request.setAttribute("csrfToken", csrfToken(session));
                request.setAttribute("erreur", "Login ou mot de passe incorrect.");
                request.setAttribute("firstAdminSetupRequired", !authService.hasAdministrator());
                request.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(request, response);
                return;
            }

            request.changeSessionId();
            session.removeAttribute(CSRF_SESSION_ATTRIBUTE);
            session.setAttribute(AdminAuthorizationFilter.SESSION_USER_ATTRIBUTE, SessionUser.from(utilisateur));
            session.setMaxInactiveInterval(30 * 60);
            response.sendRedirect(request.getContextPath() + "/dashboard");
        } catch (EJBException exception) {
            getServletContext().log("Échec du traitement de connexion.", exception);
            request.setAttribute("csrfToken", csrfToken(session));
            request.setAttribute("erreur", "Le service de connexion est temporairement indisponible.");
            request.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(request, response);
        }
    }

    private static String csrfToken(HttpSession session) {
        String token = (String) session.getAttribute(CSRF_SESSION_ATTRIBUTE);
        if (token == null) {
            token = UUID.randomUUID().toString();
            session.setAttribute(CSRF_SESSION_ATTRIBUTE, token);
        }
        return token;
    }

    private static boolean validCsrf(HttpServletRequest request, HttpSession session) {
        String expected = (String) session.getAttribute(CSRF_SESSION_ATTRIBUTE);
        String actual = request.getParameter("csrfToken");
        return expected != null && actual != null && MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                actual.getBytes(StandardCharsets.UTF_8));
    }
}
