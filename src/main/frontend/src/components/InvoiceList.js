import React, { useState, useEffect } from 'react';
import { 
  Table, 
  TableBody, 
  TableCell, 
  TableContainer, 
  TableHead, 
  TableRow, 
  Paper, 
  IconButton 
} from '@mui/material';
import { Delete, Edit, PictureAsPdf } from '@mui/icons-material';
import { invoiceService } from '../services/invoiceService';

export const InvoiceList = () => {
  const [invoices, setInvoices] = useState([]);

  useEffect(() => {
    invoiceService.getAllInvoices()
      .then(response => setInvoices(response.data))
      .catch(error => console.error('Error fetching invoices:', error));
  }, []);

  const handleDelete = (id) => {
    invoiceService.deleteInvoice(id)
      .then(() => {
        setInvoices(invoices.filter(invoice => invoice.id !== id));
      })
      .catch(error => console.error('Error deleting invoice:', error));
  };

  const handleDownloadPdf = (id) => {
    invoiceService.downloadPdf(id)
      .then(response => {
        const blob = new Blob([response.data], { type: 'application/pdf' });
        const link = document.createElement('a');
        link.href = window.URL.createObjectURL(blob);
        link.download = `invoice_${id}.pdf`;
        link.click();
      })
      .catch(error => console.error('Error downloading PDF:', error));
  };

  return (
    <TableContainer component={Paper}>
      <Table>
        <TableHead>
          <TableRow>
            <TableCell>Rechnungsnummer</TableCell>
            <TableCell>Datum</TableCell>
            <TableCell>Betrag</TableCell>
            <TableCell>Aktionen</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {invoices.map(invoice => (
            <TableRow key={invoice.id}>
              <TableCell>{invoice.rechnungsNummer}</TableCell>
              <TableCell>{invoice.datum}</TableCell>
              <TableCell>{invoice.betrag} {invoice.waehrung}</TableCell>
              <TableCell>
                <IconButton onClick={() => handleDownloadPdf(invoice.id)}>
                  <PictureAsPdf />
                </IconButton>
                <IconButton>
                  <Edit />
                </IconButton>
                <IconButton onClick={() => handleDelete(invoice.id)}>
                  <Delete />
                </IconButton>
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </TableContainer>
  );
};
