package pl.agh.iet.service.streaming.metadata;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import net.bramp.ffmpeg.probe.FFmpegStream;
import org.apache.commons.lang3.math.Fraction;
import org.springframework.stereotype.Service;
import pl.agh.iet.service.streaming.quality.HighestQualityStrategy;
import pl.agh.iet.service.streaming.quality.Quality;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class MetadataServiceImpl implements MetadataService {

    private final FFprobe ffprobe;

    @Override
    public Metadata getMetadata(Path path) {

        try {
            FFmpegProbeResult probeResult = ffprobe.probe(path.toAbsolutePath().toString());
            FFmpegStream stream = Optional.ofNullable(probeResult)
                    .map(FFmpegProbeResult::getStreams)
                    .filter(streams -> streams.size() > 0)
                    .map(streams -> streams.get(0))
                    .orElseThrow(() -> new MetadataServiceException("Cannot find stream in " + FFmpegProbeResult.class));

            BigDecimal fps = getRoundedFps(stream.avg_frame_rate);

            return Metadata.builder()
                    .fps(fps)
                    .resolution(Metadata.Resolution.builder()
                            .width(stream.width)
                            .height(stream.height)
                            .build())
                    .duration(BigDecimal.valueOf(stream.duration).setScale(2, RoundingMode.HALF_UP))
                    .build();

        } catch (IOException e) {
            throw new MetadataServiceException("Error while trying to get info from video: " + path.toAbsolutePath(), e);
        }
    }

    @Override
    public Collection<Quality> getQualitiesFromHighest(Metadata.Resolution resolution) {
        HighestQualityStrategy strategy = HighestQualityStrategy.HIGHEST_WIDTH;
        log.info("Choosing Qualities starting from the highest possible quality by strategy: {}, for Resolution: {}", strategy, resolution);
        // TODO we can change this to make this configurable
        // TODO or we can choose during upload in frontend and send it here to backend
        return strategy.getQualitiesFromHighest(resolution);
    }

    private BigDecimal getRoundedFps(Fraction avgFrameRate) {
        BigDecimal numerator = BigDecimal.valueOf(avgFrameRate.getNumerator());
        BigDecimal denominator = BigDecimal.valueOf(avgFrameRate.getDenominator());
        return numerator.divide(denominator, 6, RoundingMode.HALF_UP);
    }
}
