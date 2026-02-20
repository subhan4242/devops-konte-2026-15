package no.pgr301.quizapp.model;

import java.time.LocalDateTime;
import java.util.List;

public class QuizResponse {
    private String quizId;
    private String topic;
    private String difficulty;
    private int questionCount;
    private LocalDateTime generatedAt;
    private List<Question> questions;

    public QuizResponse() {
    }

    public QuizResponse(String quizId, String topic, String difficulty, int questionCount,
                       LocalDateTime generatedAt, List<Question> questions) {
        this.quizId = quizId;
        this.topic = topic;
        this.difficulty = difficulty;
        this.questionCount = questionCount;
        this.generatedAt = generatedAt;
        this.questions = questions;
    }

    public String getQuizId() {
        return quizId;
    }

    public void setQuizId(String quizId) {
        this.quizId = quizId;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public int getQuestionCount() {
        return questionCount;
    }

    public void setQuestionCount(int questionCount) {
        this.questionCount = questionCount;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }
}
