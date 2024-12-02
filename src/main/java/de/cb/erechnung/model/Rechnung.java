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
    private String waehrung = "EUR";
    
    // Lieferant Details
    private String lieferantName;
    private String lieferantStrasse;
    private String lieferantPlz;
    private String lieferantOrt;
    private String lieferantLand = "DE";
    private String lieferantUstId;
    
    // Kunde Details
    private String kundenName;
    private String kundenStrasse;
    private String kundenPlz;
    private String kundenOrt;
    private String kundenLand = "DE";
    private String kundenUstId;
    
    // Zahlungsinformationen
    private LocalDate faelligkeitsDatum;
    private String iban;
    private String bic;
    private String zahlungsReferenz;
    
    // Status
    @Enumerated(EnumType.STRING)
    private RechnungsStatus status = RechnungsStatus.ERSTELLT;

    // Standard Getters and Setters
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

    public String getWaehrung() {
        return waehrung;
    }

    public void setWaehrung(String waehrung) {
        this.waehrung = waehrung;
    }

    public String getLieferantName() {
        return lieferantName;
    }

    public void setLieferantName(String lieferantName) {
        this.lieferantName = lieferantName;
    }

    public String getLieferantStrasse() {
        return lieferantStrasse;
    }

    public void setLieferantStrasse(String lieferantStrasse) {
        this.lieferantStrasse = lieferantStrasse;
    }

    public String getLieferantPlz() {
        return lieferantPlz;
    }

    public void setLieferantPlz(String lieferantPlz) {
        this.lieferantPlz = lieferantPlz;
    }

    public String getLieferantOrt() {
        return lieferantOrt;
    }

    public void setLieferantOrt(String lieferantOrt) {
        this.lieferantOrt = lieferantOrt;
    }

    public String getLieferantLand() {
        return lieferantLand;
    }

    public void setLieferantLand(String lieferantLand) {
        this.lieferantLand = lieferantLand;
    }

    public String getLieferantUstId() {
        return lieferantUstId;
    }

    public void setLieferantUstId(String lieferantUstId) {
        this.lieferantUstId = lieferantUstId;
    }

    public String getKundenName() {
        return kundenName;
    }

    public void setKundenName(String kundenName) {
        this.kundenName = kundenName;
    }

    public String getKundenStrasse() {
        return kundenStrasse;
    }

    public void setKundenStrasse(String kundenStrasse) {
        this.kundenStrasse = kundenStrasse;
    }

    public String getKundenPlz() {
        return kundenPlz;
    }

    public void setKundenPlz(String kundenPlz) {
        this.kundenPlz = kundenPlz;
    }

    public String getKundenOrt() {
        return kundenOrt;
    }

    public void setKundenOrt(String kundenOrt) {
        this.kundenOrt = kundenOrt;
    }

    public String getKundenLand() {
        return kundenLand;
    }

    public void setKundenLand(String kundenLand) {
        this.kundenLand = kundenLand;
    }

    public String getKundenUstId() {
        return kundenUstId;
    }

    public void setKundenUstId(String kundenUstId) {
        this.kundenUstId = kundenUstId;
    }

    public LocalDate getFaelligkeitsDatum() {
        return faelligkeitsDatum;
    }

    public void setFaelligkeitsDatum(LocalDate faelligkeitsDatum) {
        this.faelligkeitsDatum = faelligkeitsDatum;
    }

    public String getIban() {
        return iban;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    public String getBic() {
        return bic;
    }

    public void setBic(String bic) {
        this.bic = bic;
    }

    public String getZahlungsReferenz() {
        return zahlungsReferenz;
    }

    public void setZahlungsReferenz(String zahlungsReferenz) {
        this.zahlungsReferenz = zahlungsReferenz;
    }

    public RechnungsStatus getStatus() {
        return status;
    }

    public void setStatus(RechnungsStatus status) {
        this.status = status;
    }
}
