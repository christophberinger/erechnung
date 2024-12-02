package de.cb.erechnung.util;

import de.cb.erechnung.model.Rechnung;
import org.mustangproject.ZUGFeRD.IExportableTransaction;
import org.mustangproject.ZUGFeRD.ZUGFeRDExporter;

import java.io.InputStream;
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
        };

        InputStream templatePdf = ZugferdUtil.class.getResourceAsStream("/template.pdf");
        ZUGFeRDExporter exporter = new ZUGFeRDExporter()
                .setProducer("ERechnung System")
                .setCreator(System.getProperty("user.name"))
                .load(templatePdf);

        exporter.setTransaction(transaction);
        exporter.export(outputStream);
    }
}
