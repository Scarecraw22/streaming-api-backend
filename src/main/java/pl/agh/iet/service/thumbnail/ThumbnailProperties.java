package pl.agh.iet.service.thumbnail;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Data
@NoArgsConstructor
@Component
@ConfigurationProperties(prefix = "pl.agh.iet.thumbnail")
public class ThumbnailProperties {

    private String path;
    private String url;
}
