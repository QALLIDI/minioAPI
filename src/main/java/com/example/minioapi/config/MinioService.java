package com.example.minioapi.config;

import com.example.minioapi.exception.MinioException;
import com.example.minioapi.exception.MinioFetchException;
import io.minio.*;
import io.minio.messages.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class MinioService {
    private static final Logger log = LoggerFactory.getLogger(MinioService.class);
    private final MinioClient minioClient;
    private final MinioConfigurationProperties configurationProperties;

    @Autowired
    public MinioService(MinioClient minioClient, MinioConfigurationProperties configurationProperties) {
        this.minioClient = minioClient;
        this.configurationProperties = configurationProperties;
    }

    public List<Item> list() {
        ListObjectsArgs args = ListObjectsArgs.builder().bucket(this.configurationProperties.getBucket()).prefix("").recursive(false).build();
        Iterable<Result<Item>> myObjects = this.minioClient.listObjects(args);
        return this.getItems(myObjects);
    }

    public List<Item> fullList() {
        ListObjectsArgs args = ListObjectsArgs.builder().bucket(this.configurationProperties.getBucket()).build();
        Iterable<Result<Item>> myObjects = this.minioClient.listObjects(args);
        return this.getItems(myObjects);
    }

    public List<Item> list(Path path) {
        ListObjectsArgs args = ListObjectsArgs.builder().bucket(this.configurationProperties.getBucket()).prefix(path.toString()).recursive(false).build();
        Iterable<Result<Item>> myObjects = this.minioClient.listObjects(args);
        return this.getItems(myObjects);
    }

    public List<Item> getFullList(Path path) {
        ListObjectsArgs args = ListObjectsArgs.builder().bucket(this.configurationProperties.getBucket()).prefix(path.toString()).build();
        Iterable<Result<Item>> myObjects = this.minioClient.listObjects(args);
        return this.getItems(myObjects);
    }

    private List<Item> getItems(Iterable<Result<Item>> myObjects) {
        return StreamSupport.stream(myObjects.spliterator(), true).map((itemResult) -> {
            try {
                return itemResult.get();
            } catch (Exception var2) {
                throw new MinioFetchException("Error while parsing list of objects", var2);
            }
        }).collect(Collectors.toList());
    }

    public InputStream get(Path path) throws MinioException {
        try {
            GetObjectArgs args = GetObjectArgs.builder().bucket(this.configurationProperties.getBucket()).object(path.toString()).build();
            return this.minioClient.getObject(args);
        } catch (Exception var3) {
            throw new MinioException("Error while fetching files in Minio", var3);
        }
    }

    public StatObjectResponse getMetadata(Path path) throws MinioException {
        try {
            StatObjectArgs args = StatObjectArgs.builder().bucket(this.configurationProperties.getBucket()).object(path.toString()).build();
            return this.minioClient.statObject(args);
        } catch (Exception var3) {
            throw new MinioException("Error while fetching files in Minio", var3);
        }
    }

    public Map<Path, StatObjectResponse> getMetadata(Iterable<Path> paths) {
        return (Map) StreamSupport.stream(paths.spliterator(), false).map((path) -> {
            try {
                StatObjectArgs args = StatObjectArgs.builder().bucket(this.configurationProperties.getBucket()).object(path.toString()).build();
                return new AbstractMap.SimpleEntry(path, this.minioClient.statObject(args));
            } catch (Exception var3) {
                throw new MinioFetchException("Error while parsing list of objects", var3);
            }
        }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public void getAndSave(Path source, String fileName) throws MinioException {
        try {
            DownloadObjectArgs args = DownloadObjectArgs.builder().bucket(this.configurationProperties.getBucket()).object(source.toString()).filename(fileName).build();
            this.minioClient.downloadObject(args);
        } catch (Exception var4) {
            throw new MinioException("Error while fetching files in Minio", var4);
        }
    }

    public void upload(Path source, InputStream file, Map<String, String> headers) throws MinioException {
        try {
            PutObjectArgs args = PutObjectArgs.builder().bucket(this.configurationProperties.getBucket()).object(source.toString()).stream(file, file.available(), -1L).headers(headers).build();
            this.minioClient.putObject(args);
        } catch (Exception var5) {
            throw new MinioException("Error while fetching files in Minio", var5);
        }
    }

    public void upload(Path source, InputStream file) throws MinioException {
        try {
            PutObjectArgs args = PutObjectArgs.builder().bucket(this.configurationProperties.getBucket()).object(source.toString()).stream(file, file.available(), -1L).build();
            this.minioClient.putObject(args);
        } catch (Exception var4) {
            throw new MinioException("Error while fetching files in Minio", var4);
        }
    }

    public void upload(Path source, InputStream file, String contentType, Map<String, String> headers) throws MinioException {
        try {
            PutObjectArgs args = PutObjectArgs.builder().bucket(this.configurationProperties.getBucket()).object(source.toString()).stream(file, file.available(), -1L).headers(headers).contentType(contentType).build();
            this.minioClient.putObject(args);
        } catch (Exception var6) {
            throw new MinioException("Error while fetching files in Minio", var6);
        }
    }

    public void upload(Path source, InputStream file, String contentType) throws MinioException {
        try {
            PutObjectArgs args = PutObjectArgs.builder().bucket(this.configurationProperties.getBucket()).object(source.toString()).stream(file, file.available(), -1L).contentType(contentType).build();
            this.minioClient.putObject(args);
        } catch (Exception var5) {
            throw new MinioException("Error while fetching files in Minio", var5);
        }
    }

    public void upload(Path source, File file) throws MinioException {
        try {
            UploadObjectArgs args = UploadObjectArgs.builder().bucket(this.configurationProperties.getBucket()).object(source.toString()).filename(file.getAbsolutePath()).build();
            this.minioClient.uploadObject(args);
        } catch (Exception var4) {
            throw new MinioException("Error while fetching files in Minio", var4);
        }
    }

    public void remove(Path source) throws MinioException {
        try {
            RemoveObjectArgs args = RemoveObjectArgs.builder().bucket(this.configurationProperties.getBucket()).object(source.toString()).build();
            this.minioClient.removeObject(args);
        } catch (Exception var3) {
            throw new MinioException("Error while fetching files in Minio", var3);
        }
    }
}
