package com.uiintl.backup.agent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by jlin on 2016/2/11.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BackupResponse {

    private String id;

    private Date triggeredDate;

    private BackupState backupState;

    private int totalFiles;

    private AtomicInteger uploadedFiles;


    public enum BackupState {
        STARTED,
        SUCCESS,
        NO_FILE,
        FAIL,
        PARTIAL_FAIL;
    }
}
