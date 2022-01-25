package pl.agh.iet.video.metadata;

public class MetadataServiceException extends RuntimeException {

    public MetadataServiceException(String message) {
        super(message);
    }

    public MetadataServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
