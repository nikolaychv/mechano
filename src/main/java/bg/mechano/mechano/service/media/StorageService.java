package bg.mechano.mechano.service.media;

import org.springframework.core.io.Resource;

import java.io.InputStream;

public interface StorageService {

    /**
     * Saves content under a relative key.
     * contentLength and contentType are important for remote storages (S3, GCS, etc.).
     *
     * @return the stored key (usually same as relativeKey)
     */
    String save(InputStream in, long contentLength, String contentType, String relativeKey);

    Resource loadAsResource(String relativeKey);
}