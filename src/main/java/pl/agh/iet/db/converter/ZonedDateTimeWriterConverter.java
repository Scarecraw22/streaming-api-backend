package pl.agh.iet.db.converter;

import lombok.NonNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

import java.time.ZonedDateTime;
import java.util.Date;

@WritingConverter
public class ZonedDateTimeWriterConverter implements Converter<ZonedDateTime, Date> {

    @Override
    public Date convert(@NonNull ZonedDateTime source) {
        return Date.from(source.toInstant());
    }
}
