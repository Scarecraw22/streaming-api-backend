package pl.agh.iet.ffmpeg.hls;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum HlsSegmentType {
    MPEGTS("mpegts");

    private final String value;
}
