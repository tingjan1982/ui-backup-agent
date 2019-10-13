package com.uiintl.backup.web;

import com.uiintl.backup.agent.AwsBackupAgent;
import com.uiintl.backup.agent.BackupResponse;
import com.uiintl.backup.config.BackupProperties;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by jlin on 2016/2/10.
 */
@RestController
@RequestMapping("/backup")
public class BackupController {

    private final AwsBackupAgent awsBackupAgent;

    private final BackupProperties backupProperties;

    @Autowired
    public BackupController(final AwsBackupAgent awsBackupAgent, final BackupProperties backupProperties) {
        this.awsBackupAgent = awsBackupAgent;
        this.backupProperties = backupProperties;
    }

    @GetMapping
    public BackupResponse onDemandBackupFiles(@RequestParam(value = "path", required = false) String filePath) {

        String backupPath = StringUtils.isNotBlank(filePath) ? filePath : backupProperties.getBackupPath();

        return this.awsBackupAgent.uploadFiles(backupPath, backupProperties.getBucketName());
    }

}
