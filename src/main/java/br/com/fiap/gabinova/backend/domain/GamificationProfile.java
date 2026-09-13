package br.com.fiap.gabinova.backend.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "gamification_profiles")
public class GamificationProfile {

    /** _id = userId (chave natural, evita join extra). */
    @Id
    private String userId;

    private int points;
    private int level;
    private String levelName;
    private List<Badge> badges = new ArrayList<>();

    public GamificationProfile() {
    }

    public GamificationProfile(String userId) {
        this.userId = userId;
        this.points = 0;
        this.level = 1;
        this.levelName = "Explorador Nexus";
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

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getLevelName() {
        return levelName;
    }

    public void setLevelName(String levelName) {
        this.levelName = levelName;
    }

    public List<Badge> getBadges() {
        return badges;
    }

    public void setBadges(List<Badge> badges) {
        this.badges = badges;
    }
}
