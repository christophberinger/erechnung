package de.cb.erechnung.util;

import de.cb.erechnung.model.Rechnung;
import io.konik.InvoiceTransformer;
import io.konik.PdfHandler;
import io.konik.zugferd.Invoice;
import io.konik.zugferd.entity.Header;
import io.konik.zugferd.entity.trade.item.Item;
import io.konik.zugferd.unqualified.Amount;

import java.io.InputStream;
import java.io.OutputStream;

public class ZugferdUtil {

    public static void createZugferdInvoice(OutputStream outputStream, Rechnung rechnung) throws Exception {
        Invoice invoice = new Invoice();
        Header header = new Header();
        header.setInvoiceNumber(rechnung.getRechnungsNummer());
        invoice.setHeader(header);

        Item item = new Item();
        item.setGlobalId(new Item.Builder()
                .addSchemeId("0160")
                .addSchemeId(rechnung.getId().toString())
                .build());
        item.setName("Beispielprodukt");
        item.setGrossPrice(new Amount(rechnung.getBetrag()));

        invoice.addItem(item);

        InvoiceTransformer transformer = new InvoiceTransformer();
        byte[] zugferdXml = transformer.fromModel(invoice);

        PdfHandler handler = new PdfHandler();
        InputStream templatePdf = ZugferdUtil.class.getResourceAsStream("/template.pdf");
        handler.appendZugferdXMLToPackage(templatePdf, zugferdXml, outputStream);
    }
}
RechnungController erstellen: Erstellen Sie eine neue Datei unter src/main/java/de/cb/erechnung/controller/RechnungController.java:

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