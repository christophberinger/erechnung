package de.cb.erechnung.model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
public class Rechnung {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String rechnungsNummer;
    private LocalDate datum;
    private BigDecimal betrag;

    // Getter und Setter hier
}