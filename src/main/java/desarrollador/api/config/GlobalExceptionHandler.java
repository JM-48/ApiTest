package desarrollador.api.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;
import java.util.NoSuchElementException;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleBadRequest(IllegalArgumentException ex, org.springframework.web.context.request.WebRequest req) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body("bad_request", ex.getMessage(), req));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Object> handleNotFound(NoSuchElementException ex, org.springframework.web.context.request.WebRequest req) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body("not_found", ex.getMessage(), req));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleUnprocessable(MethodArgumentNotValidException ex, org.springframework.web.context.request.WebRequest req) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + " " + e.getDefaultMessage())
                .findFirst().orElse("datos inválidos");
        return ResponseEntity.status(422).body(body("validation_error", msg, req));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleServer(Exception ex, org.springframework.web.context.request.WebRequest req) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body("server_error", ex.getMessage(), req));
    }

    private java.util.Map<String, Object> body(String error, String message, org.springframework.web.context.request.WebRequest req) {
        String path = req.getDescription(false).replace("uri=", "");
        return java.util.Map.of(
                "error", error,
                "message", message,
                "path", path,
                "timestamp", Instant.now().toString()
        );
    }
}
