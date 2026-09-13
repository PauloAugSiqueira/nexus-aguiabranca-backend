package br.com.fiap.gabinova.backend.domain;

/** Item embutido em GamificationProfile.badges (nao e uma collection propria). */
public class Badge {

    private String id;
    private String name;
    private boolean earned;
    private String earnedAt; // ISO-8601, nullable

    public Badge() {
    }

    public Badge(String id, String name, boolean earned, String earnedAt) {
        this.id = id;
        this.name = name;
        this.earned = earned;
        this.earnedAt = earnedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEarned() {
        return earned;
    }

    public void setEarned(boolean earned) {
        this.earned = earned;
    }

    public String getEarnedAt() {
        return earnedAt;
    }

    public void setEarnedAt(String earnedAt) {
        this.earnedAt = earnedAt;
    }
}
