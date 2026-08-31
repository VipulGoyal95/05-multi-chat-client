package com.ai;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrock.BedrockClient;
import software.amazon.awssdk.services.bedrock.model.FoundationModelSummary;
import software.amazon.awssdk.services.bedrock.model.ListFoundationModelsResponse;

import java.util.Comparator;
import java.util.List;

public class ListBedrockModelsTest {

    @Test
    void listAccessibleModels() {
        String accessKey = System.getenv("AWS_ACCESS_KEY");
        String secretKey = System.getenv("AWS_SECRET_KEY");
        String region    = System.getenv("AWS_REGION");

        System.out.println("Region: " + region);

        BedrockClient client = BedrockClient.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .build();

        ListFoundationModelsResponse response = client.listFoundationModels(r -> r.byInferenceType("ON_DEMAND"));

        List<FoundationModelSummary> models = response.modelSummaries().stream()
                .filter(m -> m.modelLifecycle() != null && "ACTIVE".equals(m.modelLifecycle().statusAsString()))
                .sorted(Comparator.comparing(m -> m.providerName()))
                .toList();

        System.out.println("\n========== ACCESSIBLE BEDROCK MODELS (ON_DEMAND, ACTIVE) ==========");
        System.out.printf("%-15s  %s%n", "PROVIDER", "MODEL ID");
        System.out.println("-".repeat(70));
        models.forEach(m -> System.out.printf("%-15s  %s%n", m.providerName(), m.modelId()));
        System.out.println("=".repeat(70));
        System.out.println("Total: " + models.size() + " models");

        client.close();
    }
}
