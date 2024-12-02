package de.cb.erechnung.util;

import de.cb.erechnung.model.Rechnung;
import org.mustangproject.ZUGFeRD.*;

import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class ZugferdUtil {

    public static void createZugferdInvoice(OutputStream outputStream, Rechnung rechnung) throws Exception {
        IExportableTransaction transaction = new IExportableTransaction() {
            @Override
            public String getNumber() {
                return rechnung.getRechnungsNummer();
            }

            @Override
            public Date getIssueDate() {
                return java.sql.Date.valueOf(rechnung.getDatum());
            }

            @Override
            public Date getDeliveryDate() {
                return getIssueDate(); // Using invoice date as delivery date
            }

            @Override
            public Date getDueDate() {
                // Set due date to 30 days after issue date
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(getIssueDate());
                calendar.add(Calendar.DAY_OF_MONTH, 30);
                return calendar.getTime();
            }

            @Override
            public String getCurrency() {
                return "EUR"; // Default to EUR
            }

            @Override
            public IZUGFeRDTradeSettlement[] getTradeSettlement() {
                return new IZUGFeRDTradeSettlement[0]; // No specific trade settlement info for now
            }

            @Override
            public String getReferenceNumber() {
                return rechnung.getRechnungsNummer(); // Using invoice number as reference
            }

            @Override
            public IZUGFeRDExportableTradeParty getRecipient() {
                return null; // TODO: Implement if recipient information is needed
            }

            @Override
            public IZUGFeRDExportableItem[] getZFItems() {
                return new IZUGFeRDExportableItem[0]; // TODO: Add line items if needed
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
