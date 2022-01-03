package pl.agh.iet.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
public class Video {

    private String name;
    private MultipartFile content;
}
