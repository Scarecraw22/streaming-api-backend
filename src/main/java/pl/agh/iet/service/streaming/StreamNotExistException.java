package pl.agh.iet.service.streaming;

public class StreamNotExistException extends RuntimeException {

    public StreamNotExistException(String message) {
        super(message);
    }
}
