package ci.univ.soutenances.controller;

import java.io.IOException;
import java.util.UUID;

import ci.univ.soutenances.config.AdminAuthorizationFilter;
import ci.univ.soutenances.model.RoleUtilisateur;
import ci.univ.soutenances.model.SessionUser;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Object value = session == null ? null
                : session.getAttribute(AdminAuthorizationFilter.SESSION_USER_ATTRIBUTE);
        if (!(value instanceof SessionUser utilisateur)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        if (utilisateur.role() == RoleUtilisateur.ADMIN) {
            response.sendRedirect(request.getContextPath() + "/admin/departements");
            return;
        }

        if (utilisateur.role() == RoleUtilisateur.ENCADREUR) {
            response.sendRedirect(request.getContextPath() + "/encadreur/dashboard");
            return;
        }

        request.setAttribute("utilisateur", utilisateur);
        String csrfToken = (String) session.getAttribute("logoutCsrfToken");
        if (csrfToken == null) {
            csrfToken = UUID.randomUUID().toString();
            session.setAttribute("logoutCsrfToken", csrfToken);
        }
        request.setAttribute("csrfToken", csrfToken);
        request.getRequestDispatcher("/WEB-INF/views/auth/dashboard-placeholder.jsp").forward(request, response);
    }
}
