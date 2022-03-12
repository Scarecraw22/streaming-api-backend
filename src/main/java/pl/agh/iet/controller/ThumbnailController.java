package pl.agh.iet.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.agh.iet.service.thumbnail.ThumbnailNotExistException;
import pl.agh.iet.service.thumbnail.ThumbnailService;
import pl.agh.iet.utils.FileUtils;

@Slf4j
@CrossOrigin
@RestController
@RequiredArgsConstructor
@RequestMapping("/thumbnail")
public class ThumbnailController {

    private final ThumbnailService thumbnailService;

    @GetMapping("/{stream}")
    public ResponseEntity<byte[]> getThumbnail(@PathVariable String stream) {

        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setCacheControl(CacheControl.noCache().getHeaderValue());
            byte[] bytes = FileUtils.toBytes(thumbnailService.getThumbnail(stream));

            return ResponseEntity.status(HttpStatus.OK)
                    .headers(httpHeaders)
                    .body(bytes);
        } catch (ThumbnailNotExistException e) {
            log.error("Thumbnail for stream: {} not exists", stream);

            return ResponseEntity.badRequest().build();
        }
    }
}
