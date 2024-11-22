# AWS-Lambda-CloudWatch-Rule-S3


## Description
This project implements a serverless system on AWS for generating and storing UUIDs (Universally Unique Identifiers). It leverages AWS Lambda, CloudWatch, S3, DynamoDB, and IAM to create an automated workflow that generates UUIDs on a regular schedule and stores them in both a database and an S3 bucket.

## Architecture
1. **AWS Lambda**: The `uuid_generator` function is triggered periodically to generate UUIDs.
2. **CloudWatch Events**: A cron-like rule triggers the Lambda function every minute.
3. **S3 Bucket**: The generated UUIDs are stored in a private S3 bucket.
4. **DynamoDB**: UUIDs are also saved in a DynamoDB table for additional persistence.
5. **IAM Role**: Configured to grant the Lambda function access to CloudWatch Logs, S3, DynamoDB, and other required services.

## AWS Resources Used
### Lambda
- The `uuid_generator` function generates UUIDs.

### CloudWatch
- The `uuid_trigger` rule triggers the Lambda function using a cron expression.

### S3 Bucket
- `uuid-storage` is a private S3 bucket where the generated UUIDs are stored.

### DynamoDB
- A table that stores the generated UUIDs for persistence and quick access.

### IAM
- The `lambda-basic-execution` policy defines permissions for the Lambda function.
- The Lambda function policy allows invocation from CloudWatch Events.


