package com.infy.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Multi Chat Client demo controller.
 *
 * One REST controller routes prompts to three specialized ChatClient beans,
 * showing domain-based model routing inside a single Spring AI application.
 *
 *   GET /api/chat/passenger    -> passengerSupportChatClient
 *   GET /api/chat/operations   -> flightOperationsChatClient
 *   GET /api/chat/personalize  -> travelPersonalizationChatClient
 */
@RestController
@RequestMapping("/api/chat")
@CrossOrigin
public class AIController {

	private final ChatClient passengerSupportChatClient;
	private final ChatClient flightOperationsChatClient;
	private final ChatClient travelPersonalizationChatClient;

	public AIController(ChatClient passengerSupportChatClient,
			ChatClient flightOperationsChatClient,
			ChatClient travelPersonalizationChatClient) {
		this.passengerSupportChatClient = passengerSupportChatClient;
		this.flightOperationsChatClient = flightOperationsChatClient;
		this.travelPersonalizationChatClient = travelPersonalizationChatClient;
	}

	/**
	 * Passenger support endpoint.
	 * Uses the ChatClient configured for conversational passenger-help queries.
	 *
	 * Example: /api/chat/passenger?userPrompt=How do I add an extra bag to my booking online?
	 */
	@GetMapping(value = "/passenger", produces = MediaType.TEXT_PLAIN_VALUE)
	public String handlePassengerQuery(
			@RequestParam(defaultValue = "How can I use the United Airlines app to check in, choose my seat, and track my baggage in real time?") String userPrompt) {
		return passengerSupportChatClient.prompt()
				.user(userPrompt)
				.call()
				.content();
	}

	/**
	 * Flight operations endpoint.
	 * Uses the ChatClient configured for structured disruption and rebooking guidance.
	 *
	 * Example: /api/chat/operations?userPrompt=My flight was cancelled due to weather. What are my options?
	 */
	@GetMapping(value = "/operations", produces = MediaType.TEXT_PLAIN_VALUE)
	public String handleFlightOperationsQuery(
			@RequestParam(defaultValue = "What digital self-service tools does United Airlines provide to help passengers manage flight disruptions and rebooking without waiting in queue?") String userPrompt) {
		return flightOperationsChatClient.prompt()
				.user(userPrompt)
				.call()
				.content();
	}

	/**
	 * Travel personalization endpoint.
	 * Uses the ChatClient configured for loyalty and personalized travel recommendations.
	 *
	 * Example: /api/chat/personalize?userPrompt=I am a MileagePlus Gold member flying to London. What upgrades and lounge benefits am I eligible for?
	 */
	@GetMapping(value = "/personalize", produces = MediaType.TEXT_PLAIN_VALUE)
	public String handleTravelPersonalizationQuery(
			@RequestParam(defaultValue = "As a MileagePlus Premier member, what personalized perks and digital services can I access before, during, and after my flight to enhance my travel experience?") String userPrompt) {
		
				
		ChatResponse response = travelPersonalizationChatClient.prompt(userPrompt)
				.call()
				.chatResponse();

		String content = response.getResult().getOutput().getText();
		Integer promptTokens      = response.getMetadata().getUsage().getPromptTokens();
		Integer completionTokens  = response.getMetadata().getUsage().getCompletionTokens();
		Integer totalTokens       = response.getMetadata().getUsage().getTotalTokens();
		Long    remainingTokens   = response.getMetadata().getRateLimit().getTokensRemaining();
		String  model             = response.getMetadata().getModel();

		return content
				+ "\n\n--- query cost ---"
				+ "\nModel:             " + model
				+ "\nPrompt tokens:     " + promptTokens
				+ "\nCompletion tokens: " + completionTokens
				+ "\nTotal tokens:      " + totalTokens
				+ "\nRemaining tokens:  " + remainingTokens;

	}
}