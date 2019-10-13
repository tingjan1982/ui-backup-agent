package com.uiintl.backup.scheduler;

import com.uiintl.backup.agent.AwsBackupAgent;
import com.uiintl.backup.agent.BackupResponse;
import com.uiintl.backup.config.BackupProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Created by jlin on 2016/2/10.
 */
@Component
public class BackupScheduler {

    private static final Logger logger = LoggerFactory.getLogger(BackupScheduler.class);

    private final AwsBackupAgent awsBackupAgent;

    private final BackupProperties backupProperties;

    @Autowired
    public BackupScheduler(final AwsBackupAgent awsBackupAgent, final BackupProperties backupProperties) {
        this.awsBackupAgent = awsBackupAgent;
        this.backupProperties = backupProperties;
    }

    @Scheduled(cron = "${scheduler.backup.cron}")
    public void scheduleBackup() {

        logger.info("Backup triggered at {}", new Date());
        BackupResponse backupResponse = this.awsBackupAgent.uploadFiles(this.backupProperties.getBackupPath(), this.backupProperties.getBucketName());

        logger.info("{}", backupResponse);
    }
}
