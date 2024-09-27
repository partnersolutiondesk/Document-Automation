/* Copyright (c) 2023 Automation Anywhere. All rights reserved.
 *
 * This software is the proprietary information of Automation Anywhere. You shall use it only in
 * accordance with the terms of the license agreement you entered into with Automation Anywhere.
 */
package aws.application.service;

import static aws.application.util.JsonUtils.fromJson;
import static java.nio.charset.StandardCharsets.UTF_8;

import aaiextraction.EngineData;
import aws.application.model.Domain;
import aws.application.transformer.DocumentAnalysisResponseDataTransformer;
import aws.application.util.AWSExtractionUtil;
import aws.application.util.S3Util;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.auth.credentials.*;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.textract.TextractClient;
import software.amazon.awssdk.services.textract.model.*;

public class AWSExtractionService {

    private static final Logger LOGGER = LogManager.getLogger();

    private final DocumentAnalysisResponseDataTransformer transformer;

    public AWSExtractionService(DocumentAnalysisResponseDataTransformer transformer) {
        this.transformer = transformer;
    }

    private Domain loadDomain() {

        Domain domain = null;

        try {
            String fileName = "invoice-domain.json";
            ClassLoader classLoader = getClass().getClassLoader();
            URL resource = classLoader.getResource(fileName);

            String domainJson = readTextFile(resource);
            domain = fromJson(domainJson, Domain.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return domain;
    }

    static String readTextFile(URL url) throws IOException {
        URLConnection conn = url.openConnection();
        conn.setUseCaches(false);
        try (InputStream inputStream = conn.getInputStream()) {
            return new BufferedReader(new InputStreamReader(inputStream, UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));
        }
    }

    public String extract(String inputFilePath, String serviceAccount) {

        Map<String, String> credentialsMap = getAWSConfig(serviceAccount);
        String accessKeyId = credentialsMap.get("accessKeyId");
        String secretAccessKey = credentialsMap.get("secretAccessKey");
        String sessionToken = credentialsMap.get("sessionToken");
        String regionValue = credentialsMap.get("region");
        String bucketName = credentialsMap.get("bucket");

        LOGGER.info("DA CMD: serviceAccount : {}", serviceAccount);

        Domain domain = loadDomain();

        // AwsCredentials credentials = AwsBasicCredentials.create(accessKeyId,secretAccessKey);
        AwsCredentials credentials =
                AwsSessionCredentials.create(accessKeyId, secretAccessKey, sessionToken);

        TextractClient textractClient =
                TextractClient.builder()
                        .region(Region.of(regionValue))
                        .credentialsProvider(StaticCredentialsProvider.create(credentials))
                        .build();

        S3Util.uploadFile(regionValue, bucketName, credentials, inputFilePath);

        String inputFileName =
                inputFilePath.substring(inputFilePath.lastIndexOf(File.separatorChar) + 1);
        // AnalyzeDocumentResponse analyzeDocument = analyzeDoc(textractClient,inputImageFilePath);
        String jobId = startDocAnalysisS3(textractClient, bucketName, inputFileName);
        GetDocumentAnalysisResponse response = getJobResults(textractClient, jobId);
        S3Util.deleteFile(regionValue, bucketName, credentials, inputFileName);
        EngineData engineData = transformer.transform(response, inputFilePath, domain);

        /*DetectDocumentTextResponse detectDocumentTextResponse = detectDocText(textractClient,inputImageFilePath);
        EngineData engineData = transformer.transform(detectDocumentTextResponse);*/

        LOGGER.info("Extraction with AWS IDP is successful. Engine data generated successfully.");
        return AWSExtractionUtil.toJson(engineData);
    }

    private Map<String, String> getAWSConfig(String credentialsJSON) {
        Map<String, String> credentialsMap = fromJson(credentialsJSON, Map.class);
        return credentialsMap;
    }

    public AnalyzeDocumentResponse analyzeDoc(TextractClient textractClient, String sourceDoc) {

        try {
            InputStream sourceStream = new FileInputStream(new File(sourceDoc));
            SdkBytes sourceBytes = SdkBytes.fromInputStream(sourceStream);

            // Get the input Document object as bytes
            Document myDoc = Document.builder().bytes(sourceBytes).build();

            List<FeatureType> featureTypes = new ArrayList<FeatureType>();
            featureTypes.add(FeatureType.FORMS);
            featureTypes.add(FeatureType.TABLES);

            AnalyzeDocumentRequest analyzeDocumentRequest =
                    AnalyzeDocumentRequest.builder()
                            .featureTypes(featureTypes)
                            .document(myDoc)
                            .build();

            AnalyzeDocumentResponse analyzeDocument =
                    textractClient.analyzeDocument(analyzeDocumentRequest);
            return analyzeDocument;

        } catch (TextractException | FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static String startDocAnalysisS3(
            TextractClient textractClient, String bucketName, String docName) {

        try {
            List<FeatureType> myList = new ArrayList<>();
            myList.add(FeatureType.TABLES);
            myList.add(FeatureType.FORMS);
            myList.add(FeatureType.SIGNATURES);

            S3Object s3Object = S3Object.builder().bucket(bucketName).name(docName).build();

            DocumentLocation location = DocumentLocation.builder().s3Object(s3Object).build();

            StartDocumentAnalysisRequest documentAnalysisRequest =
                    StartDocumentAnalysisRequest.builder()
                            .documentLocation(location)
                            .featureTypes(myList)
                            .build();

            StartDocumentAnalysisResponse response =
                    textractClient.startDocumentAnalysis(documentAnalysisRequest);

            // Get the job ID
            String jobId = response.jobId();
            return jobId;

        } catch (TextractException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        return "";
    }

    private static GetDocumentAnalysisResponse getJobResults(
            TextractClient textractClient, String jobId) {

        boolean finished = false;
        int index = 0;
        String status = "";

        try {
            GetDocumentAnalysisResponse response = null;
            while (!finished) {
                GetDocumentAnalysisRequest analysisRequest =
                        GetDocumentAnalysisRequest.builder().jobId(jobId).maxResults(1000).build();

                response = textractClient.getDocumentAnalysis(analysisRequest);
                status = response.jobStatus().toString();

                if (status.compareTo("SUCCEEDED") == 0) finished = true;
                else {
                    System.out.println(index + " status is: " + status);
                    Thread.sleep(1000);
                }
                index++;
            }

            return response;

        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public DetectDocumentTextResponse detectDocText(
            TextractClient textractClient, String sourceDoc) {

        try {
            InputStream sourceStream = new FileInputStream(new File(sourceDoc));
            SdkBytes sourceBytes = SdkBytes.fromInputStream(sourceStream);

            // Get the input Document object as bytes
            Document myDoc = Document.builder().bytes(sourceBytes).build();

            DetectDocumentTextRequest detectDocumentTextRequest =
                    DetectDocumentTextRequest.builder().document(myDoc).build();

            // Invoke the Detect operation
            DetectDocumentTextResponse textResponse =
                    textractClient.detectDocumentText(detectDocumentTextRequest);
            List<Block> docInfo = textResponse.blocks();
            for (Block block : docInfo) {
                System.out.println("The block type is " + block.blockType().toString());
            }

            DocumentMetadata documentMetadata = textResponse.documentMetadata();
            System.out.println(
                    "The number of pages in the document is " + documentMetadata.pages());
            return textResponse;
        } catch (TextractException | FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
