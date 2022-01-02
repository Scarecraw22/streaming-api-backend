package pl.agh.iet.ffmpeg;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@NoArgsConstructor
@Component
@ConfigurationProperties(prefix = "pl.agh.iet.ffmpeg")
public class FfmpegProperties {

    private String path;
    private String probe;
    private String outputDir;
    private String serverUrl;
}
