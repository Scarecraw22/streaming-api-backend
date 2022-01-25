package pl.agh.iet.ffmpeg;

import lombok.Builder;
import lombok.Value;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import pl.agh.iet.ffmpeg.args.PresetArg;
import pl.agh.iet.ffmpeg.args.VideoCodecArg;
import pl.agh.iet.ffmpeg.hls.HlsPlaylistType;
import pl.agh.iet.video.quality.Quality;

import java.nio.file.Path;
import java.util.Collection;

public interface FfmpegBuilderCreator {

    /**
     * Creates {@link FFmpegBuilder} based on given {@link Config}
     *
     * @param config {@link Config}
     * @return {@link FFmpegBuilder} that is prepared to split video and create chunks for HLS streaming
     */
    FFmpegBuilder createFfmpegBuilder(Config config);

    @Value
    @Builder
    class Config {
        String baseName;
        String fps;
        String outputPattern;
        String hlsMasterFilename;
        String hlsSegmentFilename;
        String hlsBaseUrl;
        int hlsSegmentDuration;
        Path input;
        PresetArg preset;
        VideoCodecArg videoCodec;
        HlsPlaylistType hlsPlaylistType;
        Collection<Quality> qualitiesFromHighest;
    }
}
