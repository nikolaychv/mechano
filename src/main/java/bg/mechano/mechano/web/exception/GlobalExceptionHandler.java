package bg.mechano.mechano.web.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(
            NotFoundException ex
    ) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(
                        body(
                                HttpStatus.NOT_FOUND,
                                ex.getMessage()
                        )
                );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException ex
    ) {
        String details = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error ->
                        error.getField()
                                + ": "
                                + error.getDefaultMessage()
                )
                .collect(Collectors.joining("; "));

        String message = details.isBlank()
                ? "Validation failed"
                : "Validation failed: " + details;

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(
                        body(
                                HttpStatus.BAD_REQUEST,
                                message
                        )
                );
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatus(
            ResponseStatusException ex
    ) {
        HttpStatus status =
                HttpStatus.valueOf(
                        ex.getStatusCode().value()
                );

        String message = ex.getReason() != null
                ? ex.getReason()
                : status.getReasonPhrase();

        return ResponseEntity
                .status(status)
                .body(
                        body(
                                status,
                                message
                        )
                );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(
            AccessDeniedException ex
    ) {
        String message = ex.getMessage() != null
                ? ex.getMessage()
                : "Access denied";

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(
                        body(
                                HttpStatus.FORBIDDEN,
                                message
                        )
                );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrity(
            DataIntegrityViolationException ex
    ) {
        String root = rootCauseMessage(ex);

        String message = "Data integrity violation";

        if (root != null && !root.isBlank()) {
            message = message + ": " + root;
        }

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(
                        body(
                                HttpStatus.CONFLICT,
                                message
                        )
                );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(
            Exception ex
    ) {
        String message =
                ex.getClass().getSimpleName()
                        + ": "
                        + (
                        ex.getMessage() != null
                                ? ex.getMessage()
                                : ""
                );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                        body(
                                HttpStatus.INTERNAL_SERVER_ERROR,
                                message
                        )
                );
    }

    private Map<String, Object> body(
            HttpStatus status,
            String message
    ) {
        Map<String, Object> response = new HashMap<>();

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern(
                        "yyyy-MM-dd HH:mm:ss XXX"
                );

        String timestamp =
                ZonedDateTime.now().format(formatter);

        response.put("timestamp", timestamp);
        response.put("status", status.value());
        response.put("error", status.getReasonPhrase());
        response.put("message", message);

        return response;
    }

    private String rootCauseMessage(Throwable ex) {
        Throwable throwable = ex;

        while (throwable.getCause() != null) {
            throwable = throwable.getCause();
        }

        return throwable.getMessage();
    }
}