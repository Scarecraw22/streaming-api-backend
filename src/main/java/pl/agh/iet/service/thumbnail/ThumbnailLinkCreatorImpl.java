package pl.agh.iet.service.thumbnail;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ThumbnailLinkCreatorImpl implements ThumbnailLinkCreator {

    private final ThumbnailProperties thumbnailProperties;

    @Override
    public String createThumbnailLink(String streamName) {
        return thumbnailProperties.getUrl()
                + streamName;
    }
}
