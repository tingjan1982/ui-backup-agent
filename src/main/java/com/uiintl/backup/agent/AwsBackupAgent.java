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
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StopWatch;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


/**
 * AwsBackupAgent uses AWS Sdk for Java to connect to Amazon S3 to upload files.
 */
@Component
public class AwsBackupAgent {

    private static final Logger LOGGER = LoggerFactory.getLogger(AwsBackupAgent.class);

    private final AmazonS3 s3;

    private final ResourceLoader resourceLoader;

    @Autowired
    public AwsBackupAgent(final AmazonS3 s3, final ResourceLoader resourceLoader) {
        this.s3 = s3;
        this.resourceLoader = resourceLoader;
    }

    public BackupResponse uploadFiles(final String backupPath, final String bucketName) {

        BackupResponse.BackupState backupState = BackupResponse.BackupState.NO_FILE;
        int uploadFiles = 0;
        final List<PutObjectRequest> putObjectRequests = this.readFiles(backupPath, bucketName);

        if (!CollectionUtils.isEmpty(putObjectRequests)) {
            LOGGER.info("Found {} files, initiate file upload to S3.", putObjectRequests.size());

            final StopWatch overallUpload = new StopWatch();
            overallUpload.start("MYOB File Upload");

            for (PutObjectRequest putObjectRequest : putObjectRequests) {
                try {
                    long fileStart = System.currentTimeMillis();
                    LOGGER.info("Uploading: {}", putObjectRequest.getKey());

                    s3.putObject(putObjectRequest);

                    LOGGER.info("Successful. Took {} milliseconds", (System.currentTimeMillis() - fileStart));
                    uploadFiles++;

                } catch (AmazonClientException e) {
                    LOGGER.error("Error while uploading file {}: {}", putObjectRequest.getKey(), e.getMessage(), e);
                    handleAwsException(e);
                }
            }

            overallUpload.stop();
            LOGGER.info(overallUpload.prettyPrint());

            if (uploadFiles == putObjectRequests.size()) {
                backupState = BackupResponse.BackupState.SUCCESS;

            } else if (uploadFiles > 0) {
                backupState = BackupResponse.BackupState.PARTIAL_FAIL;

            } else {
                backupState = BackupResponse.BackupState.FAIL;
            }
        }

        return new BackupResponse(backupState, putObjectRequests.size(), uploadFiles);
    }

    /**
     * Loading file inside jar:
     * https://stackoverflow.com/questions/14876836/file-inside-jar-is-not-visible-for-spring
     *
     * Subdirectories in AWS S3:
     * https://stackoverflow.com/questions/11491304/amazon-web-services-aws-s3-java-create-a-sub-directory-object
     */
    private List<PutObjectRequest> readFiles(final String backupPath, final String bucketName) {

        try {
            LOGGER.info("Attempt to load resources from specified path: {}", backupPath);

            if (StringUtils.isBlank(backupPath)) {
                throw new RuntimeException("Backup path should not be blank");
            }

            List<PutObjectRequest> objectRequests = new ArrayList<>();

            final Resource resource = resourceLoader.getResource(backupPath);

            if (!resource.isFile()) {
                final InputStream inputStream = resource.getInputStream();
                final byte[] bytes = inputStream.readAllBytes();
                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentLength(bytes.length);
                
                objectRequests.add(new PutObjectRequest(bucketName, backupPath, new ByteArrayInputStream(bytes), metadata));

            } else {
                final File backupFileRoot = resource.getFile();

                if (backupFileRoot.exists()) {
                    addFilesRecursively(objectRequests, bucketName, backupFileRoot.getAbsolutePath(), backupFileRoot);
                }
            }

            return objectRequests;

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private void addFilesRecursively(final List<PutObjectRequest> objectRequests, final String bucketName, final String backupRoot, final File filePath) {

        if (filePath.isDirectory()) {
            final File[] files = filePath.listFiles();

            if (files != null) {
                for (final File file : files) {
                    this.addFilesRecursively(objectRequests, bucketName, backupRoot, file);
                }
            }

        } else {
            final String trimmedFileKey = filePath.getAbsolutePath().replace(backupRoot, "");
            final String fileKey = StringUtils.isNotBlank(trimmedFileKey) ? trimmedFileKey.substring(1) : filePath.getName();
            
            objectRequests.add(new PutObjectRequest(bucketName, fileKey, filePath));
        }
    }

    private void handleAwsException(AmazonClientException ace) {

        if (ace instanceof AmazonServiceException) {
            AmazonServiceException ase = (AmazonServiceException) ace;

            LOGGER.error("Caught an AmazonServiceException, which means your request made it to Amazon S3, but was rejected with an error response for some reason.");
            LOGGER.error("Error Message: {}", ase.getMessage());
            LOGGER.error("HTTP Status Code: {}", ase.getStatusCode());
            LOGGER.error("AWS Error Code: {}", ase.getErrorCode());
            LOGGER.error("Error Type: {}", ase.getErrorType());
            LOGGER.error("Request ID: {}", ase.getRequestId());
            LOGGER.error("Stacktrace: ", ase);
        } else {
            LOGGER.error("Caught an AmazonClientException, which means the client encountered a serious internal problem while trying to communicate with S3, such as not being able to access the network.");
            LOGGER.error("Error Message: {}", ace.getMessage());
        }
    }
}
