package com.example.backend.service.implementation;

import com.example.backend.model.entity.PatientsCluster;
import com.example.backend.model.entity.table.Patient;
import com.example.backend.model.exception.ObjectNotFound;
import com.example.backend.model.repo.BloodPressureRepo;
import com.example.backend.model.repo.PatientRepo;
import com.example.backend.service.BloodPressureService;
import com.example.backend.service.ClusteringService;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import smile.clustering.HierarchicalClustering;
import smile.clustering.linkage.CompleteLinkage;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Log4j2
public class ClusteringServiceImpl implements ClusteringService {
    private final BloodPressureRepo bloodPressureRepo;
    private final PatientRepo patientRepo;

    private final BloodPressureService bloodPressureService;
    public ClusteringServiceImpl(BloodPressureRepo bloodPressureRepo, PatientRepo patientRepo, BloodPressureService bloodPressureService) {
        this.bloodPressureRepo = bloodPressureRepo;
        this.patientRepo = patientRepo;
        this.bloodPressureService = bloodPressureService;
    }
    @Override
    public List<PatientsCluster> clusterPatients() {
            List<Patient> patients = patientRepo.findAll();

            /* splitting the patients who gave consent for sharing their personal data with other patients
                in 2 categories based on their bp-records-history existence */
            List<Patient> patientsWithRecords = patients.stream().filter(p -> p.getBloodPressures().size() != 0 && p.getAcceptedSharingData()).collect(Collectors.toList());
            List<Patient> patientsWithoutRecords = patients.stream().filter(p -> p.getBloodPressures().size() == 0 && p.getAcceptedSharingData()).collect(Collectors.toList());

            /*log.info("Number of patients with records: " + patientsWithRecords.size());
            log.info("Number of patients without records: " + patientsWithoutRecords.size());*/

            double[][] data = patientsWithRecords.stream()
                    .map(p -> p.getBloodPressures().stream()
                            .mapToDouble(bp -> bp.getSystolic())
                            .toArray())
                    .toArray(double[][]::new);

            /*log.info("Data length: " + data.length);
            if (data.length > 0) {
                log.info("First patient blood pressure data: " + Arrays.toString(data[0]));
            }*/

            List<PatientsCluster> clusters = new ArrayList<>();

            int clustersNumber = 3;

            if (clustersNumber > patientsWithRecords.size()) {
                clustersNumber = patientsWithRecords.size();
            }

            // calculez matricea de distante, nu am reusit sa folosesc DTW
            if (data.length > 1) {
                double[][] distanceMatrix = new double[data.length][data.length];
                for (int i = 0; i < data.length; i++) {
                    for (int j = 0; j < data.length; j++) {
                        if (i != j) {
                            double distance = distance(data[i], data[j]);
                            distanceMatrix[i][j] = distance;
                            distanceMatrix[j][i] = distance;
                        }
                    }
                }

                /*log.info("Distance matrix calculated successfully.");
                log.info("Distance matrix:");*/
                for (int i = 0; i < distanceMatrix.length; i++) {
                    log.info(Arrays.toString(distanceMatrix[i]));
                }

                CompleteLinkage linkage = new CompleteLinkage(distanceMatrix);
                HierarchicalClustering clustering = HierarchicalClustering.fit(linkage);
                // log.info("Clustering: " + clustering.getTree().length + ", " + clustering.getHeight().length);

                int[] labels = clustering.partition(clustersNumber);
                // log.info("Cluster labels: " + Arrays.toString(labels));

                Map<Integer, List<Patient>> clustersMap = new HashMap<>();
                for (int i = 0; i < labels.length; i++) {
                    clustersMap.computeIfAbsent(labels[i], k -> new ArrayList<>()).add(patientsWithRecords.get(i));
                }


                for (Map.Entry<Integer, List<Patient>> entry : clustersMap.entrySet()) {
                    // log.info("Cluster " + entry.getKey() + ": " + entry.getValue().stream().map(Patient::getEmail).collect(Collectors.toList()));
                    clusters.add(new PatientsCluster(
                                    entry.getKey(),
                                    entry.getValue().stream()
                                            .map(Patient::getEmail)
                                            .collect(Collectors.toList()),
                                    entry.getValue().stream()
                                            .map(p -> bloodPressureService.getPatientBloodPressures(p.getEmail()))
                                            .collect(Collectors.toList())));
                }
            }


            if (!patientsWithoutRecords.isEmpty()) {
                clusters.add(new PatientsCluster(
                        -1,
                        patientsWithoutRecords.stream()
                                .map(Patient::getEmail)
                                .collect(Collectors.toList()),
                        patientsWithoutRecords.stream()
                                .map(p -> bloodPressureService.getPatientBloodPressures(p.getEmail()))
                                .collect(Collectors.toList())));
            }

            return clusters;
        }

    @Override
    public PatientsCluster getPatientCluster(String email) {
        if(! patientRepo.findByEmail(email).isPresent()) {
            throw new ObjectNotFound("Patient not found");
        }

        List<PatientsCluster> allClusters = clusterPatients();
        PatientsCluster result = allClusters.stream().filter(c -> c.getPatients().contains(email)).findAny().get();
        return result;
    }

    public double distance(double[] s, double[] t) {
        int n = s.length;
        int m = t.length;
        double[][] dtw = new double[n + 1][m + 1];

        for (int i = 0; i <= n; i++) {
            for (int j = 0; j <= m; j++) {
                dtw[i][j] = Double.POSITIVE_INFINITY;
            }
        }
        dtw[0][0] = 0;

        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                double cost = Math.abs(s[i - 1] - t[j - 1]);
                dtw[i][j] = cost + Math.min(Math.min(dtw[i - 1][j], dtw[i][j - 1]), dtw[i - 1][j - 1]);
            }
        }

        return dtw[n][m];
    }
}
