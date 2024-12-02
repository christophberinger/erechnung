import React, { useState } from 'react';
import { 
  TextField, 
  Button, 
  Grid, 
  Paper, 
  Typography,
  Snackbar,
  Alert
} from '@mui/material';
import { invoiceService } from '../services/invoiceService';

export const InvoiceForm = () => {
  const initialState = {
    rechnungsNummer: '',
    datum: new Date().toISOString().split('T')[0],
    betrag: '',
    waehrung: 'EUR',
    lieferantName: '',
    kundenName: ''
  };

  const [invoice, setInvoice] = useState(initialState);
  const [snackbar, setSnackbar] = useState({
    open: false,
    message: '',
    severity: 'success'
  });

  const handleCloseSnackbar = () => {
    setSnackbar(prev => ({...prev, open: false}));
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setInvoice(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const response = await invoiceService.createInvoice(invoice);
      setSnackbar({
        open: true,
        message: 'Rechnung erfolgreich gespeichert',
        severity: 'success'
      });
      setInvoice(initialState); // Reset form
    } catch (error) {
      console.error('Error creating invoice:', error);
      setSnackbar({
        open: true,
        message: 'Fehler beim Speichern der Rechnung',
        severity: 'error'
      });
    }
  };

  return (
    <div>
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
      <Snackbar 
      open={snackbar.open} 
      autoHideDuration={6000} 
      onClose={handleCloseSnackbar}
    >
      <Alert 
        onClose={handleCloseSnackbar} 
        severity={snackbar.severity}
      >
        {snackbar.message}
      </Alert>
      </Snackbar>
    </div>
  );
};
