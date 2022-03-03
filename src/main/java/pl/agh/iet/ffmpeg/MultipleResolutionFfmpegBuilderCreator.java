package pl.agh.iet.ffmpeg;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import org.springframework.stereotype.Service;
import pl.agh.iet.ffmpeg.args.FormatArg;
import pl.agh.iet.ffmpeg.args.VideoCodecArg;
import pl.agh.iet.ffmpeg.hls.HlsFFmpegBuilder;
import pl.agh.iet.ffmpeg.hls.HlsFFmpegOutputBuilder;
import pl.agh.iet.utils.StringConsts;
import pl.agh.iet.service.streaming.quality.Quality;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class MultipleResolutionFfmpegBuilderCreator implements FfmpegBuilderCreator {

    @Override
    public FFmpegBuilder createFfmpegBuilder(Config config) {
        HlsFFmpegOutputBuilder builder = new HlsFFmpegBuilder(new FFmpegBuilder())
                .setInput(config.getInput().toAbsolutePath().toString())
                .addOutput(config.getOutputPattern());

        String complexFilter = createComplexFilter(config.getQualitiesFromHighest());

        builder = builder
                .addFilterComplex(complexFilter)
                .setPreset(config.getPreset())
                .setGopSize(config.getFps())
                .makeSegmentsEqualSized();

        builder = builder.addExtraArgs("-ar", "48000");
//                .addExtraArgs("-ab", "128k")
//                .addExtraArgs("-acodec", "aac");
        builder = addMapArgs(builder, config.getVideoCodec(), config.getQualitiesFromHighest());

        builder.setFormat(FormatArg.HLS)
                .setHlsSegmentDuration(config.getHlsSegmentDuration())
                .setHlsPlaylistType(config.getHlsPlaylistType())
                .setMasterPlaylistName(config.getHlsMasterFilename())
                .setHlsSegmentFilename(config.getHlsSegmentFilename())
                .setHlsSegmentType(config.getHlsSegmentType())
                .addExtraArgs("-use_localtime_mkdir", "1");

        String varStreamMap = createVarStreamMap(config.getQualitiesFromHighest().size());

        return builder.addExtraArgs("-var_stream_map", varStreamMap)
                .done();
    }

    private String createComplexFilter(Collection<Quality> qualitiesFromHighest) {
        List<Quality> qualities = new ArrayList<>(qualitiesFromHighest);
        StringBuilder filterComplex = new StringBuilder();
        int numberOfStreams = qualitiesFromHighest.size();
        filterComplex.append(String.format("[v:0]split=%d", numberOfStreams));

        for (int i = 1; i <= numberOfStreams; i++) {
            filterComplex.append(String.format("[v%d]", i));
        }
        filterComplex.append(StringConsts.SEMICOLON);

        for (int i = 1; i <= numberOfStreams; i++) {
            Quality quality = qualities.get(i - 1);
            int width = quality.getWidth();
            int height = quality.getHeight();
            filterComplex.append(String.format("[v%d]scale=w=%d:h=%d[v%dout]", i, width, height, i));

            if (i != numberOfStreams) {
                filterComplex.append(StringConsts.SEMICOLON);
            }
        }

        return filterComplex.toString();
    }

    private HlsFFmpegOutputBuilder addMapArgs(HlsFFmpegOutputBuilder builder, VideoCodecArg videoCodec, Collection<Quality> qualitiesFromHighest) {
        List<Quality> qualities = new ArrayList<>(qualitiesFromHighest);

        for (int i = 1; i <= qualities.size(); i++) {
            Quality quality = qualities.get(i - 1);
            String mapValue = String.format("[v%dout] v:0", i);
            String codecArg = String.format("-c:v:%d", i - 1);
            String bitrateVideoArg = String.format("-b:v:%d", i - 1);
            String bitrateValue = String.format("%dk", quality.getVideoBitrate());

            builder = builder.addExtraArgs("-map", mapValue)
                    .addExtraArgs(codecArg, videoCodec.getValue())
                    .addExtraArgs(bitrateVideoArg, bitrateValue)
                    .addExtraArgs("-maxrate:v:" + (i-1), "5M")
                    .addExtraArgs("-minrate:v:" + (i-1), "5M")
                    .addExtraArgs("-bufsize:v:" + (i-1), "10M")
                    .addExtraArgs("-map", "a:0")
                    .addExtraArgs("-c:a:" + (i-1), "aac")
                    .addExtraArgs("-b:a:" + (i-1), "128k");
        }

        return builder;
    }

    private String createVarStreamMap(int size) {
        return IntStream.range(0, size)
                .mapToObj(i -> String.format("v:%d,a:%d", i, i))
                .collect(Collectors.joining(StringConsts.SPACE));
    }
}
