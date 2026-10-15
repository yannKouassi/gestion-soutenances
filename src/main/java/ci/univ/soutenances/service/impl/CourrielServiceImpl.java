package ci.univ.soutenances.service.impl;

import java.util.Properties;

import ci.univ.soutenances.service.CourrielService;
import jakarta.ejb.EJBException;
import jakarta.ejb.Stateless;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

@Stateless
public class CourrielServiceImpl implements CourrielService {
    @Override
    public void sendCredentials(String recipient, String displayName, String login, String password,
            boolean reset) {
        try {
            SmtpSettings smtp = SmtpSettings.fromEnvironment();
            Session session = smtp.createSession();

            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(smtp.from(), true));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient, true));
            message.setSubject(reset ? "Réinitialisation de votre mot de passe" : "Votre compte soutenances",
                    "UTF-8");
            message.setText("Bonjour " + displayName + ",\n\n"
                    + (reset ? "Votre mot de passe temporaire a été réinitialisé."
                            : "Votre compte de gestion des soutenances a été créé.")
                    + "\n\nIdentifiant : " + login + "\nMot de passe temporaire : " + password
                    + "\n\nConservez ces identifiants de manière confidentielle. Contactez l’administration "
                    + "si vous avez besoin d’un nouveau mot de passe.\n",
                    "UTF-8");
            Transport.send(message);
        } catch (MessagingException | IllegalArgumentException exception) {
            throw new EJBException("Échec de l’envoi du courriel. Vérifiez les paramètres SMTP du serveur.", exception);
        }
    }

    private record SmtpSettings(String host, int port, String username, String password,
            String from, boolean startTls, boolean ssl) {
        private static SmtpSettings fromEnvironment() {
            String host = setting("SMTP_HOST");
            String from = setting("SMTP_FROM");
            String username = setting("SMTP_USERNAME");
            String password = setting("SMTP_PASSWORD");
            boolean ssl = booleanSetting("SMTP_SSL", false);
            boolean startTls = booleanSetting("SMTP_STARTTLS", !ssl);
            int port = integerSetting("SMTP_PORT", ssl ? 465 : 587);

            if (host == null || host.isBlank() || host.chars().anyMatch(Character::isWhitespace)) {
                throw new IllegalStateException("La variable SMTP_HOST doit contenir le nom de l’hôte SMTP.");
            }
            if (from == null || from.isBlank()) {
                throw new IllegalStateException("La variable SMTP_FROM doit contenir l’adresse d’expédition.");
            }
            if (port < 1 || port > 65535) {
                throw new IllegalStateException("SMTP_PORT doit être compris entre 1 et 65535.");
            }
            if (ssl && startTls) {
                throw new IllegalStateException("SMTP_SSL et SMTP_STARTTLS ne peuvent pas être activés ensemble.");
            }
            if ((username == null) != (password == null)) {
                throw new IllegalStateException("SMTP_USERNAME et SMTP_PASSWORD doivent être définis ensemble.");
            }
            if (username != null && !startTls && !ssl) {
                throw new IllegalStateException("L’authentification SMTP doit utiliser STARTTLS ou TLS implicite.");
            }
            return new SmtpSettings(host, port, username, password, from, startTls, ssl);
        }

        private Session createSession() {
            Properties properties = new Properties();
            properties.setProperty("mail.smtp.host", host);
            properties.setProperty("mail.smtp.port", Integer.toString(port));
            properties.setProperty("mail.smtp.auth", Boolean.toString(username != null));
            properties.setProperty("mail.smtp.starttls.enable", Boolean.toString(startTls));
            properties.setProperty("mail.smtp.starttls.required", Boolean.toString(startTls));
            properties.setProperty("mail.smtp.ssl.enable", Boolean.toString(ssl));
            properties.setProperty("mail.smtp.connectiontimeout", "10000");
            properties.setProperty("mail.smtp.timeout", "10000");
            properties.setProperty("mail.smtp.writetimeout", "10000");
            properties.setProperty("mail.from", from);

            Authenticator authenticator = username == null ? null : new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            };
            return Session.getInstance(properties, authenticator);
        }

        private static String setting(String name) {
            String value = System.getenv(name);
            if (value == null || value.isBlank()) {
                value = System.getProperty(name);
            }
            return value == null || value.isBlank() ? null : value.trim();
        }

        private static boolean booleanSetting(String name, boolean defaultValue) {
            String value = setting(name);
            if (value == null) {
                return defaultValue;
            }
            if ("true".equalsIgnoreCase(value)) {
                return true;
            }
            if ("false".equalsIgnoreCase(value)) {
                return false;
            }
            throw new IllegalStateException(name + " doit valoir true ou false.");
        }

        private static int integerSetting(String name, int defaultValue) {
            String value = setting(name);
            if (value == null) {
                return defaultValue;
            }
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException exception) {
                throw new IllegalStateException(name + " doit être un nombre entier.", exception);
            }
        }
    }
}
