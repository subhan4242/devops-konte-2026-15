package no.pgr301.quizapp.model;

public class QuizRequest {
    private String topic;
    private int count;
    private String difficulty;

    public QuizRequest() {
    }

    public QuizRequest(String topic, int count, String difficulty) {
        this.topic = topic;
        this.count = count;
        this.difficulty = difficulty;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }
}
