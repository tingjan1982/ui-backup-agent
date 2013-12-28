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
package com.uiintl.main;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

/**
 * AwsBackupAgent uses AWS Sdk for Java to connect to Amazon S3 service for backing up
 * myob files.
 */
public class AwsBackupAgent {

    private static final Logger logger = LoggerFactory.getLogger(AwsBackupAgent.class);

    public static final String DEFAULT_PROPERTY = "uiintl-default.properties";

    public static final String MYOB_PATH = "myobPath";

    public static final String BUCKET_NAME = "bucketName";

    public static final int EXTENDED_SO_TIMEOUT = 25 * 60 * 1000;

    private final AmazonS3 s3;

    private final Properties properties;

    public AwsBackupAgent() throws IOException {

        ClientConfiguration configuration = new ClientConfiguration();
        configuration.setSocketTimeout(EXTENDED_SO_TIMEOUT);

        s3 = new AmazonS3Client(new ClasspathPropertiesFileCredentialsProvider(), configuration);
        Region usWest2 = Region.getRegion(Regions.AP_SOUTHEAST_2);
        s3.setRegion(usWest2);

        properties = new Properties();
        InputStream propertyStream = getClass().getResourceAsStream("/default.properties");
        properties.load(propertyStream);

        File userDefault = new File(System.getProperty("user.home"), DEFAULT_PROPERTY);

        if (userDefault.exists()) {
            InputStream is = FileUtils.openInputStream(userDefault);

            try {
                properties.load(is);
            } finally {
                IOUtils.closeQuietly(is);
            }

        }
    }

    void initCheck() {

        logger.debug("Loaded property values...");

        for (Map.Entry property : properties.entrySet()) {
            logger.debug("{}: {}", property.getKey(), property.getValue());
        }
    }

    File[] readMyobFiles() {

        String myobPath = properties.getProperty(MYOB_PATH);
        logger.info("Myob Path: {}", myobPath);

        if (StringUtils.isNotBlank(myobPath)) {
            File f = new File(myobPath);

            if (f.exists() && f.isDirectory()) {

                File[] files = f.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File file, String name) {
                        return name.endsWith(".MYO") || name.endsWith(".myo");
                    }
                });

                return files;
            }
        }

        return null;
    }

    void uploadMyobFiles() {

        File[] myobFiles = this.readMyobFiles();

        if (myobFiles != null && myobFiles.length > 0) {
            logger.info("Detected {} myob files, begin to upload to S3.", myobFiles.length);
            String bucketName = properties.getProperty(BUCKET_NAME);

            long start = System.currentTimeMillis();
            logger.info("Started at: {}", new Date());

            for (File myobFile : myobFiles) {
                try {
                    long fileStart = System.currentTimeMillis();
                    logger.info("Uploading {}", myobFile.getAbsolutePath());
                    s3.putObject(new PutObjectRequest(bucketName, myobFile.getName(), myobFile));
                    logger.info("--- Successful. Took {} seconds", (System.currentTimeMillis() - fileStart) / 1000);

                } catch (AmazonClientException e) {
                    logger.error("--- Failed");
                    handleAwsException(e);
                }
            }

            logger.info("Finished at: {}", new Date());
            logger.info("Total time spent in seconds: {}", (System.currentTimeMillis() - start) / 1000);
        }
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

    public static void main(String[] args) throws IOException {

        System.out.println("===========================================");
        System.out.println("Getting started with Amazon S3");
        System.out.println("===========================================");
        System.out.println();

        AwsBackupAgent awsBackupAgent = new AwsBackupAgent();
        awsBackupAgent.initCheck();

        try {
            awsBackupAgent.uploadMyobFiles();

        } catch (AmazonClientException ace) {
            awsBackupAgent.handleAwsException(ace);
        }
    }
}
