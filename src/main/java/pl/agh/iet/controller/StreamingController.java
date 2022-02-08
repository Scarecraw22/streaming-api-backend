package pl.agh.iet.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.agh.iet.video.VideoService;
import pl.agh.iet.video.model.Video;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

@Slf4j
@CrossOrigin
@RestController
@RequiredArgsConstructor
@RequestMapping("/streaming-api")
public class StreamingController {

    private final VideoService videoService;

    @PostMapping(value = "/video", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public void addVideo(@RequestParam String name, @RequestParam MultipartFile content) {
        videoService.prepareForHlsStreaming(new Video(name, content));
    }

    @GetMapping("/{streamName}")
    public ResponseEntity<String> getMasterFile(@PathVariable String streamName) {
        return ResponseEntity.ok(videoService.getMasterFileContent(streamName));
    }

    @GetMapping("/{streamName}/{segmentName}")
    public ResponseEntity<String> getSegmentMaster(@PathVariable String streamName, @PathVariable String segmentName) {
        return ResponseEntity.ok(videoService.getSegmentMasterFileContent(streamName, segmentName));
    }

    @GetMapping("/{streamName}/{segmentName}/{chunkName}")
    public ResponseEntity<Resource> getChunk(@PathVariable String streamName, @PathVariable String segmentName, @PathVariable String chunkName) {
        return getResource(videoService.getChunk(streamName, segmentName, chunkName));
    }

    private ResponseEntity<Resource> getResource(File file) {
        try {
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
