package com.uiintl.backup.config;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.PropertiesFileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Created by jlin on 2016/2/10.
 */
@Configuration
@EnableScheduling
public class AppConfig {

    private static final int EXTENDED_SO_TIMEOUT = 25 * 60 * 1000;

    private final BackupProperties backupProperties;

    @Autowired
    public AppConfig(final BackupProperties backupProperties) {
        this.backupProperties = backupProperties;
    }

    @Bean
    public AmazonS3 amazonS3() {

        ClientConfiguration configuration = new ClientConfiguration();
        configuration.setSocketTimeout(EXTENDED_SO_TIMEOUT);

        final AmazonS3ClientBuilder clientBuilder = AmazonS3ClientBuilder.standard();
        clientBuilder.setCredentials(new PropertiesFileCredentialsProvider(backupProperties.getCredentialFilePath()));
        clientBuilder.setClientConfiguration(configuration);
        clientBuilder.setRegion(Regions.AP_SOUTHEAST_2.getName());

        return clientBuilder.build();
    }
}
