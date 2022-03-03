package pl.agh.iet.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.agh.iet.model.GetVideoDetailsListResponse;
import pl.agh.iet.service.video.VideoService;

@Slf4j
@CrossOrigin
@RestController
@RequiredArgsConstructor
@RequestMapping("/video-details")
public class VideoDetailsController {

    private final VideoService videoService;

    @GetMapping
    public ResponseEntity<GetVideoDetailsListResponse> getVideoDetailsList() {
        return ResponseEntity.ok(videoService.getVideoDetailsList());
    }
}
