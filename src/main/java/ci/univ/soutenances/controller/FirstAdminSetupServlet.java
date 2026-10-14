package ci.univ.soutenances.controller;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;

import ci.univ.soutenances.service.AuthService;
import jakarta.ejb.EJB;
import jakarta.ejb.EJBException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/setup-admin")
public class FirstAdminSetupServlet extends HttpServlet {
    private static final String CSRF_SESSION_ATTRIBUTE = "firstAdminCsrfToken";

    @EJB
    private AuthService authService;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!isLocalRequest(request)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        if (redirectIfAdminExists(request, response)) {
            return;
        }
        HttpSession session = request.getSession(true);
        request.setAttribute("csrfToken", csrfToken(session));
        request.setAttribute("erreur", request.getParameter("erreur"));
        request.getRequestDispatcher("/WEB-INF/views/auth/setup-admin.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        if (!isLocalRequest(request)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        HttpSession session = request.getSession(false);
        if (session == null || !validCsrf(request, session)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Jeton de formulaire invalide.");
            return;
        }
        if (redirectIfAdminExists(request, response)) {
            return;
        }

        try {
            authService.createFirstAdministrator(
                    request.getParameter("login"),
                    request.getParameter("password"),
                    request.getParameter("confirmation"));
            session.removeAttribute(CSRF_SESSION_ATTRIBUTE);
            response.sendRedirect(request.getContextPath() + "/login?message=admin-created");
        } catch (IllegalArgumentException | IllegalStateException exception) {
            showError(request, response, session, exception.getMessage());
        } catch (EJBException exception) {
            getServletContext().log("Échec de la création du premier compte administrateur.", exception);
            showError(request, response, session, "La création du compte a échoué. Réessaie.");
        }
    }

    private boolean redirectIfAdminExists(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            if (authService.hasAdministrator()) {
                response.sendRedirect(request.getContextPath() + "/login");
                return true;
            }
            return false;
        } catch (EJBException exception) {
            getServletContext().log("Impossible de vérifier l’existence du compte administrateur.", exception);
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "La base de données est temporairement indisponible.");
            return true;
        }
    }

    private void showError(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, String message) throws ServletException, IOException {
        request.setAttribute("csrfToken", csrfToken(session));
        request.setAttribute("erreur", message);
        request.getRequestDispatcher("/WEB-INF/views/auth/setup-admin.jsp").forward(request, response);
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

    private static boolean isLocalRequest(HttpServletRequest request) {
        try {
            return InetAddress.getByName(request.getRemoteAddr()).isLoopbackAddress();
        } catch (UnknownHostException exception) {
            return false;
        }
    }
}
