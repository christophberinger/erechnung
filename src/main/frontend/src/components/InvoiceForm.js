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
    lieferantStrasse: '',
    lieferantPlz: '',
    lieferantOrt: '',
    lieferantLand: 'DE',
    lieferantUstId: '',
    kundenName: '',
    kundenStrasse: '',
    kundenPlz: '',
    kundenOrt: '',
    kundenLand: 'DE',
    kundenUstId: '',
    faelligkeitsDatum: '',
    zahlungsReferenz: '',
    iban: '',
    bic: ''
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
      // Check server connection first
      const isConnected = await invoiceService.checkConnection();
      if (!isConnected) {
        setSnackbar({
          open: true,
          message: 'Server nicht erreichbar. Bitte überprüfen Sie die Verbindung zum Backend.',
          severity: 'error'
        });
        return;
      }

      // Format data before sending
      const formattedInvoice = {
        ...invoice,
        betrag: Number(parseFloat(invoice.betrag || 0).toFixed(2)),
        datum: invoice.datum ? new Date(invoice.datum).toISOString().split('T')[0] : null,
        faelligkeitsDatum: invoice.faelligkeitsDatum ? 
          new Date(invoice.faelligkeitsDatum).toISOString().split('T')[0] : null,
        waehrung: invoice.waehrung || 'EUR',
        iban: invoice.iban?.trim().toUpperCase(),
        bic: invoice.bic?.trim().toUpperCase()
      };
      
      console.log('Submitting invoice:', formattedInvoice);
      const savedInvoice = await invoiceService.createInvoice(formattedInvoice);
      console.log('Saved invoice:', savedInvoice);
      setSnackbar({
        open: true,
        message: 'Rechnung erfolgreich gespeichert',
        severity: 'success'
      });
      setInvoice(initialState); // Reset form
    } catch (error) {
      console.error('Error creating invoice:', error);
      let errorMessage;
      
      if (error.code === 'ERR_NETWORK' || error.code === 'ERR_CONNECTION_REFUSED') {
        errorMessage = 'Keine Verbindung zum Server möglich. Bitte überprüfen Sie, ob der Server läuft.';
      } else if (error.code === 'ECONNABORTED') {
        errorMessage = 'Die Anfrage wurde wegen Zeitüberschreitung abgebrochen.';
      } else {
        errorMessage = error.message || 
                      error.response?.data?.message || 
                      'Fehler beim Speichern der Rechnung. Bitte überprüfen Sie Ihre Eingaben.';
      }
      setSnackbar({
        open: true,
        message: errorMessage,
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
              inputProps={{ step: "0.01", min: "0" }}
              value={invoice.betrag}
              onChange={handleChange}
              required
            />
          </Grid>
          <Grid item xs={6}>
            <TextField
              fullWidth
              label="Währung"
              name="waehrung"
              value={invoice.waehrung}
              onChange={handleChange}
              required
            />
          </Grid>
          
          {/* Lieferant Details */}
          <Grid item xs={12}>
            <Typography variant="subtitle1">Lieferant Details</Typography>
          </Grid>
          <Grid item xs={6}>
            <TextField
              fullWidth
              label="Name"
              name="lieferantName"
              value={invoice.lieferantName}
              onChange={handleChange}
              required
            />
          </Grid>
          <Grid item xs={6}>
            <TextField
              fullWidth
              label="Straße"
              name="lieferantStrasse"
              value={invoice.lieferantStrasse}
              onChange={handleChange}
              required
            />
          </Grid>
          <Grid item xs={4}>
            <TextField
              fullWidth
              label="PLZ"
              name="lieferantPlz"
              value={invoice.lieferantPlz}
              onChange={handleChange}
              required
            />
          </Grid>
          <Grid item xs={4}>
            <TextField
              fullWidth
              label="Ort"
              name="lieferantOrt"
              value={invoice.lieferantOrt}
              onChange={handleChange}
              required
            />
          </Grid>
          <Grid item xs={4}>
            <TextField
              fullWidth
              label="USt-ID"
              name="lieferantUstId"
              value={invoice.lieferantUstId}
              onChange={handleChange}
            />
          </Grid>

          {/* Kunde Details */}
          <Grid item xs={12}>
            <Typography variant="subtitle1">Kunde Details</Typography>
          </Grid>
          <Grid item xs={6}>
            <TextField
              fullWidth
              label="Name"
              name="kundenName"
              value={invoice.kundenName}
              onChange={handleChange}
              required
            />
          </Grid>
          <Grid item xs={6}>
            <TextField
              fullWidth
              label="Straße"
              name="kundenStrasse"
              value={invoice.kundenStrasse}
              onChange={handleChange}
              required
            />
          </Grid>
          <Grid item xs={4}>
            <TextField
              fullWidth
              label="PLZ"
              name="kundenPlz"
              value={invoice.kundenPlz}
              onChange={handleChange}
              required
            />
          </Grid>
          <Grid item xs={4}>
            <TextField
              fullWidth
              label="Ort"
              name="kundenOrt"
              value={invoice.kundenOrt}
              onChange={handleChange}
              required
            />
          </Grid>
          <Grid item xs={4}>
            <TextField
              fullWidth
              label="USt-ID"
              name="kundenUstId"
              value={invoice.kundenUstId}
              onChange={handleChange}
            />
          </Grid>

          {/* Payment Details */}
          <Grid item xs={12}>
            <Typography variant="subtitle1">Zahlungsdetails</Typography>
          </Grid>
          <Grid item xs={6}>
            <TextField
              fullWidth
              type="date"
              label="Fälligkeitsdatum"
              name="faelligkeitsDatum"
              value={invoice.faelligkeitsDatum}
              onChange={handleChange}
              InputLabelProps={{ shrink: true }}
              required
            />
          </Grid>
          <Grid item xs={6}>
            <TextField
              fullWidth
              label="Zahlungsreferenz"
              name="zahlungsReferenz"
              value={invoice.zahlungsReferenz}
              onChange={handleChange}
            />
          </Grid>
          <Grid item xs={6}>
            <TextField
              fullWidth
              label="IBAN"
              name="iban"
              value={invoice.iban || ''}
              onChange={handleChange}
              inputProps={{ style: { textTransform: 'uppercase' } }}
            />
          </Grid>
          <Grid item xs={6}>
            <TextField
              fullWidth
              label="BIC"
              name="bic"
              value={invoice.bic || ''}
              onChange={handleChange}
              inputProps={{ style: { textTransform: 'uppercase' } }}
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
