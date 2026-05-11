package com.smartbpm.demo.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProcessEventRepository extends JpaRepository<ProcessEventEntity, UUID> {
    List<ProcessEventEntity> findByProcessIdOrderByCreatedAtAsc(String processId);
}
