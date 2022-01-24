package pl.agh.iet.ffmpeg.hls;

import lombok.RequiredArgsConstructor;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.builder.FFmpegOutputBuilder;

@RequiredArgsConstructor
public class HlsFFmpegOutputBuilder {

    private final FFmpegOutputBuilder output;

    public HlsFFmpegOutputBuilder addExtraArgs(String... values) {
        output.addExtraArgs(values);
        return this;
    }

    public HlsFFmpegOutputBuilder setAudioCodec(String codec) {
        output.setAudioCodec(codec);
        return this;
    }

    public HlsFFmpegOutputBuilder setVideoCodec(String codec) {
        output.setVideoCodec(codec);
        return this;
    }

    public HlsFFmpegOutputBuilder setFormat(String format) {
        output.setFormat(format);
        return this;
    }

    public HlsFFmpegOutputBuilder setVideoQuality(int quality) {
        output.setVideoQuality(quality);
        return this;
    }

    public HlsFFmpegOutputBuilder setAudioChannels(int channels) {
        output.setAudioChannels(channels);
        return this;
    }

    public HlsFFmpegOutputBuilder setAudioBitRate(int bitRate) {
        output.setAudioBitRate(bitRate);
        return this;
    }

    /**
     * Sets Group of Picture size. GOP - number of frames between two keyframes.
     * Should be the same as FPS for specific video
     *
     * @param size
     * @return
     */
    public HlsFFmpegOutputBuilder setGopSize(String size) {
        output.addExtraArgs("-g", size);
        return this;
    }

    public HlsFFmpegOutputBuilder makeSegmentsEqualSized() {
        output.addExtraArgs("-sc_threshold", "0");
        return this;
    }

    public HlsFFmpegOutputBuilder setHlsSegmentDuration(int durationInSeconds) {
        output.addExtraArgs("-hls_time", String.valueOf(durationInSeconds));
        return this;
    }

    public HlsFFmpegOutputBuilder setHlsPlaylistType(HlsPlaylistType type) {
        output.addExtraArgs("-hls_playlist_type", type.getValue());
        return this;
    }

    public HlsFFmpegOutputBuilder setMasterPlaylistName(String masterPlaylistName) {
        output.addExtraArgs("-master_pl_name", masterPlaylistName);

        return this;
    }

    public FFmpegBuilder done() {
        return output.done();
    }
}
