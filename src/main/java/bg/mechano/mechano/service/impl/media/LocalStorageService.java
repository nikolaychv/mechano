package bg.mechano.mechano.service.impl.media;

import bg.mechano.mechano.config.MediaProperties;
import bg.mechano.mechano.service.media.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;

@Service
@RequiredArgsConstructor
public class LocalStorageService implements StorageService {

    private final MediaProperties props;

    @Override
    public String save(byte[] bytes, String relativeKey) {
        try {
            Path base = Path.of(props.getBaseDir()).toAbsolutePath().normalize();
            Path target = base.resolve(relativeKey).normalize();

            if (!target.startsWith(base)) {
                throw new IllegalArgumentException("Invalid storage key");
            }

            Files.createDirectories(target.getParent());
            Files.write(target, bytes);
            return relativeKey;

        } catch (Exception e) {
            throw new RuntimeException("Failed to save file", e);
        }
    }

    @Override
    public Resource loadAsResource(String relativeKey) {
        Path base = Path.of(props.getBaseDir()).toAbsolutePath().normalize();
        Path target = base.resolve(relativeKey).normalize();
        return new FileSystemResource(target);
    }
}