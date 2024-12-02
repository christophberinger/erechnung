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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRechnungsNummer() {
        return rechnungsNummer;
    }

    public void setRechnungsNummer(String rechnungsNummer) {
        this.rechnungsNummer = rechnungsNummer;
    }

    public LocalDate getDatum() {
        return datum;
    }

    public void setDatum(LocalDate datum) {
        this.datum = datum;
    }

    public BigDecimal getBetrag() {
        return betrag;
    }

    public void setBetrag(BigDecimal betrag) {
        this.betrag = betrag;
    }
}
