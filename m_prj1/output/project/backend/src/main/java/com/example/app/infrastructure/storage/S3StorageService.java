package com.example.app.infrastructure.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3StorageService {

    private final S3Client s3Client;
    private final S3Presigner presigner;

    @Value("${s3.bucket-name}")
    private String bucketName;

    @Value("${s3.cdn-url:}")
    private String cdnUrl;

    public String upload(MultipartFile file, String directory) throws IOException {
        validateFile(file);
        String key = directory + "/" + UUID.randomUUID() + getExtension(file.getOriginalFilename());

        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .contentType(file.getContentType())
                        .contentLength(file.getSize())
                        .build(),
                RequestBody.fromInputStream(file.getInputStream(), file.getSize())
        );

        return buildUrl(key);
    }

    public record PresignedUrlResponse(String uploadUrl, String fileUrl, String key) {}

    public PresignedUrlResponse generatePresignedUrl(String filename, String contentType) {
        String key = "uploads/" + UUID.randomUUID() + getExtension(filename);

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(15))
                .putObjectRequest(req -> req
                        .bucket(bucketName)
                        .key(key)
                        .contentType(contentType))
                .build();

        String uploadUrl = presigner.presignPutObject(presignRequest).url().toString();
        String fileUrl = buildUrl(key);

        return new PresignedUrlResponse(uploadUrl, fileUrl, key);
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) throw new IllegalArgumentException("파일이 비어있습니다");
        if (file.getSize() > 50 * 1024 * 1024) {
            throw new IllegalArgumentException("파일 크기는 50MB를 초과할 수 없습니다");
        }
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.startsWith("image/") &&
                !contentType.equals("application/pdf"))) {
            throw new IllegalArgumentException("지원하지 않는 파일 형식입니다");
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf("."));
    }

    private String buildUrl(String key) {
        return cdnUrl.isEmpty()
                ? "https://" + bucketName + ".s3.amazonaws.com/" + key
                : cdnUrl + "/" + key;
    }
}
