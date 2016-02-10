package com.uiintl.backup.web;

import com.uiintl.backup.agent.AwsBackupAgent;
import com.uiintl.backup.agent.BackupState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by jlin on 2016/2/10.
 */
@RestController
@RequestMapping("/backup")
public class BackupController {

    @Autowired
    private AwsBackupAgent awsBackupAgent;

    @Value("${backupPath}")
    private String backupPath;

    @Value("${bucketName}")
    private String bucketName;


    @RequestMapping(method = RequestMethod.GET)
    public BackupResponse backupFiles() {

        long start = System.currentTimeMillis();
        BackupState backupState = this.awsBackupAgent.uploadFiles(this.backupPath, this.bucketName);
        long duration = System.currentTimeMillis() - start;

        return new BackupResponse(backupState, "Duration in milliseconds: " + duration);
    }

    class BackupResponse {

        private final BackupState backupState;

        private final String message;

        public BackupResponse(BackupState backupState, String message) {
            this.backupState = backupState;
            this.message = message;
        }

        public BackupState getBackupState() {
            return backupState;
        }

        public String getMessage() {
            return message;
        }
    }
}