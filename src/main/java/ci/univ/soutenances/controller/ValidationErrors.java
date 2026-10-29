package ci.univ.soutenances.controller;

final class ValidationErrors {
    private ValidationErrors() {
    }

    static String find(Throwable exception) {
        Throwable cause = exception;
        while (cause != null) {
            if (cause instanceof IllegalArgumentException && cause.getMessage() != null) {
                return cause.getMessage();
            }
            if (cause.getCause() == cause) {
                break;
            }
            cause = cause.getCause();
        }
        return null;
    }
}
