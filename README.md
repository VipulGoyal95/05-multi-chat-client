# Spring AI Multi Chat Client Demo

This demo shows how to configure and use **multiple ChatClient beans** in one Spring Boot application.

Instead of a single generic chat endpoint, the app exposes three domain-specific chat paths. Each path uses a dedicated ChatClient, and each ChatClient can point to a different Bedrock model ID.

## What This Demo Demonstrates

- Manual multi-model configuration with Spring AI Bedrock
- Three ChatClient beans for three different business domains
- One controller that routes requests by endpoint/domain
- Reusable AWS credentials and region configuration
- Clean separation of model wiring (`config`) and API routing (`controller`)

## Multi Chat Client Concept

The key idea is **domain-based routing**:

- `/api/chat/passenger` -> `passengerSupportChatClient`
- `/api/chat/operations` -> `flightOperationsChatClient`
- `/api/chat/personalize` -> `travelPersonalizationChatClient`

Each ChatClient has its own default system prompt and model configuration, so behavior is specialized without changing controller logic.

## Project Structure

```text
05-multi-chat-client/
|-- src/main/java/com/infy/
|   |-- config/
|   |   `-- ApplicationConfig.java
|   |-- controller/
|   |   `-- AIController.java
|   `-- DemoApplication.java
|-- src/main/resources/
|   `-- application.properties
`-- pom.xml
```

## Configuration

Set your Bedrock credentials and model IDs in `src/main/resources/application.properties`.

Expected properties used by this demo:

```properties
spring.ai.bedrock.aws.access-key=${AWS_ACCESS_KEY_ID}
spring.ai.bedrock.aws.secret-key=${AWS_SECRET_ACCESS_KEY}
spring.ai.bedrock.aws.region=us-east-1

spring.ai.bedrock.converse.chat.passenger.model=<passenger-model-id>
spring.ai.bedrock.converse.chat.operations.model=<operations-model-id>
spring.ai.bedrock.converse.chat.personalization.model=<personalization-model-id>

spring.ai.bedrock.converse.chat.options.max-tokens=500
```

## Run

```cmd
mvnw clean test
mvnw spring-boot:run
```

## API Endpoints

```text
GET /api/chat/passenger?userPrompt=How do I check in online?
GET /api/chat/operations?userPrompt=My flight is delayed. What are my options?
GET /api/chat/personalize?userPrompt=What perks do I get as a loyalty member?
```

## Why Manual Bean Configuration?

Spring AI auto-configuration typically creates one ChatModel bean per provider. For this demo, we need three model-backed clients in the same app, so beans are created manually in `ApplicationConfig.java`.

## Key Takeaways

- A single app can host multiple AI personas cleanly.
- Endpoint-level routing is a practical pattern for multi-model applications.
- ChatClient abstraction keeps controller code simple and provider-agnostic.