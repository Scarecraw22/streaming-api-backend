package pl.agh.iet.ffmpeg.hls;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum HlsPlaylistType {
    EVENT("event");

    private final String value;
}
