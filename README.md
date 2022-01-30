
#### Environment setup:

1. Go to page: https://github.com/BtbN/FFmpeg-Builds/releases
2. Download ```ffmpeg-n4.4-latest-win64-gpl-4.4.zip``` package
3. Move to your desired dir and setup application.yml properties for ffmpeg and ffprobe 
Example application.yml:

![img.png](img.png)

#### To test:
1. Run app
2. Enter into web browser: http://localhost:8080/streaming-api/test
3. Get response body and paste it into file with .m3u8 extension
4. Open that file using VLC
5. Check if files are created in dir defined in application.yml