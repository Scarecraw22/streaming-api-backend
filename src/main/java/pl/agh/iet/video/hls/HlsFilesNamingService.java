package pl.agh.iet.video.hls;

public interface HlsFilesNamingService {

    /**
     * Creates template for naming convention in HLS data segments.
     * Example:
     * <pre><code>baseName/data_0001.ts</code></pre>
     * <pre><code>baseName/data_0002.ts</code></pre>
     * <pre><code>baseName/data_0003.ts</code></pre>
     *
     * @param baseName Prefix for .ts files
     * @return
     */
    String createHlsSegmentName(String baseName);

    /**
     * Creates template for HLS master filename
     * Example:
     * <pre><code>baseName_master.m3u8</code></pre>
     *
     * @param baseName Prefix for master filename
     * @return
     */
    String createHlsMasterFilename(String baseName);

    /**
     * Creates filename pattern for each m3u8 file.
     * Example:
     * <pre><code>baseName/data_0_master.m3u8</code></pre>
     * <pre><code>baseName/data_1_master.m3u8</code></pre>
     * <pre><code>baseName/data_2_master.m3u8</code></pre>
     *
     * @param baseName Filename pattern will be based on given string
     * @return
     */
    String createHlsOutputPattern(String baseName);
}
