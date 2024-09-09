package com.task07;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.RetentionSetting;

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
@EnvironmentVariables(value = {
    @EnvironmentVariable(key = "BUCKET_NAME", value = "uuid-storage")
})
public class UuidGenerator implements RequestHandler<Object, Map<String, Object>> {

    private final AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();
    private final String BUCKET_NAME = "uuid-storage";

    @Override
    public Map<String, Object> handleRequest(Object request, Context context) {
        // Generate 10 random UUIDs
        List<String> uuidList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            uuidList.add(UUID.randomUUID().toString());
        }
        
        // Create a filename with the ISO time of execution
        String fileName = Instant.now().toString() + ".json";
        
        // Create JSON content for the file
        String fileContent = "{\"ids\": " + uuidList.toString() + "}";
        
        // Upload the file to S3
        s3Client.putObject(new PutObjectRequest(BUCKET_NAME, fileName, fileContent));
        
        // Return response
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("statusCode", 200);
        resultMap.put("body", "File " + fileName + " uploaded successfully.");
        
        return resultMap;
    }
}