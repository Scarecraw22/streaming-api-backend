package pl.agh.iet.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.agh.iet.model.AddVideoRequest;
import pl.agh.iet.model.AddVideoResponse;
import pl.agh.iet.model.DeleteVideoRequest;
import pl.agh.iet.service.streaming.StreamingService;

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

    private final StreamingService streamingService;

    @PostMapping(value = "/video", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<AddVideoResponse> addVideo(@Valid @ModelAttribute AddVideoRequest request) {

        String id = streamingService.addVideo(request);

        return ResponseEntity.ok(AddVideoResponse.builder()
                .id(id)
                .build());
    }

    @GetMapping("/{videoName}/{m3u8File}")
    public ResponseEntity<String> getM3u8File(@PathVariable String videoName, @PathVariable String m3u8File) {
        return ResponseEntity.ok(streamingService.getM3u8File(videoName, m3u8File));
    }

    @GetMapping("/{videoName}/{segmentName}/{chunkName}")
    public ResponseEntity<Resource> getChunk(@PathVariable String videoName, @PathVariable String segmentName, @PathVariable String chunkName) {
        return getResource(streamingService.getChunk(videoName, segmentName, chunkName));
    }

    @DeleteMapping
    public ResponseEntity<Object> deleteStream(@RequestBody DeleteVideoRequest request) {
        streamingService.deleteStreamById(request.getId());
        return ResponseEntity.status(200).build();
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
