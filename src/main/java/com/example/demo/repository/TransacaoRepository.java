package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.Transacao;

public interface TransacaoRepository extends JpaRepository<Transacao, Long> {

    List<Transacao> findByIdCliente(Long idCliente);
}
