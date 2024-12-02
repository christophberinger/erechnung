package de.cb.erechnung.util;

import de.cb.erechnung.model.Rechnung;
import org.mustangproject.ZUGFeRD.IExportableTransaction;
import org.mustangproject.ZUGFeRD.model.DocumentCodeTypeConstants;
import org.mustangproject.ZUGFeRD.model.TradeParty;
import com.helger.pdflayout4.spec.FontSpec;

import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.Date;

public class ZugferdUtil {

    public static void createZugferdInvoice(OutputStream outputStream, Rechnung rechnung) throws Exception {
        // Create a basic invoice with minimum required fields
        TradeParty recipient = new TradeParty();
        recipient.setName("Customer Name");
        
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
            public BigDecimal getTotal() {
                return rechnung.getBetrag();
            }

            @Override
            public TradeParty getRecipient() {
                return recipient;
            }
        };

        // Create PDF with ZUGFeRD data
        org.mustangproject.ZUGFeRD.ZUGFeRDExporterFromA1 exporter = 
            new org.mustangproject.ZUGFeRD.ZUGFeRDExporterFromA1()
                .setProducer("ERechnung System")
                .setCreator(System.getProperty("user.name"));

        exporter.setTransaction(transaction);
        exporter.export(outputStream);
    }
}
