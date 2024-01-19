package com.example.backend.model.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import java.util.Optional;

@NoRepositoryBean
public interface UserRepo<T> extends JpaRepository<T, Long> {
    Optional<T> findByEmail(String email);
}
