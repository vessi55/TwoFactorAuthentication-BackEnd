package twofactorauth.exception;

public class PasswordsDoNotMatchException extends RuntimeException {

    public PasswordsDoNotMatchException(String message) {
        super(message);
    }
}
