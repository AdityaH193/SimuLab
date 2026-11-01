package com.simulab.dto;

import java.time.LocalDateTime;

public class ArchitectureDto {
    private Long id;
    private String name;
    private String description;
    private boolean isTemplate;
    private String canvasDataJson;
    private String scenarioDataJson;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ArchitectureDto() {}

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
