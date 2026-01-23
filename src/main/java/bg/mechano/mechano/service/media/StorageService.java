package bg.mechano.mechano.service.media;

import org.springframework.core.io.Resource;

public interface StorageService {
    String save(byte[] bytes, String relativeKey);
    Resource loadAsResource(String relativeKey);
}