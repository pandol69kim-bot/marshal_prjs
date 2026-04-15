package com.example.app.domain.resource.repository;

import com.example.app.domain.resource.entity.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ResourceRepository extends JpaRepository<Resource, Long> {

    @Query("""
            SELECT r FROM Resource r
            WHERE r.status != 'DELETED'
            AND (:status IS NULL OR r.status = :status)
            AND (:keyword IS NULL OR r.name LIKE %:keyword%)
            """)
    Page<Resource> findAllWithFilter(
            @Param("status") Resource.Status status,
            @Param("keyword") String keyword,
            Pageable pageable);
}
