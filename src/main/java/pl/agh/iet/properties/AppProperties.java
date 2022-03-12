package pl.agh.iet.properties;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Data
@NoArgsConstructor
@Component
@ConfigurationProperties(prefix = "pl.agh.iet")
public class AppProperties {

    private String serverUrl;
}
