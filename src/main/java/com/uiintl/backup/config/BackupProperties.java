package com.uiintl.backup.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Created by jlin on 2016/2/10.
 */
@Component
@ConfigurationProperties(prefix = "backup")
@Data
public class BackupProperties {

    private String credentialFilePath;

    private String bucketName;

    private String backupPath;
}
