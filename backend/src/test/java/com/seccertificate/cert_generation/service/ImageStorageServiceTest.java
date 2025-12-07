package com.seccertificate.cert_generation.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class ImageStorageServiceTest {

    private Path tmpDir;

    @AfterEach
    void cleanup() throws IOException {
        if (tmpDir != null && Files.exists(tmpDir)) {
            Files.walk(tmpDir)
                    .map(Path::toFile)
                    .sorted((a, b) -> -a.compareTo(b))
                    .forEach(f -> f.delete());
        }
    }

    @Test
    void upload_localStoresFileAndReturnsPath() throws Exception {
        tmpDir = Files.createTempDirectory("imgstore");

        ImageStorageService svc = new ImageStorageService();

        // set private fields via reflection
        setField(svc, "backend", "local");
        setField(svc, "localBasePath", tmpDir.toString());

        MockMultipartFile file = new MockMultipartFile("file", "logo.png", "image/png", "data".getBytes());

        String ret = svc.upload("cust123", file);

        assertThat(ret).startsWith("/uploads/cust123/");
        String filename = ret.substring(ret.lastIndexOf('/') + 1);

        Path stored = tmpDir.resolve("cust123").resolve(filename);
        assertThat(Files.exists(stored)).isTrue();
        assertThat(Files.size(stored)).isGreaterThan(0);
    }

    private void setField(Object target, String name, Object value) throws Exception {
        Field f = ImageStorageService.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(target, value);
    }
}
