package pl.agh.iet.controller.advice;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import pl.agh.iet.video.VideoServiceException;
import pl.agh.iet.video.metadata.MetadataServiceException;

import java.time.Clock;
import java.time.ZonedDateTime;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class StreamingControllerAdvice {

    private final Clock clock;

    @ExceptionHandler(VideoServiceException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<?> handle(VideoServiceException e) {
        log.error("Exception: ", e);
        return withStatus(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }

    @ExceptionHandler(MetadataServiceException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<?> handle(MetadataServiceException e) {
        log.error("Exception: ", e);
        return withStatus(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
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
