package pl.agh.iet.utils;

import com.google.common.io.Resources;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Optional;

@Slf4j
@UtilityClass
public class FileUtils {

    public final String TMP_EXTENSION = ".tmp";

    public static File getFileFromResources(@NonNull String pathString) {
        try {
            Path path = Paths.get(Resources.getResource(pathString).toURI());
            return path.toFile();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static File getFile(@NonNull String pathString) {
        Path path = Paths.get(pathString);
        return path.toFile();
    }

    public static String getFileContent(@NonNull String pathString) {
        try {
            return new String(Files.readAllBytes(getFile(pathString).toPath()));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static void copyFile(Path src, Path dst) {
        try {
            if (dst.toFile().createNewFile()) {
                log.info("New file created: {}", dst);
            }
            Files.copy(src, dst, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public Optional<String> getExtension(String filename) {
        return Optional.ofNullable(filename)
                .filter(f -> f.contains(StringConsts.DOT))
                .map(f -> f.substring(filename.lastIndexOf(".") + 1));
    }

    public void copyFile(MultipartFile src, Path dst) {

        try {
            if (dst.getParent().toFile().mkdirs()) {
                log.info("Parent dirs created for file: {}", dst);
            }
            if (dst.toFile().createNewFile()) {
                log.info("Created file: {}", dst);
            }
        } catch (IOException e) {
            log.error("Error while trying to copy file to dst: {}", dst);
            throw new RuntimeException(e);
        }

        try (OutputStream os = new FileOutputStream(dst.toFile())) {
            IOUtils.copy(src.getInputStream(), os);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public byte[] toBytes(File file) {
        try {
            return Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void deleteNonEmptyDir(@NonNull File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();

            if (files != null) {
                Arrays.asList(files)
                        .forEach(FileUtils::deleteNonEmptyDir);
            }

            if (file.delete()) {
                log.info("Directory: {} deleted", file.getAbsolutePath());
            }

        } else if (file.isFile()) {
            if (file.exists() && file.delete()) {
                log.info("File {} deleted", file.getAbsolutePath());
            }
        } else {
            log.warn("Given object is not a file and not a directory: {}", file);
        }
    }
}
