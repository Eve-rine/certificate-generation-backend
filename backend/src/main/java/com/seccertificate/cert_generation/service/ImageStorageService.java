package com.seccertificate.cert_generation.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

// S3 integration:
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class ImageStorageService {

    @Value("${storage.backend:local}") // 'local' or 's3'
    private String backend;

    @Value("${storage.local.path:uploads}")
    private String localBasePath;

    @Value("${storage.s3.bucket:my-bucket}")
    private String s3Bucket;

    @Value("${storage.s3.region:us-east-1}")
    private String s3Region;

    @Value("${storage.s3.accessKey:}")
    private String s3AccessKey;

    @Value("${storage.s3.secretKey:}")
    private String s3SecretKey;

    public String upload(String customerId, MultipartFile file) throws IOException {
        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        if ("s3".equalsIgnoreCase(backend)) {
            return uploadToS3(customerId, filename, file);
        } else {
            return uploadToLocal(customerId, filename, file);
        }
    }

    private String uploadToLocal(String customerId, String filename, MultipartFile file) throws IOException {
        Path dir = Paths.get(localBasePath, customerId);
        Path path = dir.resolve(filename);

        Files.createDirectories(dir);
        Files.write(path, file.getBytes());

        // Return local URL (adjust according to your static mapping)
        return "/uploads/" + customerId + "/" + filename;
    }

    private String uploadToS3(String customerId, String filename, MultipartFile file) throws IOException {
        // Create S3 client
        S3Client s3 = S3Client.builder()
                .region(Region.of(s3Region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(s3AccessKey, s3SecretKey)
                ))
                .build();

        String key = customerId + "/" + filename;

        PutObjectRequest putObj = PutObjectRequest.builder()
                .bucket(s3Bucket)
                .key(key)
                .contentType(file.getContentType())
                .build();

        s3.putObject(putObj, RequestBody.fromBytes(file.getBytes()));

        // Return S3 public URL (or pre-signed if your bucket isn't public)
        return String.format("https://%s.s3.%s.amazonaws.com/%s", s3Bucket, s3Region, key);
    }
}