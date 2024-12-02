package de.cb.erechnung.util;

import de.cb.erechnung.model.Rechnung;
import org.mustangproject.ZUGFeRD.IZUGFeRDExportableTransaction;
import org.mustangproject.ZUGFeRD.ZUGFeRDExporterFromA1;

import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.Date;

public class ZugferdUtil {

    public static void createZugferdInvoice(OutputStream outputStream, Rechnung rechnung) throws Exception {
        IZUGFeRDExportableTransaction transaction = new IZUGFeRDExportableTransaction() {
            @Override
            public String getNumber() {
                return rechnung.getRechnungsNummer();
            }

            @Override
            public Date getDate() {
                return new Date();
            }

            @Override
            public String getOwnCountry() {
                return "DE";
            }

            @Override
            public String getCurrency() {
                return "EUR";
            }

            @Override
            public String getOwnVATID() {
                return "DE123456789"; // Replace with your company's VAT ID
            }

            @Override
            public String getOwnStreet() {
                return "Musterstraße 1";
            }

            @Override
            public String getOwnZIP() {
                return "12345";
            }

            @Override
            public String getOwnLocation() {
                return "Musterstadt";
            }

            @Override
            public String getOwnOrganisationName() {
                return "Musterfirma GmbH";
            }

            @Override
            public BigDecimal getTotal() {
                return BigDecimal.valueOf(rechnung.getBetrag());
            }
        };

        InputStream templatePdf = ZugferdUtil.class.getResourceAsStream("/template.pdf");
        ZUGFeRDExporterFromA1 exporter = new ZUGFeRDExporterFromA1()
                .setProducer("ERechnung System")
                .setCreator(System.getProperty("user.name"))
                .load(templatePdf);

        exporter.setTransaction(transaction);
        exporter.export(outputStream);
    }
}
