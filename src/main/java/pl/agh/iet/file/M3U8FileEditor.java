package pl.agh.iet.file;

public interface M3U8FileEditor {

    /**
     * Sets .m3u8 file content to support streaming via HTTP
     *
     * @param videoName
     */
    void setFileContent(String videoName);
}
