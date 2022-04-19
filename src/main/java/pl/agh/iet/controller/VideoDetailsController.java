package pl.agh.iet.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.agh.iet.influxdb.StaticInfluxDbClient;
import pl.agh.iet.model.GetVideoDetailsListResponse;
import pl.agh.iet.model.SearchStreamRequest;
import pl.agh.iet.service.video.VideoService;

import javax.validation.Valid;

@Slf4j
@CrossOrigin
@RestController
@RequiredArgsConstructor
@RequestMapping("/video-details")
public class VideoDetailsController {

    private final VideoService videoService;

    @GetMapping
    public ResponseEntity<GetVideoDetailsListResponse> getVideoDetailsList() {
        StaticInfluxDbClient.incrementRequestCounter();
        return ResponseEntity.ok(videoService.getVideoDetailsList());
    }

    @PostMapping(value = "/search", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<GetVideoDetailsListResponse> addVideo(@Valid @ModelAttribute SearchStreamRequest request) {
        return ResponseEntity.ok(videoService.filterStreams(request));
    }
}
