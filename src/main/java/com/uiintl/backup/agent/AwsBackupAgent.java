/*
 * Copyright 2010-2013 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.uiintl.backup.agent;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 * AwsBackupAgent uses AWS Sdk for Java to connect to Amazon S3 service for backing up
 * myob files.
 */
@Component
public class AwsBackupAgent {

    private static final Logger logger = LoggerFactory.getLogger(AwsBackupAgent.class);

    public static final int EXTENDED_SO_TIMEOUT = 25 * 60 * 1000;

    private AmazonS3 s3;

    public AwsBackupAgent() throws IOException {

        this.initS3Client();
    }

    void initS3Client() {

        ClientConfiguration configuration = new ClientConfiguration();
        configuration.setSocketTimeout(EXTENDED_SO_TIMEOUT);

        s3 = new AmazonS3Client(new ClasspathPropertiesFileCredentialsProvider(), configuration);
        Region usWest2 = Region.getRegion(Regions.AP_SOUTHEAST_2);
        s3.setRegion(usWest2);
    }

    File[] readFiles(final String filePath) {

        logger.info("Reading files from file path:: {}", filePath);

        if (StringUtils.isNotBlank(filePath)) {
            File f = new File(filePath);

            if (f.exists() && f.isDirectory()) {

                return f.listFiles();
            }
        }

        logger.info("No file was found in file path: {}", filePath);
        return null;
    }

    public BackupState uploadFiles(final String backupPath, final String bucketName) {

        File[] files = this.readFiles(backupPath);
        BackupState backupState = BackupState.NO_FILE;

        if (files != null && files.length > 0) {
            logger.info("Detected {} files, begin uploading to S3.", files.length);

            long start = System.currentTimeMillis();
            logger.info("Started at: {}", new Date());
            int failCount = 0;

            for (File file : files) {
                try {
                    long fileStart = System.currentTimeMillis();
                    logger.info("Uploading: {}", file.getAbsolutePath());
                    s3.putObject(new PutObjectRequest(bucketName, file.getName(), file));
                    logger.info("Successful. Took {} seconds", (System.currentTimeMillis() - fileStart) / 1000);

                } catch (AmazonClientException e) {
                    logger.error("Error occurred: {}", e.getMessage(), e);
                    handleAwsException(e);
                    failCount++;
                }
            }

            logger.info("Finished at: {}", new Date());
            logger.info("Total time spent in milliseconds: {}", (System.currentTimeMillis() - start));

            if (failCount == 0) {
                backupState = BackupState.SUCCESS;
            } else if (failCount > 0 && failCount < files.length) {
                backupState = BackupState.PARTIAL_FAIL;
            } else {
                backupState = BackupState.FAIL;
            }
        }

        return backupState;
    }

    void handleAwsException(AmazonClientException ace) {

        if (ace instanceof AmazonServiceException) {
            AmazonServiceException ase = (AmazonServiceException) ace;

            logger.error("Caught an AmazonServiceException, which means your request made it to Amazon S3, but was rejected with an error response for some reason.");
            logger.error("Error Message: {}", ase.getMessage());
            logger.error("HTTP Status Code: {}", ase.getStatusCode());
            logger.error("AWS Error Code: {}", ase.getErrorCode());
            logger.error("Error Type: {}", ase.getErrorType());
            logger.error("Request ID: {}", ase.getRequestId());
            logger.error("Stacktrace: ", ase);
        } else {
            logger.error("Caught an AmazonClientException, which means the client encountered a serious internal problem while trying to communicate with S3, such as not being able to access the network.");
            logger.error("Error Message: {}", ace.getMessage());
        }
    }
}
