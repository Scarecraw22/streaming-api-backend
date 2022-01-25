package pl.agh.iet.controller;

import lombok.RequiredArgsConstructor;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import pl.agh.iet.ffmpeg.FfmpegProperties;
import pl.agh.iet.utils.FileUtils;
import pl.agh.iet.video.VideoService;
import pl.agh.iet.video.model.Video;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

@RestController
@RequiredArgsConstructor
@RequestMapping("/streaming-api")
public class StreamingController {

    private final FfmpegProperties ffmpegProperties;
    private final VideoService videoService;

    @PostMapping(value = "/video", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public void addVideo(@ModelAttribute Video video) {
        videoService.prepareForHlsStreaming(video);
    }

    @GetMapping("/test/{videoName}")
    public ResponseEntity<String> test(@PathVariable String videoName) throws IOException {

        // Only for test purposes replace
        try {
            Files.createDirectories(Paths.get(ffmpegProperties.getOutputDir()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        File file = FileUtils.getFileFromResources("movie.mp4");
        FileItem fileItem = new DiskFileItemFactory().createItem(file.getName(), Files.probeContentType(file.toPath()), false, file.getName());
        try (InputStream in = new FileInputStream(file); OutputStream out = fileItem.getOutputStream()) {
            in.transferTo(out);
        } catch (IOException e) {
            e.printStackTrace();
        }
        CommonsMultipartFile multipartFile = new CommonsMultipartFile(fileItem);
        videoService.prepareForHlsStreaming(new Video(videoName, multipartFile));

        return ResponseEntity.ok("Elo");
    }

    @GetMapping("/{streamName}/{filename}")
    public ResponseEntity<Resource> getFile(@PathVariable String streamName, @PathVariable String filename) {

        try {
            File file = FileUtils.getFileFromResources(Paths.get(streamName).resolve(filename).toString());

            InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

            return ResponseEntity.ok()
                    .contentLength(file.length())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return ResponseEntity.internalServerError().build();
    }
}
