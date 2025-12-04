package com.seccertificate.cert_generation.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class ImageStorageService {

    public String upload(String customerId, MultipartFile file) throws IOException, IOException {
        // TODO: Upload to MinIO, S3, or local folder.
        // Return public URL or path.

        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path path = Paths.get("uploads/" + customerId + "/" + filename);

        Files.createDirectories(path.getParent());
        Files.write(path, file.getBytes());

        return "/uploads/" + customerId + "/" + filename;
    }
}
