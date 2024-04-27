package com.example.backend.model.repo;

import com.example.backend.model.entity.table.Recommandation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RecommandationRepo extends JpaRepository<Recommandation, Long> {
    @Query("SELECT r FROM Recommandation r JOIN r.doctor d WHERE d.email = :doctorEmail AND r.hashtag = :hashtag")
    Page<Recommandation> findByHashtag(@Param("doctorEmail") String doctorEmail,
                                       @Param("hashtag") String hashtag,
                                       Pageable pageable);

    @Query("SELECT r FROM Recommandation r JOIN r.doctor d WHERE d.email = :doctorEmail AND r.recommandationType = :recommandationType")
    Page<Recommandation> findByType(@Param("doctorEmail") String doctorEmail,
                                    @Param("recommandationType") String recommandationType,
                                    Pageable pageable);

    @Query("SELECT r FROM Recommandation r JOIN r.doctor d WHERE d.email = :doctorEmail AND r.hashtag = :hashtag AND r.recommandationType = :recommandationType")
    Page<Recommandation> findByHashtagAndType(@Param("doctorEmail") String doctorEmail,
                                              @Param("hashtag") String hashtag,
                                              @Param("recommandationType") String recommandationType,
                                              Pageable pageable);
    @Query("SELECT r FROM Recommandation r JOIN r.doctor d WHERE d.email = :doctorEmail")
    Page<Recommandation> findAll(@Param("doctorEmail") String doctorEmail, Pageable pageable);

    @Query("SELECT r FROM Recommandation r JOIN r.doctor d WHERE d.email = :doctorEmail AND (r.recommandationType = :patientType OR r.recommandationType = 'All')")
    Page<Recommandation> findAllForPatient(@Param("doctorEmail") String doctorEmail,
                                            @Param("patientType") String patientType,
                                            Pageable pageable);

    @Query("SELECT r FROM Recommandation r JOIN r.doctor d WHERE d.email = :doctorEmail AND r.hashtag = :hashtag AND (r.recommandationType = :patientType OR r.recommandationType = 'All')")
    Page<Recommandation> findByTagForPatient(@Param("doctorEmail") String doctorEmail,
                                             @Param("hashtag") String hashtag,
                                             @Param("patientType") String patientType,
                                             Pageable pageable);
}
