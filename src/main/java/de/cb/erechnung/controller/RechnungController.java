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
@CrossOrigin(origins = "http://localhost:3000")
public class RechnungController {

    @Autowired
    private RechnungRepository rechnungRepository;

    @GetMapping("/")
    public ResponseEntity<List<Rechnung>> getAllRechnungen() {
        try {
            List<Rechnung> rechnungen = rechnungRepository.findAll();
            return ResponseEntity.ok(rechnungen);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/check")
    public ResponseEntity<?> checkConnection() {
        try {
            // Try a simple database query first
            boolean tableExists = rechnungRepository.count() >= 0;
            System.out.println("Database connection check - Table exists: " + tableExists);
            
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("status", "ok");
            response.put("timestamp", new java.util.Date());
            response.put("message", "Backend server is running and database is accessible");
            response.put("databaseStatus", "connected");
            response.put("tableStatus", tableExists ? "exists" : "not found");
            
            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
                
        } catch (org.hibernate.exception.SQLGrammarException e) {
            System.err.println("Database schema error: " + e.getMessage());
            e.printStackTrace();
            
            // Extract the root cause for better error reporting
            Throwable rootCause = e;
            while (rootCause.getCause() != null) {
                rootCause = rootCause.getCause();
            }
            
            java.util.Map<String, Object> errorResponse = new java.util.HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("timestamp", new java.util.Date());
            errorResponse.put("message", "Database schema error - The required database tables may not exist");
            errorResponse.put("details", rootCause.getMessage());
            errorResponse.put("sql", e.getSQL());
            errorResponse.put("suggestion", "Please ensure the database schema is properly initialized");
            
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .body(errorResponse);
                
        } catch (org.hibernate.exception.JDBCConnectionException e) {
            System.err.println("Database connection error: " + e.getMessage());
            e.printStackTrace();
            
            java.util.Map<String, Object> errorResponse = new java.util.HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("timestamp", new java.util.Date());
            errorResponse.put("message", "Cannot connect to database");
            errorResponse.put("details", e.getSQLException().getMessage());
            
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .body(errorResponse);
                
        } catch (Exception e) {
            System.err.println("Unexpected error during connection check: " + e.getMessage());
            e.printStackTrace();
            
            java.util.Map<String, Object> errorResponse = new java.util.HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("timestamp", new java.util.Date());
            errorResponse.put("message", "Unexpected error during database check");
            errorResponse.put("details", e.getMessage());
            errorResponse.put("type", e.getClass().getName());
            
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .body(errorResponse);
        }
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
            
            if (rechnung.getFaelligkeitsDatum() == null) {
                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Fälligkeitsdatum ist erforderlich"));
            }

            // Validate and format amount
            try {
                if (rechnung.getBetrag() != null) {
                    rechnung.setBetrag(rechnung.getBetrag().setScale(2, java.math.RoundingMode.HALF_UP));
                }
            } catch (ArithmeticException e) {
                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Ungültiges Betragsformat"));
            }

            // Validate IBAN if provided
            if (rechnung.getIban() != null && !rechnung.getIban().trim().isEmpty()) {
                if (!rechnung.getIban().matches("[A-Z]{2}[0-9]{2}[A-Z0-9]{1,30}")) {
                    return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("Ungültiges IBAN-Format"));
                }
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
    public ResponseEntity<?> updateRechnung(@PathVariable Long id, @RequestBody Rechnung rechnung) {
        try {
            if (!rechnungRepository.existsById(id)) {
                return ResponseEntity.notFound().build();
            }
            rechnung.setId(id);
            Rechnung updated = rechnungRepository.save(rechnung);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Fehler beim Aktualisieren der Rechnung: " + e.getMessage()));
        }
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
