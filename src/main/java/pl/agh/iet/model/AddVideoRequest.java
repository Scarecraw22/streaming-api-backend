package pl.agh.iet.model;

import lombok.Builder;
import lombok.Value;
import org.springframework.web.multipart.MultipartFile;
import pl.agh.iet.validation.CreateStreamRequestValid;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Value
@Builder
@CreateStreamRequestValid
public class AddVideoRequest {

    @NotBlank(message = "name cannot be blank")
    String name;
    @NotBlank(message = "title cannot be blank")
    String title;
    @NotBlank(message = "description cannot be blank")
    String description;
    @NotNull(message = "content cannot be null")
    MultipartFile content;
    @NotNull(message = "thumbnail cannot be null")
    MultipartFile thumbnail;
}
