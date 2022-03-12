package pl.agh.iet.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.agh.iet.model.CreateStreamRequest;
import pl.agh.iet.model.CreateStreamResponse;
import pl.agh.iet.model.DeleteStreamRequest;
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
    public ResponseEntity<CreateStreamResponse> addVideo(@Valid @ModelAttribute CreateStreamRequest request) {

        String id = streamingService.createStream(request);

        return ResponseEntity.ok(CreateStreamResponse.builder()
                .id(id)
                .build());
    }

    @GetMapping("/{streamName}/{m3u8File}")
    public ResponseEntity<String> getM3u8File(@PathVariable String streamName, @PathVariable String m3u8File) {
        return ResponseEntity.ok(streamingService.getM3u8File(streamName, m3u8File));
    }

    @GetMapping("/{streamName}/{segmentName}/{chunkName}")
    public ResponseEntity<Resource> getChunk(@PathVariable String streamName, @PathVariable String segmentName, @PathVariable String chunkName) {
        return getResource(streamingService.getChunk(streamName, segmentName, chunkName));
    }

    @DeleteMapping
    public ResponseEntity<Object> deleteStream(@RequestBody DeleteStreamRequest request) {
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
