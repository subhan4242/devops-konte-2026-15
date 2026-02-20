package no.pgr301.quizapp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.pgr301.quizapp.model.Question;
import no.pgr301.quizapp.model.QuizRequest;
import no.pgr301.quizapp.model.QuizResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class QuizService {

    private final BedrockRuntimeClient bedrockClient;
    private final ObjectMapper objectMapper;

    @Value("${aws.region:eu-west-1}")
    private String awsRegion;

    public QuizService() {
        // Use default credential provider chain and region provider
        this.bedrockClient = BedrockRuntimeClient.builder()
                .region(Region.EU_WEST_1)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public QuizResponse generateQuiz(QuizRequest request) {
        try {
            String prompt = buildPrompt(request);
            String responseText = callBedrock(prompt);
            List<Question> questions = parseQuestions(responseText);

            String quizId = "quiz-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"))
                           + "-" + UUID.randomUUID().toString().substring(0, 8);

            return new QuizResponse(
                    quizId,
                    request.getTopic(),
                    request.getDifficulty(),
                    questions.size(),
                    LocalDateTime.now(),
                    questions
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate quiz: " + e.getMessage(), e);
        }
    }

    private String buildPrompt(QuizRequest request) {
        return String.format("""
            You are a DevOps expert creating quiz questions for students.

            Generate exactly %d multiple-choice questions about: %s
            Difficulty level: %s

            Return ONLY valid JSON in this exact format (no markdown, no extra text):
            [
              {
                "id": 1,
                "question": "Question text here?",
                "options": ["A) Option 1", "B) Option 2", "C) Option 3", "D) Option 4"],
                "correctAnswer": "B",
                "explanation": "Brief explanation of why this answer is correct"
              }
            ]

            Requirements:
            - Questions must be practical and relevant to real DevOps work
            - Each question must have exactly 4 options (A, B, C, D)
            - Provide clear explanations for correct answers
            - Vary the correct answer position (don't always use the same letter)
            """, request.getCount(), request.getTopic(), request.getDifficulty());
    }

    private String callBedrock(String prompt) throws Exception {
        // Using Amazon Nova Pro model via cross-region inference profile
        String modelId = "eu.amazon.nova-pro-v1:0";

        String requestBody = objectMapper.writeValueAsString(java.util.Map.of(
                "messages", java.util.List.of(
                        java.util.Map.of(
                                "role", "user",
                                "content", java.util.List.of(
                                        java.util.Map.of("text", prompt)
                                )
                        )
                ),
                "inferenceConfig", java.util.Map.of(
                        "max_new_tokens", 4096,
                        "temperature", 0.7,
                        "top_p", 0.9
                )
        ));

        InvokeModelRequest invokeRequest = InvokeModelRequest.builder()
                .modelId(modelId)
                .body(SdkBytes.fromUtf8String(requestBody))
                .build();

        InvokeModelResponse response = bedrockClient.invokeModel(invokeRequest);
        String responseBody = response.body().asUtf8String();

        JsonNode jsonResponse = objectMapper.readTree(responseBody);
        return jsonResponse.get("output").get("message").get("content").get(0).get("text").asText();
    }

    private List<Question> parseQuestions(String responseText) throws Exception {
        // Remove markdown code blocks if present
        String cleanedText = responseText.trim();
        if (cleanedText.startsWith("```json")) {
            cleanedText = cleanedText.substring(7);
        }
        if (cleanedText.startsWith("```")) {
            cleanedText = cleanedText.substring(3);
        }
        if (cleanedText.endsWith("```")) {
            cleanedText = cleanedText.substring(0, cleanedText.length() - 3);
        }
        cleanedText = cleanedText.trim();

        JsonNode questionsNode = objectMapper.readTree(cleanedText);
        List<Question> questions = new ArrayList<>();

        if (questionsNode.isArray()) {
            for (JsonNode node : questionsNode) {
                Question question = new Question();
                question.setId(node.get("id").asInt());
                question.setQuestion(node.get("question").asText());

                List<String> options = new ArrayList<>();
                JsonNode optionsNode = node.get("options");
                for (JsonNode option : optionsNode) {
                    options.add(option.asText());
                }
                question.setOptions(options);

                question.setCorrectAnswer(node.get("correctAnswer").asText());
                question.setExplanation(node.get("explanation").asText());

                questions.add(question);
            }
        }

        return questions;
    }
}
