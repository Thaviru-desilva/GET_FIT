package com.gym.management.repository;

import com.gym.management.model.GymResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GymResourceRepository extends JpaRepository<GymResource, Long> {
    List<GymResource> findByCategoryOrderByAvailableQuantityDesc(String category);
    List<GymResource> findByStatusOrderByCategoryAsc(String status);
    List<GymResource> findByAvailableQuantityGreaterThan(Integer minAvailable);
}
