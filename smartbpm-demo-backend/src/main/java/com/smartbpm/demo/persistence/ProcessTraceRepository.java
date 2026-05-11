package com.smartbpm.demo.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessTraceRepository extends JpaRepository<ProcessTraceEntity, String> {
}
