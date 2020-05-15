package twofactorauth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler({Exception.class})
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    private ResponseEntity<ErrorMessage> handleInternalServerError(Exception e) {
        return new ResponseEntity<>(new ErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler({WrongCredentialsException.class})
    @ResponseStatus(value = HttpStatus.UNAUTHORIZED)
    public ResponseEntity<ErrorMessage> handleUnauthorized(Exception e) {
        return new ResponseEntity<>(new ErrorMessage(HttpStatus.UNAUTHORIZED, e.getMessage()), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler({ElementAlreadyExistsException.class, PasswordsDoNotMatchException.class,
            NotAllowedException.class, InvalidVerificationCodeException.class})
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    private ResponseEntity<ErrorMessage> handleBadRequest(Exception e) {
        return new ResponseEntity<>(new ErrorMessage(HttpStatus.BAD_REQUEST, e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({ElementNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    private ResponseEntity<ErrorMessage> handleNotFound(Exception e) {
        return new ResponseEntity<>(new ErrorMessage(HttpStatus.NOT_FOUND, e.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({NotAllowedException.class})
    @ResponseStatus(value = HttpStatus.METHOD_NOT_ALLOWED)
    private ResponseEntity<ErrorMessage> handleNotAllowed(Exception e) {
        return new ResponseEntity<>(new ErrorMessage(HttpStatus.METHOD_NOT_ALLOWED, e.getMessage()), HttpStatus.METHOD_NOT_ALLOWED);
    }
}