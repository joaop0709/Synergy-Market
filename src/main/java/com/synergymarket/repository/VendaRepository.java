package com.synergymarket.repository;

import com.synergymarket.entity.Venda;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VendaRepository extends JpaRepository<Venda, Long> {
    List<Venda> findByClienteId(Long clienteId);
    boolean existsByClienteId(Long clienteId);
}
