package com.uiintl.backup.agent;

/**
 * Created by jlin on 2016/2/11.
 */
public class BackupResponse {

    private final BackupState backupState;

    private final String message;

    private int totalFiles;

    private int uploadedFiles;

    private BackupResponse(BackupState backupState, String message) {
        this.backupState = backupState;
        this.message = message;
    }

    public BackupState getBackupState() {
        return backupState;
    }

    public String getMessage() {
        return message;
    }

    public int getTotalFiles() {
        return totalFiles;
    }

    public void setTotalFiles(int totalFiles) {
        this.totalFiles = totalFiles;
    }

    public int getUploadedFiles() {
        return uploadedFiles;
    }

    public void setUploadedFiles(int uploadedFiles) {
        this.uploadedFiles = uploadedFiles;
    }

    public static class BackupResponseBuilder {

        private BackupState backupState;

        private String message;

        private int totalFiles;

        private int uploadedFiles;

        private BackupResponseBuilder() {
        }

        public static BackupResponseBuilder builder() {
            return new BackupResponseBuilder();
        }

        public BackupResponseBuilder setBackupState(BackupState backupState) {
            this.backupState = backupState;
            return this;
        }

        public BackupResponseBuilder setMessage(String message) {
            this.message = message;
            return this;
        }

        public BackupResponseBuilder setTotalFiles(int totalFiles) {
            this.totalFiles = totalFiles;
            return this;
        }

        public BackupResponseBuilder setUploadedFiles(int uploadedFiles) {
            this.uploadedFiles = uploadedFiles;
            return this;
        }

        public BackupResponse build() {
            BackupResponse backupResponse = new BackupResponse(this.backupState, this.message);
            backupResponse.setTotalFiles(this.totalFiles);
            backupResponse.setUploadedFiles(this.uploadedFiles);

            return backupResponse;
        }
    }
}
