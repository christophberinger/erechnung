import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api/invoices';

export const invoiceService = {
  getAllInvoices: () => axios.get(API_BASE_URL),
  getInvoiceById: (id) => axios.get(`${API_BASE_URL}/${id}`),
  createInvoice: (invoice) => axios.post(API_BASE_URL, invoice),
  updateInvoice: (id, invoice) => axios.put(`${API_BASE_URL}/${id}`, invoice),
  deleteInvoice: (id) => axios.delete(`${API_BASE_URL}/${id}`),
  downloadPdf: (id) => axios.get(`${API_BASE_URL}/${id}/pdf`, { responseType: 'blob' })
};
