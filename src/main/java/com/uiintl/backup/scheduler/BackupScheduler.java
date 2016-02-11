package com.uiintl.backup.scheduler;

import com.google.gson.Gson;
import com.uiintl.backup.agent.AwsBackupAgent;
import com.uiintl.backup.agent.BackupResponse;
import com.uiintl.backup.config.BackupProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Created by jlin on 2016/2/10.
 */
@Component
public class BackupScheduler {

    private static final Logger logger = LoggerFactory.getLogger(BackupScheduler.class);

    @Autowired
    private AwsBackupAgent awsBackupAgent;

    @Autowired
    private BackupProperties backupProperties;

    @Scheduled(cron = "${scheduler.backup.cron}")
    public void scheduleBackup() {
        logger.info("Backup triggered by scheduler.");
        BackupResponse backupResponse = this.awsBackupAgent.uploadFiles(this.backupProperties.getBackupPath(), this.backupProperties.getBucketName());

        Gson gson = new Gson();
        String responseJson = gson.toJson(backupResponse);

        logger.info(responseJson);
    }
}
