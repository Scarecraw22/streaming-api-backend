package pl.agh.iet.model;

import lombok.Builder;
import lombok.Value;
import org.springframework.web.multipart.MultipartFile;
import pl.agh.iet.validation.CreateStreamRequestValid;

@Value
@Builder
@CreateStreamRequestValid
public class CreateStreamRequest {

    String name;
    String description;
    MultipartFile content;
    MultipartFile thumbnail;
}
