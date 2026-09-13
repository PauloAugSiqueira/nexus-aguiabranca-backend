package br.com.fiap.gabinova.backend.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "score_history")
public class ScoreHistoryEntry {

    @Id
    private String id;

    @Indexed
    private String userId;

    private int points;
    private String description;
    private ScoreEventType eventType;
    private Instant date;

    public ScoreHistoryEntry() {
    }

    public ScoreHistoryEntry(String userId, int points, String description, ScoreEventType eventType) {
        this.userId = userId;
        this.points = points;
        this.description = description;
        this.eventType = eventType;
        this.date = Instant.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ScoreEventType getEventType() {
        return eventType;
    }

    public void setEventType(ScoreEventType eventType) {
        this.eventType = eventType;
    }

    public Instant getDate() {
        return date;
    }

    public void setDate(Instant date) {
        this.date = date;
    }
}
