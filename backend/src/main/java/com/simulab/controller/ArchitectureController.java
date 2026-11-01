package com.simulab.controller;

import com.simulab.dto.ArchitectureDto;
import com.simulab.service.ArchitectureService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/architectures")
public class ArchitectureController {

    private final ArchitectureService architectureService;

    public ArchitectureController(ArchitectureService architectureService) {
        this.architectureService = architectureService;
    }

    @GetMapping
    public ResponseEntity<List<ArchitectureDto>> getAll() {
        return ResponseEntity.ok(architectureService.getAll());
    }

    @GetMapping("/templates")
    public ResponseEntity<List<ArchitectureDto>> getTemplates() {
        return ResponseEntity.ok(architectureService.getTemplates());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArchitectureDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(architectureService.getById(id));
    }

    @PostMapping
    public ResponseEntity<ArchitectureDto> create(@RequestBody ArchitectureDto dto) {
        return ResponseEntity.ok(architectureService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ArchitectureDto> update(@PathVariable Long id, @RequestBody ArchitectureDto dto) {
        return ResponseEntity.ok(architectureService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        architectureService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Deleted architecture successfully"));
    }
}
