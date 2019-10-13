package com.uiintl.backup.agent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by jlin on 2016/2/11.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BackupResponse {

    private BackupState backupState;

    private int totalFiles;

    private int uploadedFiles;


    public enum BackupState {
        SUCCESS,
        NO_FILE,
        FAIL,
        PARTIAL_FAIL;
    }
}
