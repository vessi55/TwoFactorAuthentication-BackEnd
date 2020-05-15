package twofactorauth.exception;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ConstraintViolationExceptionHandler {

    private static final String NO_ERROR_MESSAGES = "No error messages";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorMessage> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {

        List<FieldError> errors = e.getBindingResult().getFieldErrors();

        Optional<String> errorMsg = errors.stream()
                .filter(error -> error.getDefaultMessage() != null)
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .sorted()
                .findFirst();

        return new ResponseEntity<>(new ErrorMessage(HttpStatus.BAD_REQUEST, errorMsg.orElse(NO_ERROR_MESSAGES)), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ErrorMessage> handleConstraintViolation(ConstraintViolationException e) {

        Set<ConstraintViolation<?>> constraintViolations = e.getConstraintViolations();

        Optional<String> errorMsg = constraintViolations.stream()
                .map(ConstraintViolation::getMessage)
                .max(Comparator.naturalOrder());

        return new ResponseEntity<>(new ErrorMessage(HttpStatus.BAD_REQUEST, errorMsg.orElse(NO_ERROR_MESSAGES)), HttpStatus.BAD_REQUEST);
    }
}
