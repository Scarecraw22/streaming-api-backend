package pl.agh.iet.db.converter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import java.util.Arrays;

@Configuration
public class CustomConverters {

    @Bean
    public MongoCustomConversions MongoCustomConversions() {
        return new MongoCustomConversions(
                Arrays.asList(
                        new ZonedDateTimeReadConverter(),
                        new ZonedDateTimeWriterConverter()
                )
        );
    }
}
