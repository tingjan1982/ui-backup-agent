package com.uiintl.backup.scheduler;

import com.uiintl.backup.agent.AwsBackupAgent;
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

    /**
     * Midnight of every weekday.
     */
    @Scheduled(cron = "${scheduler.backup.cron}")
    public void scheduleBackup() {
        logger.info("Backup triggered by scheduler.");
        this.awsBackupAgent.uploadFiles(this.backupProperties.getBackupPath(), this.backupProperties.getBucketName());
    }
}
