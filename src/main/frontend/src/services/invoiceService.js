import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api/invoices';
const MAX_RETRIES = 3;
const RETRY_DELAY = 1000; // 1 second

const axiosInstance = axios.create({
  baseURL: API_BASE_URL,
  timeout: 5000 // 5 seconds
});

const sleep = (ms) => new Promise(resolve => setTimeout(resolve, ms));

const withRetry = async (operation, retries = MAX_RETRIES) => {
  for (let i = 0; i < retries; i++) {
    try {
      return await operation();
    } catch (error) {
      if (i === retries - 1) throw error;
      
      // Check if server is unreachable
      if (error.code === 'ERR_CONNECTION_REFUSED') {
        console.warn(`Connection refused, retrying in ${RETRY_DELAY}ms... (Attempt ${i + 1}/${retries})`);
        await sleep(RETRY_DELAY);
        continue;
      }
      
      throw error;
    }
  }
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
      await axiosInstance.get('/check');
      return true;
    } catch (error) {
      console.error('Connection check failed:', error);
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
