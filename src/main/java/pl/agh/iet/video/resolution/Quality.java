package pl.agh.iet.video.resolution;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pl.agh.iet.audio.AudioChannel;

@Getter
@RequiredArgsConstructor
public enum Quality {

    LOW(480, 270, 400, 64, AudioChannel.MONO);

    private final long width;
    private final long height;
    private final long videoBitrate;
    private final long audioBitrate;
    private final AudioChannel audioChannel;

}
