package pl.agh.iet.db;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document
public class MetadataEntity {

    @Id
    private String id;
    private String streamName;
    private String thumbnailFilename;
    private String description;
    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal duration;
    private long initialSize;
    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal fps;
    private ZonedDateTime createdAt;
}
