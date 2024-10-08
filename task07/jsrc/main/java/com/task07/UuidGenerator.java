package com.task07;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.syndicate.deployment.annotations.events.RuleEventSource;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.RetentionSetting;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@LambdaHandler(
    lambdaName = "uuid_generator",
    roleName = "uuid_generator-role",
    isPublishVersion = false,
    logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)

@RuleEventSource(targetRule = "uuid_trigger")
@EnvironmentVariables(value = {
    @EnvironmentVariable(key = "BUCKET_NAME", value = "uuid-storage")
})
public class UuidGenerator implements RequestHandler<Object, Map<String, Object>> {

  
	private final AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneOffset.UTC);

    @Override
    public Map<String, Object> handleRequest(Object request, Context context) {
        String bucketName = System.getenv("BUCKET_NAME");
        context.getLogger().log("Bucket Name: " + bucketName);

        List<String> uuidList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            uuidList.add(UUID.randomUUID().toString());
        }

        // Ensure filename matches the required timestamp format without extension
        String fileName = formatter.format(Instant.now());
        String fileContent;
        try {
            fileContent = objectMapper.writeValueAsString(Map.of("ids", uuidList));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert UUID list to JSON", e);
        }

        context.getLogger().log("File Content: " + fileContent);

        ByteArrayInputStream inputStream = new ByteArrayInputStream(fileContent.getBytes(StandardCharsets.UTF_8));
        s3Client.putObject(new PutObjectRequest(bucketName, fileName, inputStream, null));
        context.getLogger().log("File uploaded: " + fileName);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("statusCode", 200);
        resultMap.put("body", "File " + fileName + " uploaded successfully.");

        return resultMap;
    }
}