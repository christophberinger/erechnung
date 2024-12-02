package de.cb.erechnung.util;

import de.cb.erechnung.model.Rechnung;
import org.mustangproject.ZUGFeRD.IExportableTransaction;
import org.mustangproject.ZUGFeRD.ZUGFeRDExporterFromA1;

import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.Date;

public class ZugferdUtil {

    public static void createZugferdInvoice(OutputStream outputStream, Rechnung rechnung) throws Exception {
        IExportableTransaction transaction = new IExportableTransaction() {
            @Override
            public String getNumber() {
                return rechnung.getRechnungsNummer();
            }

            @Override
            public Date getDate() {
                return java.sql.Date.valueOf(rechnung.getDatum());
            }

            @Override
            public Date getDeliveryDate() {
                return getDate(); // Using invoice date as delivery date
            }

            @Override
            public BigDecimal getTotal() {
                return rechnung.getBetrag();
            }

            @Override
            public String getCurrency() {
                return "EUR"; // Default to EUR
            }

            @Override
            public IZUGFeRDExportableContact getRecipient() {
                return null; // TODO: Implement if recipient information is needed
            }
        };

        // Create PDF with ZUGFeRD data
        ZUGFeRDExporterFromA1 exporter = new ZUGFeRDExporterFromA1()
            .setProducer("ERechnung System")
            .setCreator(System.getProperty("user.name"));

        exporter.setTransaction(transaction);
        exporter.export(outputStream);
    }
}
