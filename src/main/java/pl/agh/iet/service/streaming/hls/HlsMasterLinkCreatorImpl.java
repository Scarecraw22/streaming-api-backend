package pl.agh.iet.service.streaming.hls;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.agh.iet.ffmpeg.FfmpegProperties;
import pl.agh.iet.utils.HlsConsts;
import pl.agh.iet.utils.StringConsts;

@Service
@RequiredArgsConstructor
public class HlsMasterLinkCreatorImpl implements HlsMasterLinkCreator {

    private final FfmpegProperties ffmpegProperties;

    @Override
    public String createMasterLink(String streamName) {
        return ffmpegProperties.getServerUrl()
                + streamName
                + StringConsts.SLASH
                + HlsConsts.MASTER_M3U8;
    }
}
