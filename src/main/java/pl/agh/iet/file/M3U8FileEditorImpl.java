package pl.agh.iet.file;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.agh.iet.ex.StreamingServerException;
import pl.agh.iet.ffmpeg.FfmpegProperties;
import pl.agh.iet.utils.StringConsts;
import pl.agh.iet.video.quality.Quality;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class M3U8FileEditorImpl implements M3U8FileEditor {

    private static final String MASTER = "master";
    private static final String M3U8_EXTENSION = ".m3u8";
    private static final Pattern RESOLUTION_PATTERN = Pattern.compile("RESOLUTION=(\\d+x\\d+)");

    private final FfmpegProperties ffmpegProperties;

    @Override
    public void setFileContent(String streamName, Collection<Quality> qualitiesFromHighest) {

        Path rootStreamPath = Path.of(ffmpegProperties.getOutputDir())
                .resolve(streamName);

        List<File> allM3u8Files = new ArrayList<>();

        List<File> dirsWithChunks = new ArrayList<>();

        Arrays.stream(Objects.requireNonNull(rootStreamPath.toFile().listFiles()))
                .forEach(file -> {
                    if (file.isFile()) {
                        allM3u8Files.add(file);
                    } else {
                        dirsWithChunks.add(file);
                    }
                });

        allM3u8Files.sort(Comparator.comparing(File::getName));
        dirsWithChunks.sort(Comparator.comparing(File::getName));

        log.info("Found following .m3u8 files: {}, for stream: {}", allM3u8Files.stream().map(File::getName).collect(Collectors.toList()), streamName);

        File masterFile = allM3u8Files.remove(allM3u8Files.size() - 1);
        setupMasterFileContent(masterFile.toPath(), streamName);

        List<Quality> qualities = new ArrayList<>(qualitiesFromHighest);
        if (qualities.size() == allM3u8Files.size() && allM3u8Files.size() == dirsWithChunks.size()) {
            for (int index = 0; index < qualities.size(); index++) {
                Quality currentQuality = qualities.get(index);
                setupSegmentFileContent(allM3u8Files.get(index).toPath(), streamName, currentQuality);

                File currentDir = dirsWithChunks.get(index);
                renameDirectory(currentDir, streamName, currentQuality);
            }
        } else {
            throw new IllegalStateException("Qualities list are not equal to m3u8 segment files list and directories with chunks list");
        }
    }

    private void setupMasterFileContent(Path masterFile, String streamName) {
        try {
            log.info("Starting setup process of {} file for stream: {}", masterFile.toFile().getName(), streamName);
            LinkedList<String> newLines = new LinkedList<>();
            String currentResolution = StringConsts.EMPTY_STRING;

            for (String currentLine : Files.readAllLines(masterFile, StandardCharsets.UTF_8)) {
                boolean extensionLine = !currentLine.isBlank() && currentLine.startsWith(StringConsts.HASH);
                if (extensionLine) {
                    newLines.addLast(currentLine);
                    Matcher matcher = RESOLUTION_PATTERN.matcher(currentLine);
                    if (matcher.find()) {
                        currentResolution = matcher.group(1);
                    }

                } else if (!currentLine.isBlank()) {
                    String lineToAdd = ffmpegProperties.getServerUrl()
                            + streamName
                            + StringConsts.SLASH
                            + streamName
                            + StringConsts.UNDERSCORE
                            + currentResolution
                            + M3U8_EXTENSION;

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

    private void setupSegmentFileContent(Path segmentFile, String streamName, Quality quality) {
        try {
            log.info("Starting setup process of {} file for stream: {}", segmentFile.toFile().getName(), streamName);

            LinkedList<String> newLines = new LinkedList<>();

            for (String currentLine : Files.readAllLines(segmentFile, StandardCharsets.UTF_8)) {
                boolean extensionLine = !currentLine.isBlank() && currentLine.startsWith(StringConsts.HASH);
                if (extensionLine) {
                    newLines.addLast(currentLine);

                } else if (!currentLine.isBlank()) {
                    Path segmentPath = Path.of(currentLine);
                    String chunkName = segmentPath.toFile().getName();

                    String lineToAdd = ffmpegProperties.getServerUrl()
                            + streamName
                            + StringConsts.SLASH
                            + streamName
                            + StringConsts.UNDERSCORE
                            + quality.getResolution()
                            + StringConsts.SLASH
                            + chunkName;

                    newLines.addLast(lineToAdd);
                } else {
                    newLines.addLast(currentLine);
                }
            }

            Files.write(segmentFile, newLines, StandardCharsets.UTF_8);
            String newFilename = streamName + StringConsts.UNDERSCORE + quality.getResolution() + M3U8_EXTENSION;
            Files.move(segmentFile, segmentFile.resolveSibling(newFilename));

            log.info("Finished setup process of {} file for stream: {}", segmentFile.toFile().getName(), streamName);
        } catch (IOException e) {
            throw new StreamingServerException("Error while reading file: " + segmentFile.toFile().getName(), e);
        }
    }

    private void renameDirectory(File currentDir, String streamName, Quality quality) {
        try {
            String newDirname = streamName + StringConsts.UNDERSCORE + quality.getResolution();
            Files.move(currentDir.toPath(), currentDir.toPath().resolveSibling(newDirname));
        } catch (IOException e) {
            throw new IllegalStateException("Error while trying to rename directory: " + currentDir.getName(), e);
        }
    }
}
