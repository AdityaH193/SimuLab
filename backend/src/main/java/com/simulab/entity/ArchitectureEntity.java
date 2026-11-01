package com.simulab.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "architectures")
public class ArchitectureEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
    private boolean isTemplate = false;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String canvasDataJson; // React Flow nodes, edges, positions

    @Lob
    @Column(columnDefinition = "TEXT")
    private String scenarioDataJson; // Simulation specific settings

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public ArchitectureEntity() {}

    public ArchitectureEntity(String name, String description, boolean isTemplate, String canvasDataJson, String scenarioDataJson) {
        this.name = name;
        this.description = description;
        this.isTemplate = isTemplate;
        this.canvasDataJson = canvasDataJson;
        this.scenarioDataJson = scenarioDataJson;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isTemplate() { return isTemplate; }
    public void setTemplate(boolean template) { isTemplate = template; }

    public String getCanvasDataJson() { return canvasDataJson; }
    public void setCanvasDataJson(String canvasDataJson) { this.canvasDataJson = canvasDataJson; }

    public String getScenarioDataJson() { return scenarioDataJson; }
    public void setScenarioDataJson(String scenarioDataJson) { this.scenarioDataJson = scenarioDataJson; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
