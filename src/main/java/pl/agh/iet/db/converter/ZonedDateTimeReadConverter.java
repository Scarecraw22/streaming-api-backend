package pl.agh.iet.db.converter;

import lombok.NonNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

@ReadingConverter
public class ZonedDateTimeReadConverter implements Converter<Date, ZonedDateTime> {

    @Override
    public ZonedDateTime convert(@NonNull Date source) {
        return source.toInstant().atZone(ZoneId.of("Europe/Warsaw"));
    }
}
