package com.example.backend.controller;

import com.example.backend.model.entity.PatientsCluster;
import com.example.backend.service.ClusteringService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/clusters")
public class ClusterController {
    private final ClusteringService clusteringService;

    public ClusterController(ClusteringService clusteringService) {
        this.clusteringService = clusteringService;
    }

    @GetMapping
    public ResponseEntity<?> getClusters() {
        return ResponseEntity.ok(clusteringService.clusterPatients());
    }

    @GetMapping("/patient")
    public ResponseEntity<PatientsCluster> getPatientCluster(@RequestParam(name = "email") String email) {
        return ResponseEntity.ok(clusteringService.getPatientCluster(email));
    }
}
