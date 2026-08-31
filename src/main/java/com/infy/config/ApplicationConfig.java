package com.infy.config;

import org.springframework.ai.bedrock.converse.BedrockChatOptions;
import org.springframework.ai.bedrock.converse.BedrockProxyChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;

/**
 * Multi Chat Client demo - manual multi-model Bedrock configuration.
 *
 * Demo focus:
 * - Expose three ChatClient beans in one Spring Boot app.
 * - Bind each ChatClient to a different Bedrock model ID.
 * - Keep the controller provider-agnostic by depending only on ChatClient.
 *
 * ChatClient roles:
 * - passengerSupportChatClient      : passenger support conversations
 * - flightOperationsChatClient      : disruption and operations guidance
 * - travelPersonalizationChatClient : loyalty and personalization advice
 *
 * Why manual configuration:
 * - Spring AI auto-configuration creates a single ChatModel bean per provider.
 * - This demo needs multiple side-by-side ChatModel beans for routing by use-case.
 */
@Configuration
public class ApplicationConfig {

	@Value("${spring.ai.bedrock.aws.access-key}")
	private String accessKey;

	@Value("${spring.ai.bedrock.aws.secret-key}")
	private String secretKey;

	@Value("${spring.ai.bedrock.aws.region}")
	private String region;

	// Passenger support model ID (configured externally in application.properties)
	@Value("${spring.ai.bedrock.converse.chat.passenger.model}")
	private String passengerModel;

	// Flight operations model ID (configured externally in application.properties)
	@Value("${spring.ai.bedrock.converse.chat.operations.model}")
	private String operationsModel;

	// Personalization model ID (configured externally in application.properties)
	@Value("${spring.ai.bedrock.converse.chat.personalization.model}")
	private String personalizationModel;

	@Value("${spring.ai.bedrock.converse.chat.options.max-tokens}")
	private Integer maxTokens;

	/**
	 * Shared AWS credentials provider used by all ChatModel beans.
	 */
	@Bean
	public StaticCredentialsProvider credentialsProvider() {
		return StaticCredentialsProvider.create(
				AwsBasicCredentials.create(accessKey, secretKey));
	}

	/**
	 * Passenger Support ChatModel.
	 *
	 * Serves conversational passenger-help use cases such as booking, check-in,
	 * baggage, upgrades, and special assistance.
	 */
	@Bean
	public BedrockProxyChatModel passengerSupportChatModel(StaticCredentialsProvider credentialsProvider) {
		return BedrockProxyChatModel.builder()
				.credentialsProvider(credentialsProvider)
				.region(Region.of(region))
				.options(BedrockChatOptions.builder()
						.model(passengerModel)
						.maxTokens(maxTokens)
						.build())
				.build();
	}

	/**
	 * Flight Operations ChatModel.
	 *
	 * Serves latency-sensitive and structured operational guidance such as flight
	 * status, gate changes, cancellations, and rebooking steps.
	 */
	@Bean
	public BedrockProxyChatModel flightOperationsChatModel(StaticCredentialsProvider credentialsProvider) {
		return BedrockProxyChatModel.builder()
				.credentialsProvider(credentialsProvider)
				.region(Region.of(region))
				.options(BedrockChatOptions.builder()
						.model(operationsModel)
						.maxTokens(maxTokens)
						.build())
				.build();
	}

	/**
	 * Travel Personalization ChatModel.
	 *
	 * Serves recommendation-style use cases such as loyalty optimization, lounge
	 * eligibility, perks discovery, and personalized trip guidance.
	 */
	@Bean
	public BedrockProxyChatModel travelPersonalizationChatModel(StaticCredentialsProvider credentialsProvider) {
		return BedrockProxyChatModel.builder()
				.credentialsProvider(credentialsProvider)
				.region(Region.of(region))
				.options(BedrockChatOptions.builder()
						.model(personalizationModel)
						.maxTokens(maxTokens)
						.build())
				.build();
	}

	@Bean
	public ChatClient passengerSupportChatClient(BedrockProxyChatModel passengerSupportChatModel) {
		return ChatClient.builder(passengerSupportChatModel)
				.defaultSystem("""
						You are a friendly and empathetic airline customer service agent for United Airlines.
						Help passengers with booking inquiries, online check-in, seat selection, baggage allowances,
						flight upgrades, special assistance requests, and travel documentation requirements.
						Always prioritize passenger comfort and provide clear, step-by-step guidance.
						Respond in plain text only. Do not use backticks, asterisks, hash headers, or any other markdown symbols.
						""")
				.build();
	}

	@Bean
	public ChatClient flightOperationsChatClient(BedrockProxyChatModel flightOperationsChatModel) {
		return ChatClient.builder(flightOperationsChatModel)
				.defaultSystem("""
						You are an airline operations specialist for United Airlines.
						Provide accurate, structured information about flight status, gate changes, delays, cancellations,
						and irregular operations (IROPS). Guide passengers through rebooking options, compensation policies,
						hotel vouchers, and meal allowances during disruptions.
						Present information as a numbered list of clear, actionable steps.
						Respond in plain text only. Do not use backticks, asterisks, hash headers, or any other markdown symbols.
						""")
				.build();
	}

	@Bean
	public ChatClient travelPersonalizationChatClient(BedrockProxyChatModel travelPersonalizationChatModel) {
		return ChatClient.builder(travelPersonalizationChatModel)
				.defaultSystem("""
						You are an airline concierge and loyalty program specialist for United Airlines.
						Provide personalized travel recommendations based on passenger preferences, MileagePlus loyalty tier,
						travel history, and destination interests. Topics include: lounge access eligibility, upgrade strategies,
						award mile redemption, co-branded credit card benefits, in-flight connectivity, and premium cabin perks.
						Tailor every response to maximize the passenger's travel experience.
						Respond in plain text only. Do not use backticks, asterisks, hash headers, or any other markdown symbols.
						""")
				.build();
	}
}