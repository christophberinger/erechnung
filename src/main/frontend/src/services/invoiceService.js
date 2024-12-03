import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api/invoices/';
const MAX_RETRIES = 3;
const RETRY_DELAY = 1000; // 1 second
const CONNECTION_TIMEOUT = 5000; // 5 seconds

const axiosInstance = axios.create({
  baseURL: API_BASE_URL,
  timeout: CONNECTION_TIMEOUT,
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json'
  },
  validateStatus: status => status >= 200 && status < 300
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
      console.log('Checking connection to:', API_BASE_URL);
      const instance = axios.create({
        baseURL: API_BASE_URL,
        timeout: CONNECTION_TIMEOUT,
        validateStatus: status => status >= 200 && status < 300
      });
      
      const response = await instance.get('/check');
      console.log('Connection check successful:', response.status);
      return true;
    } catch (error) {
      console.error('Connection check failed:', {
        code: error.code,
        message: error.message,
        url: API_BASE_URL,
        response: error.response,
        request: error.request
      });
      
      let message;
      if (error.code === 'ERR_CONNECTION_REFUSED') {
        message = `Der Server unter ${API_BASE_URL} ist nicht erreichbar. Bitte stellen Sie sicher, dass der Backend-Server auf Port 8080 läuft.`;
      } else if (error.code === 'ECONNABORTED') {
        message = `Zeitüberschreitung beim Verbindungsversuch zu ${API_BASE_URL}`;
      } else if (error.response) {
        message = `Server antwortet mit Fehler: ${error.response.status} ${error.response.statusText}`;
      } else {
        message = `Verbindungsfehler: ${error.message}`;
      }
      throw new Error(message);
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
