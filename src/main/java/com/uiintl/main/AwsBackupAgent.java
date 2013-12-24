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
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.util.Arrays;
import java.util.Properties;
import java.util.UUID;

/**
 * This sample demonstrates how to make basic requests to Amazon S3 using
 * the AWS SDK for Java.
 * <p/>
 * <b>Prerequisites:</b> You must have a valid Amazon Web Services developer
 * account, and be signed up to use Amazon S3. For more information on
 * Amazon S3, see http://aws.amazon.com/s3.
 * <p/>
 * <b>Important:</b> Be sure to fill in your AWS access credentials in the
 * AwsCredentials.properties file before you try to run this
 * sample.
 * http://aws.amazon.com/security-credentials
 */
public class AwsBackupAgent {

    public static final String MYOB_PATH = "myobPath";

    public static final String BUCKET_NAME = "bucketName";

    private final AmazonS3 s3;

    private final Properties properties;

    public AwsBackupAgent() throws IOException {

        s3 = new AmazonS3Client(new ClasspathPropertiesFileCredentialsProvider());
        Region usWest2 = Region.getRegion(Regions.AP_SOUTHEAST_2);
        s3.setRegion(usWest2);

        InputStream propertyStream = getClass().getResourceAsStream("/default.properties");

        properties = new Properties();
        properties.load(propertyStream);
    }

    File[] readMyobFiles() {

        String myobPath = properties.getProperty(MYOB_PATH);
        System.out.println("Myob Path: " + myobPath);

        if(StringUtils.isNotBlank(myobPath)) {
            File f = new File(myobPath);

            if(f.exists() && f.isDirectory()) {

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
        String bucketName = properties.getProperty(BUCKET_NAME);

        for(File myobFile : myobFiles) {
            System.out.print("Uploading myob file: " + myobFile.getAbsolutePath() + "- ");
            s3.putObject(new PutObjectRequest(bucketName, myobFile.getName(), myobFile));
            System.out.println("Uploaded");
        }
    }

    public static void main(String[] args) throws IOException {

        System.out.println("===========================================");
        System.out.println("Getting started with Amazon S3");
        System.out.println("===========================================\n");

        try {
            new AwsBackupAgent().uploadMyobFiles();

        } catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException, which means your request made it "
                    + "to Amazon S3, but was rejected with an error response for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with S3, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
        }
    }
}
