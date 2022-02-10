package pl.agh.iet.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.agh.iet.model.CreateStreamRequest;
import pl.agh.iet.video.VideoService;
import pl.agh.iet.video.model.Video;

import javax.validation.Valid;
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
    public void addVideo(@Valid @ModelAttribute CreateStreamRequest request) {


        videoService.prepareForHlsStreaming(Video.builder()
                .name(request.getName())
                .content(request.getContent())
                .build());
    }

    @GetMapping("/{streamName}/{m3u8File}")
    public ResponseEntity<String> getM3u8File(@PathVariable String streamName, @PathVariable String m3u8File) {
        return ResponseEntity.ok(videoService.getM3u8File(streamName, m3u8File));
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
