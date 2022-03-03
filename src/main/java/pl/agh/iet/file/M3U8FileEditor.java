package pl.agh.iet.file;

import pl.agh.iet.service.streaming.quality.Quality;

import java.util.Collection;

public interface M3U8FileEditor {

    /**
     * Sets .m3u8 file content to support streaming via HTTP
     *
     * @param videoName
     */
    void setFileContent(String videoName, Collection<Quality> qualitiesFromHighest);
}
