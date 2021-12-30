package pl.agh.iet.file;

import com.google.common.io.Resources;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

@UtilityClass
public class FileUtils {

    public static File getFileFromResources(@NonNull String pathString) {
        try {
            Path path = Paths.get(Resources.getResource(pathString).toURI());
            return path.toFile();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
