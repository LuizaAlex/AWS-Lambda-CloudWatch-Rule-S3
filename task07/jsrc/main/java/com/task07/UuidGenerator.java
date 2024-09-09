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

import java.time.Instant;
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

    @Override
    public Map<String, Object> handleRequest(Object request, Context context) {
        String bucketName = System.getenv("BUCKET_NAME");
        context.getLogger().log("Bucket Name: " + bucketName);

        List<String> uuidList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            uuidList.add(UUID.randomUUID().toString());
        }

        String fileName = Instant.now().toString() + ".json";
        String fileContent;
        try {
            fileContent = objectMapper.writeValueAsString(Map.of("ids", uuidList));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert UUID list to JSON", e);
        }

        context.getLogger().log("File Content: " + fileContent);

        s3Client.putObject(new PutObjectRequest(bucketName, fileName, fileContent));
        context.getLogger().log("File uploaded: " + fileName);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("statusCode", 200);
        resultMap.put("body", "File " + fileName + " uploaded successfully.");

        return resultMap;
    }
}