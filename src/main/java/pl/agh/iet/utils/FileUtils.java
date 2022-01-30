package pl.agh.iet.utils;

import com.google.common.io.Resources;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
}
