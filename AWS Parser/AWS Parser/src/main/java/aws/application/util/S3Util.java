/* Copyright (c) 2023 Automation Anywhere. All rights reserved.
 *
 * This software is the proprietary information of Automation Anywhere. You shall use it only in
 * accordance with the terms of the license agreement you entered into with Automation Anywhere.
 */
package aws.application.util;

import java.io.File;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

public class S3Util {

    /** upload file to s3 so that textract can process multi page files */
    public static void uploadFile(
            String region, String bucketName, AwsCredentials credentials, String inputFilePath) {

        File file = new File(inputFilePath);
        S3Client s3client =
                S3Client.builder()
                        .credentialsProvider(StaticCredentialsProvider.create(credentials))
                        .region(Region.of(region))
                        .build();

        PutObjectRequest objectRequest =
                PutObjectRequest.builder().bucket(bucketName).key(file.getName()).build();

        s3client.putObject(objectRequest, RequestBody.fromFile(file));
    }

    /** delete file from S3 */
    public static void deleteFile(
            String region, String bucketName, AwsCredentials credentials, String inputFileName) {
        S3Client s3client =
                S3Client.builder()
                        .credentialsProvider(StaticCredentialsProvider.create(credentials))
                        .region(Region.of(region))
                        .build();
        DeleteObjectRequest deleteObjectRequest =
                DeleteObjectRequest.builder().bucket(bucketName).key(inputFileName).build();
        s3client.deleteObject(deleteObjectRequest);
    }
}
