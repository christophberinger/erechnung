package de.cb.erechnung.repository;

import de.cb.erechnung.model.Rechnung;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RechnungRepository extends JpaRepository<Rechnung, Long> {
}