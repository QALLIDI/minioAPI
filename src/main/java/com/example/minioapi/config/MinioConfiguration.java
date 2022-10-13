package com.example.minioapi.config;

import com.example.minioapi.exception.MinioException;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.errors.*;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Configuration
@ConditionalOnClass({MinioClient.class})
@EnableConfigurationProperties({MinioConfigurationProperties.class})
@ComponentScan({"com.example.minioapi.config"})
public class MinioConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(MinioConfiguration.class);

    private final MinioConfigurationProperties minioConfigurationProperties;

    public MinioConfiguration(MinioConfigurationProperties minioConfigurationProperties) {
        this.minioConfigurationProperties = minioConfigurationProperties;
    }

    @Bean
    public MinioClient minioClient() throws IOException, InvalidKeyException, NoSuchAlgorithmException, MinioException, ServerException, InsufficientDataException, ErrorResponseException, InvalidResponseException, XmlParserException, InternalException {
        MinioClient minioClient;
        if (!this.configuredProxy()) {
            minioClient = MinioClient.builder().endpoint(this.minioConfigurationProperties.getUrl()).credentials(this.minioConfigurationProperties.getAccessKey(), this.minioConfigurationProperties.getSecretKey()).build();
        } else {
            minioClient = MinioClient.builder().endpoint(this.minioConfigurationProperties.getUrl()).credentials(this.minioConfigurationProperties.getAccessKey(), this.minioConfigurationProperties.getSecretKey()).httpClient(this.client()).build();
        }

        minioClient.setTimeout(this.minioConfigurationProperties.getConnectTimeout().toMillis(), this.minioConfigurationProperties.getWriteTimeout().toMillis(), this.minioConfigurationProperties.getReadTimeout().toMillis());
        if (this.minioConfigurationProperties.isCheckBucket()) {
            try {
                LOGGER.debug("Checking if bucket {} exists", this.minioConfigurationProperties.getBucket());
                BucketExistsArgs existsArgs = BucketExistsArgs.builder().bucket(this.minioConfigurationProperties.getBucket()).build();
                boolean b = minioClient.bucketExists(existsArgs);
                if (!b) {
                    if (!this.minioConfigurationProperties.isCreateBucket()) {
                        throw new IllegalStateException("Bucket does not exist: " + this.minioConfigurationProperties.getBucket());
                    }

                    try {
                        MakeBucketArgs makeBucketArgs = MakeBucketArgs.builder().bucket(this.minioConfigurationProperties.getBucket()).build();
                        minioClient.makeBucket(makeBucketArgs);
                    } catch (Exception var5) {
                        throw new MinioException("Cannot create bucket", var5);
                    }
                }
            } catch (Exception var6) {
                LOGGER.error("Error while checking bucket", var6);
                throw var6;
            }
        }

        return minioClient;
    }

    private boolean configuredProxy() {
        String httpHost = System.getProperty("http.proxyHost");
        String httpPort = System.getProperty("http.proxyPort");
        return httpHost != null && httpPort != null;
    }

    private OkHttpClient client() {
        String httpHost = System.getProperty("http.proxyHost");
        String httpPort = System.getProperty("http.proxyPort");
        okhttp3.OkHttpClient.Builder builder = new okhttp3.OkHttpClient.Builder();
        if (httpHost != null) {
            builder.proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(httpHost, Integer.parseInt(httpPort))));
        }

        return builder.build();
    }
}

