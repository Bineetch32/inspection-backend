package com.inspection.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.inspection.backend.model.PartMaster;


public interface PartMasterRepository
        extends JpaRepository<PartMaster, Long> {

    Optional<PartMaster> findByPartNoIgnoreCase(String partNo);
}