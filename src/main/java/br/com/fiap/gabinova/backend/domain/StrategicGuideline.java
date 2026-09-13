package br.com.fiap.gabinova.backend.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "strategic_guidelines")
public class StrategicGuideline {

    @Id
    private String id;

    private String title;
    private String description;
    private String category;

    /** String numerica "1".."5" (1=Critica ... 5=Informativa) - ver SPEC_FUNCIONAL_BACKEND.md, decisao 9.2. */
    private String priority;

    private GuidelineStatus status;

    private Instant createdAt;
    private Instant updatedAt;

    public StrategicGuideline() {
    }

    public StrategicGuideline(String title, String description, String category, String priority) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.priority = priority;
        this.status = GuidelineStatus.ACTIVE;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public GuidelineStatus getStatus() {
        return status;
    }

    public void setStatus(GuidelineStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
