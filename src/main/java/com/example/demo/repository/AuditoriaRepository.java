package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.Auditoria;

public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {
}
