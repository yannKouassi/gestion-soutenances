package ci.univ.soutenances.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;

import ci.univ.soutenances.model.RoleUtilisateur;
import ci.univ.soutenances.model.SessionUser;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebFilter("/etudiant/*")
public class EtudiantAuthorizationFilter implements Filter {
    public static final String SESSION_USER_ATTRIBUTE = "utilisateurConnecte";
    public static final String SESSION_CSRF_ATTRIBUTE = "etudiantCsrfToken";
    private static final String REQUEST_CSRF_ATTRIBUTE = "csrfToken";

    @Override
    public void doFilter(jakarta.servlet.ServletRequest request, jakarta.servlet.ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = httpRequest.getSession(false);

        if (session == null) {
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Connexion requise.");
            return;
        }

        Object sessionUser = session.getAttribute(SESSION_USER_ATTRIBUTE);
        if (!(sessionUser instanceof SessionUser utilisateur)) {
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Connexion requise.");
            return;
        }

        if (utilisateur.role() != RoleUtilisateur.ETUDIANT) {
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Accès réservé aux étudiants.");
            return;
        }

        String csrfToken = (String) session.getAttribute(SESSION_CSRF_ATTRIBUTE);
        if (csrfToken == null) {
            csrfToken = UUID.randomUUID().toString();
            session.setAttribute(SESSION_CSRF_ATTRIBUTE, csrfToken);
        }
        if ("POST".equalsIgnoreCase(httpRequest.getMethod())) {
            String submittedToken = httpRequest.getParameter(REQUEST_CSRF_ATTRIBUTE);
            if (submittedToken == null || !MessageDigest.isEqual(
                    csrfToken.getBytes(StandardCharsets.UTF_8),
                    submittedToken.getBytes(StandardCharsets.UTF_8))) {
                httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Jeton de formulaire invalide.");
                return;
            }
        }
        httpRequest.setAttribute(REQUEST_CSRF_ATTRIBUTE, csrfToken);
        chain.doFilter(request, response);
    }
}
