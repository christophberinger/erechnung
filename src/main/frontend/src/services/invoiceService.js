import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api/invoices/';
const MAX_RETRIES = 5;
const RETRY_DELAY = 2000; // 2 seconds

const axiosInstance = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000, // 10 seconds
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json'
  }
});

// Add response interceptor
axiosInstance.interceptors.response.use(
  response => response,
  error => {
    if (error.code === 'ECONNABORTED') {
      throw new Error('Die Verbindung zum Server wurde wegen Zeitüberschreitung abgebrochen.');
    }
    if (error.code === 'ERR_NETWORK' || error.code === 'ERR_CONNECTION_REFUSED') {
      throw new Error('Keine Verbindung zum Server möglich. Bitte überprüfen Sie, ob der Server läuft.');
    }
    throw error;
  }
);

const sleep = (ms) => new Promise(resolve => setTimeout(resolve, ms));

const withRetry = async (operation, retries = MAX_RETRIES) => {
  let lastError;
  for (let i = 0; i < retries; i++) {
    try {
      return await operation();
    } catch (error) {
      lastError = error;
      
      // Only retry on connection errors
      if (error.code === 'ERR_CONNECTION_REFUSED' || 
          error.code === 'ECONNABORTED' ||
          error.code === 'ERR_NETWORK') {
        console.warn(`Connection attempt ${i + 1}/${retries} failed: ${error.message}`);
        if (i < retries - 1) {
          console.warn(`Retrying in ${RETRY_DELAY}ms...`);
          await sleep(RETRY_DELAY * (i + 1)); // Exponential backoff
          continue;
        }
      }
      
      throw error;
    }
  }
  throw lastError;
};

const handleError = (error) => {
  if (error.code === 'ERR_CONNECTION_REFUSED') {
    throw new Error('Server nicht erreichbar. Bitte überprüfen Sie, ob der Backend-Server läuft.');
  }
  
  // Handle specific HTTP status codes
  if (error.response) {
    const status = error.response.status;
    const message = error.response.data?.message || error.response.data;
    
    switch (status) {
      case 500:
        throw new Error(`Interner Serverfehler: ${message}`);
      case 400:
        throw new Error(`Ungültige Eingabe: ${message}`);
      case 404:
        throw new Error('Ressource nicht gefunden');
      default:
        throw new Error(`Fehler: ${message || error.message}`);
    }
  }
  
  throw error;
};

export const invoiceService = {
  checkConnection: async () => {
    try {
      await withRetry(() => axiosInstance.get('/check'));
      return true;
    } catch (error) {
      console.error('Connection check failed:', error);
      // Throw specific error for connection issues
      if (error.code === 'ERR_CONNECTION_REFUSED') {
        throw new Error('Der Server ist nicht erreichbar. Bitte stellen Sie sicher, dass der Backend-Server läuft und erreichbar ist.');
      }
      return false;
    }
  },

  getAllInvoices: async () => {
    const response = await withRetry(() => axiosInstance.get('')).catch(handleError);
    return response.data;
  },

  getInvoiceById: async (id) => {
    const response = await withRetry(() => axiosInstance.get(`/${id}`)).catch(handleError);
    return response.data;
  },

  createInvoice: async (invoice) => {
    const response = await withRetry(() => axiosInstance.post('', invoice)).catch(handleError);
    return response.data;
  },

  updateInvoice: async (id, invoice) => {
    const response = await withRetry(() => axiosInstance.put(`/${id}`, invoice)).catch(handleError);
    return response.data;
  },

  deleteInvoice: async (id) => {
    await withRetry(() => axiosInstance.delete(`/${id}`)).catch(handleError);
    return true;
  },

  downloadPdf: async (id) => {
    const response = await withRetry(() => 
      axiosInstance.get(`/${id}/pdf`, { responseType: 'blob' })
    ).catch(handleError);
    return response.data;
  }
};
