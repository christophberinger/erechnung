package de.cb.erechnung.controller;

import de.cb.erechnung.model.Rechnung;
import de.cb.erechnung.repository.RechnungRepository;
import de.cb.erechnung.util.ZugferdUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import java.math.BigDecimal;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.util.List;

@RestController
@RequestMapping("/api/invoices")
@CrossOrigin(origins = "http://localhost:3000") // For React frontend
public class RechnungController {

    @Autowired
    private RechnungRepository rechnungRepository;

    @GetMapping
    public List<Rechnung> getAllRechnungen() {
        return rechnungRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Rechnung> getRechnungById(@PathVariable Long id) {
        return rechnungRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createRechnung(@RequestBody Rechnung rechnung) {
        try {
            // Log incoming request
            System.out.println("Received invoice creation request: " + rechnung.getRechnungsNummer());
            // Validate required fields
            if (rechnung.getRechnungsNummer() == null || rechnung.getRechnungsNummer().trim().isEmpty()) {
                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Rechnungsnummer ist erforderlich"));
            }
            if (rechnung.getBetrag() == null || rechnung.getBetrag().compareTo(BigDecimal.ZERO) <= 0) {
                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Betrag muss größer als 0 sein"));
            }
            if (rechnung.getDatum() == null) {
                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Rechnungsdatum ist erforderlich"));
            }
            if (rechnung.getLieferantName() == null || rechnung.getLieferantName().trim().isEmpty()) {
                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Lieferant Name ist erforderlich"));
            }
            if (rechnung.getKundenName() == null || rechnung.getKundenName().trim().isEmpty()) {
                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Kunde Name ist erforderlich"));
            }
            if (rechnung.getWaehrung() == null || rechnung.getWaehrung().trim().isEmpty()) {
                rechnung.setWaehrung("EUR"); // Default to EUR if not specified
            }
            
            // Parse amount if it's a string
            if (rechnung.getBetrag() != null && rechnung.getBetrag().scale() > 2) {
                rechnung.setBetrag(rechnung.getBetrag().setScale(2, java.math.RoundingMode.HALF_UP));
            }

            Rechnung saved = rechnungRepository.save(rechnung);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            // Log the full stack trace
            e.printStackTrace();
            
            String errorMessage = "Fehler beim Speichern der Rechnung: " + e.getMessage();
            System.err.println(errorMessage);
            
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(errorMessage));
        }
    }

    private static class ErrorResponse {
        private String message;
        
        public ErrorResponse(String message) {
            this.message = message;
        }
        
        public String getMessage() {
            return message;
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Rechnung> updateRechnung(@PathVariable Long id, @RequestBody Rechnung rechnung) {
        if (!rechnungRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        rechnung.setId(id);
        return ResponseEntity.ok(rechnungRepository.save(rechnung));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRechnung(@PathVariable Long id) {
        if (!rechnungRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        rechnungRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> getZugferdRechnung(@PathVariable Long id) throws Exception {
        Rechnung rechnung = rechnungRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rechnung nicht gefunden"));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ZugferdUtil.createZugferdInvoice(outputStream, rechnung);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("filename", "rechnung_" + rechnung.getRechnungsNummer() + ".pdf");

        return new ResponseEntity<>(outputStream.toByteArray(), headers, HttpStatus.OK);
    }
}
