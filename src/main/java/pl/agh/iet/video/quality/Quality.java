package pl.agh.iet.video.quality;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pl.agh.iet.audio.AudioChannel;

@Getter
@RequiredArgsConstructor
public enum Quality {
    LOW(480, 270, 400, 64, AudioChannel.MONO),
    MED(640, 360, 1000, 96, AudioChannel.STEREO),
    HIGH(960, 540, 1500, 96, AudioChannel.STEREO),
    HD_720(1280, 720, 3000, 128, AudioChannel.STEREO),
    HD_1080(1920, 1080, 6000, 192, AudioChannel.STEREO),
    _4K(3840, 2160, 12000, 192, AudioChannel.STEREO);

    private final int width;
    private final int height;
    private final int videoBitrate;
    private final int audioBitrate;
    private final AudioChannel audioChannel;
}
