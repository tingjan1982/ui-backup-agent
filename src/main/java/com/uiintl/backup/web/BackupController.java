package com.uiintl.backup.web;

import com.uiintl.backup.agent.AwsBackupAgent;
import com.uiintl.backup.agent.BackupResponse;
import com.uiintl.backup.config.BackupProperties;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private BackupProperties backupProperties;

    @RequestMapping(method = RequestMethod.GET)
    public BackupResponse onDemandBackupFiles() {

        return this.awsBackupAgent.uploadFiles(this.backupProperties.getBackupPath(), this.backupProperties.getBucketName());
    }

}
