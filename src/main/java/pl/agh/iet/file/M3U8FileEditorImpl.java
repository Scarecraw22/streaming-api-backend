package pl.agh.iet.file;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.agh.iet.ex.StreamingServerException;
import pl.agh.iet.ffmpeg.FfmpegProperties;
import pl.agh.iet.utils.StringConsts;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class M3U8FileEditorImpl implements M3U8FileEditor {

    private static final String MASTER = "master";
    private static final String M3U8_EXTENSION = ".m3u8";

    private final FfmpegProperties ffmpegProperties;

    @Override
    public void setFileContent(String streamName) {

        Path rootStreamPath = Path.of(ffmpegProperties.getOutputDir())
                .resolve(streamName);

        List<File> allM3u8Files = Arrays.stream(Objects.requireNonNull(rootStreamPath.toFile().listFiles()))
                .filter(File::isFile)
                .collect(Collectors.toList());

        log.info("Found following .m3u8 files: {}, for stream: {}", allM3u8Files.stream().map(File::getName).collect(Collectors.toList()), streamName);

        allM3u8Files.forEach(m3u8File -> {
            if (m3u8File.getName().contains(MASTER)) {
                setupMasterFileContent(m3u8File.toPath(), streamName);
            } else {
                setupSegmentFileContent(m3u8File.toPath(), streamName);
            }
        });
    }

    private void setupMasterFileContent(Path masterFile, String streamName) {
        try {
            log.info("Starting setup process of {} file for stream: {}", masterFile.toFile().getName(), streamName);
            LinkedList<String> newLines = new LinkedList<>();

            for (String currentLine : Files.readAllLines(masterFile, StandardCharsets.UTF_8)) {
                boolean extensionLine = !currentLine.isBlank() && currentLine.startsWith(StringConsts.HASH);
                if (extensionLine) {
                    newLines.addLast(currentLine);

                } else if (!currentLine.isBlank()) {
                    String lineToAdd = ffmpegProperties.getServerUrl()
                            + streamName
                            + StringConsts.SLASH
                            + currentLine.trim();

                    newLines.addLast(lineToAdd);
                } else {
                    newLines.addLast(currentLine);
                }
            }

            Files.write(masterFile, newLines, StandardCharsets.UTF_8);

            log.info("Finished setup process of {} file for stream: {}", masterFile.toFile().getName(), streamName);

        } catch (IOException e) {
            throw new StreamingServerException("Error while reading file: " + masterFile.toFile().getName(), e);
        }
    }

    private void setupSegmentFileContent(Path segmentFile, String streamName) {
        try {
            log.info("Starting setup process of {} file for stream: {}", segmentFile.toFile().getName(), streamName);

            LinkedList<String> newLines = new LinkedList<>();

            for (String currentLine : Files.readAllLines(segmentFile, StandardCharsets.UTF_8)) {
                boolean extensionLine = !currentLine.isBlank() && currentLine.startsWith(StringConsts.HASH);
                if (extensionLine) {
                    newLines.addLast(currentLine);

                } else if (!currentLine.isBlank()) {
                    Path segmentPath = Path.of(currentLine);
                    String segmentName = segmentPath.getParent()
                            .toFile().getName();
                    String chunkName = segmentPath.toFile().getName();

                    String lineToAdd = ffmpegProperties.getServerUrl()
                            + streamName
                            + StringConsts.SLASH
                            + segmentName
                            + StringConsts.SLASH
                            + chunkName;

                    newLines.addLast(lineToAdd);
                } else {
                    newLines.addLast(currentLine);
                }
            }

            Files.write(segmentFile, newLines, StandardCharsets.UTF_8);

            log.info("Finished setup process of {} file for stream: {}", segmentFile.toFile().getName(), streamName);
        } catch (IOException e) {
            throw new StreamingServerException("Error while reading file: " + segmentFile.toFile().getName(), e);
        }
    }
}
