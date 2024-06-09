package com.example.backend.service;

import com.example.backend.model.entity.PatientsCluster;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ClusteringService {
    List<PatientsCluster> clusterPatients();
    PatientsCluster getPatientCluster(String email);
}
