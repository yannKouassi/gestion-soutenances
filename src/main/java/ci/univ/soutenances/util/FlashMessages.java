package ci.univ.soutenances.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public final class FlashMessages {
    private static final String SESSION_KEY = FlashMessages.class.getName();
    private static final String MESSAGE = "message";
    private static final String ERREUR = "erreur";

    private FlashMessages() {
    }

    public static void succes(HttpServletRequest request, String message) {
        ajouter(request, MESSAGE, message);
    }

    public static void erreur(HttpServletRequest request, String message) {
        ajouter(request, ERREUR, message);
    }

    public static void exposer(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return;
        }
        Object value = session.getAttribute(SESSION_KEY);
        if (!(value instanceof List<?> messages) || messages.isEmpty()) {
            return;
        }
        session.removeAttribute(SESSION_KEY);
        StringBuilder succes = new StringBuilder();
        StringBuilder erreurs = new StringBuilder();
        for (Object element : messages) {
            if (!(element instanceof FlashMessage message)) {
                continue;
            }
            StringBuilder cible = MESSAGE.equals(message.type) ? succes : erreurs;
            if (cible.length() > 0) {
                cible.append(' ');
            }
            cible.append(message.texte);
        }
        if (succes.length() > 0) {
            request.setAttribute(MESSAGE, succes.toString());
        }
        if (erreurs.length() > 0) {
            request.setAttribute(ERREUR, erreurs.toString());
        }
    }

    private static void ajouter(HttpServletRequest request, String type, String message) {
        if (message == null || message.isBlank()) {
            return;
        }
        HttpSession session = request.getSession(false);
        if (session == null) {
            return;
        }
        List<FlashMessage> messages = new ArrayList<>();
        Object existant = session.getAttribute(SESSION_KEY);
        if (existant instanceof List<?> liste) {
            for (Object element : liste) {
                if (element instanceof FlashMessage flash) {
                    messages.add(flash);
                }
            }
        }
        messages.add(new FlashMessage(type, message));
        session.setAttribute(SESSION_KEY, messages);
    }

    private static final class FlashMessage implements Serializable {
        private static final long serialVersionUID = 1L;

        private final String type;
        private final String texte;

        private FlashMessage(String type, String texte) {
            this.type = type;
            this.texte = texte;
        }
    }
}
