package pl.agh.iet.controller.advice;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import pl.agh.iet.utils.StringConsts;
import pl.agh.iet.service.streaming.StreamingServiceException;
import pl.agh.iet.service.streaming.metadata.MetadataServiceException;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class StreamingControllerAdvice {

    private final Clock clock;

    @ExceptionHandler(StreamingServiceException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<?> handle(StreamingServiceException e) {
        log.error("Exception: ", e);
        return withStatus(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }

    @ExceptionHandler(MetadataServiceException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<?> handle(MetadataServiceException e) {
        log.error("Exception: ", e);
        return withStatus(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<?> handle(BindException e) {
        log.error("Exception: ", e);
        String message = e.getMessage();
        List<FieldError> errors = e.getBindingResult().getFieldErrors();
        if (errors.size() > 0) {
            message = errors.stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.joining(StringConsts.SEMICOLON));
        }
        return withStatus(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<?> handle(Exception e) {
        log.error("Exception: ", e);
        return withStatus(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }

    private ResponseEntity<Error> withStatus(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Error.of(message, ZonedDateTime.now(clock)));
    }

    @Value(staticConstructor = "of")
    private static class Error {
        String message;
        ZonedDateTime timestamp;

    }
}
