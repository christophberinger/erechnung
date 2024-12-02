import React, { useState } from 'react';
import { 
  TextField, 
  Button, 
  Grid, 
  Paper, 
  Typography 
} from '@mui/material';
import { invoiceService } from '../services/invoiceService';

export const InvoiceForm = () => {
  const [invoice, setInvoice] = useState({
    rechnungsNummer: '',
    datum: new Date().toISOString().split('T')[0],
    betrag: '',
    waehrung: 'EUR',
    lieferantName: '',
    kundenName: ''
  });

  const handleChange = (e) => {
    const { name, value } = e.target;
    setInvoice(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    invoiceService.createInvoice(invoice)
      .then(response => {
        console.log('Invoice created:', response.data);
        // Reset form or navigate
      })
      .catch(error => console.error('Error creating invoice:', error));
  };

  return (
    <Paper style={{ padding: 16 }}>
      <Typography variant="h6">Neue Rechnung</Typography>
      <form onSubmit={handleSubmit}>
        <Grid container spacing={2}>
          <Grid item xs={6}>
            <TextField
              fullWidth
              label="Rechnungsnummer"
              name="rechnungsNummer"
              value={invoice.rechnungsNummer}
              onChange={handleChange}
              required
            />
          </Grid>
          <Grid item xs={6}>
            <TextField
              fullWidth
              type="date"
              label="Datum"
              name="datum"
              value={invoice.datum}
              onChange={handleChange}
              InputLabelProps={{ shrink: true }}
            />
          </Grid>
          <Grid item xs={6}>
            <TextField
              fullWidth
              label="Betrag"
              name="betrag"
              type="number"
              value={invoice.betrag}
              onChange={handleChange}
              required
            />
          </Grid>
          <Grid item xs={6}>
            <TextField
              fullWidth
              label="Lieferant"
              name="lieferantName"
              value={invoice.lieferantName}
              onChange={handleChange}
            />
          </Grid>
          <Grid item xs={12}>
            <Button 
              type="submit" 
              variant="contained" 
              color="primary"
            >
              Rechnung speichern
            </Button>
          </Grid>
        </Grid>
      </form>
    </Paper>
  );
};
