package de.cb.erechnung.controller;

import de.cb.erechnung.model.Rechnung;
import de.cb.erechnung.repository.RechnungRepository;
import de.cb.erechnung.util.ZugferdUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.util.List;

@RestController
@RequestMapping("/rechnungen")
public class RechnungController {

    @Autowired
    private RechnungRepository rechnungRepository;

    @GetMapping
    public List<Rechnung> getAllRechnungen() {
        return rechnungRepository.findAll();
    }

    @PostMapping
    public Rechnung createRechnung(@RequestBody Rechnung rechnung) {
        return rechnungRepository.save(rechnung);
    }

    @GetMapping("/{id}/zugferd")
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
