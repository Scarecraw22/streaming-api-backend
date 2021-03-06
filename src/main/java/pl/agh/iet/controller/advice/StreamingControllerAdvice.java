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
import pl.agh.iet.service.streaming.StreamNotExistException;
import pl.agh.iet.service.streaming.StreamingServiceException;
import pl.agh.iet.service.streaming.metadata.MetadataServiceException;
import pl.agh.iet.service.thumbnail.ForbiddenThumbnailExtensionException;
import pl.agh.iet.service.thumbnail.ThumbnailNotExistException;
import pl.agh.iet.utils.StringConsts;

import javax.validation.ValidationException;
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

    @ExceptionHandler(ThumbnailNotExistException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<?> handle(ThumbnailNotExistException e) {
        log.error("Exception: ", e);
        return withStatus(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(StreamNotExistException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<?> handle(StreamNotExistException e) {
        log.error("Exception: ", e);
        return withStatus(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<?> handle(ValidationException e) {
        log.error("Exception: ", e);
        return withStatus(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<?> handle(BindException e) {
        String message = e.getMessage();
        List<FieldError> errors = e.getBindingResult().getFieldErrors();
        if (errors.size() > 0) {
            message = errors.stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.joining(StringConsts.SEMICOLON));
        }
        log.info("BAD_REQUEST, message: {}", message);
        return withStatus(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(ForbiddenThumbnailExtensionException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<?> handle(ForbiddenThumbnailExtensionException e) {
        log.info("BAD_REQUEST, message: {}", e.getMessage());
        return withStatus(HttpStatus.BAD_REQUEST, e.getMessage());
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
