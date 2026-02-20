package no.pgr301.quizapp.controller;

import no.pgr301.quizapp.model.QuizRequest;
import no.pgr301.quizapp.model.QuizResponse;
import no.pgr301.quizapp.service.QuizService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/quiz")
public class QuizController {

    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @PostMapping("/generate")
    public ResponseEntity<QuizResponse> generateQuiz(@RequestBody QuizRequest request) {
        // Validate input
        if (request.getTopic() == null || request.getTopic().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        if (request.getCount() <= 0 || request.getCount() > 20) {
            return ResponseEntity.badRequest().build();
        }
        if (request.getDifficulty() == null || request.getDifficulty().isEmpty()) {
            request.setDifficulty("medium"); // default
        }

        QuizResponse response = quizService.generateQuiz(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("DevOps Quiz Generator is running!");
    }
}
