package com.xavier.usuario.infrastructure.repository;

import com.xavier.usuario.infrastructure.entity.Telefone;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TelefoneRepository extends JpaRepository<Telefone, Long> {
}
