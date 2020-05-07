package twofactorauth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler({ElementAlreadyExistsException.class, PasswordsDoNotMatchException.class})
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    private ResponseEntity<ErrorMessage> handleBadRequest(Exception e) {
        return new ResponseEntity<>(new ErrorMessage(e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({ElementNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    private ResponseEntity<ErrorMessage> handleNotFound(Exception e) {
        return new ResponseEntity<>(new ErrorMessage(e.getMessage()), HttpStatus.NOT_FOUND);
    }
}