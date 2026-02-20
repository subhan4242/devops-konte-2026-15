package no.pgr301.quizapp.controller;

import no.pgr301.quizapp.model.QuizRequest;
import no.pgr301.quizapp.model.QuizResponse;
import no.pgr301.quizapp.service.QuizService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(QuizController.class)
class QuizControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private QuizService quizService;

    @Test
    void healthEndpointShouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/quiz/health"))
                .andExpect(status().isOk());
    }

    @Test
    void generateQuizShouldReturnOkWithValidRequest() throws Exception {
        QuizResponse mockResponse = new QuizResponse(
                "quiz-123",
                "docker",
                "medium",
                3,
                LocalDateTime.now(),
                Collections.emptyList()
        );

        when(quizService.generateQuiz(any(QuizRequest.class))).thenReturn(mockResponse);

        String requestBody = """
                {
                    "topic": "docker",
                    "count": 3,
                    "difficulty": "medium"
                }
                """;

        mockMvc.perform(post("/api/quiz/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());
    }

    @Test
    void generateQuizShouldReturnBadRequestForInvalidCount() throws Exception {
        String requestBody = """
                {
                    "topic": "docker",
                    "count": 0,
                    "difficulty": "medium"
                }
                """;

        mockMvc.perform(post("/api/quiz/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void generateQuizShouldReturnBadRequestForEmptyTopic() throws Exception {
        String requestBody = """
                {
                    "topic": "",
                    "count": 3,
                    "difficulty": "medium"
                }
                """;

        mockMvc.perform(post("/api/quiz/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }
}
