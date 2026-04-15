import { apiClient } from './axios';

export const pdfApi = {
  getPublishedVersionPdf: async (documentId: number): Promise<Blob> => {
    const response = await apiClient.get(`/documents/${documentId}/published-version/pdf`, {
      responseType: 'blob',
    });
    return response.data;
  },

  getVersionPdf: async (documentId: number, versionId: number): Promise<Blob> => {
    const response = await apiClient.get(
      `/documents/${documentId}/versions/${versionId}/pdf`,
      {
        responseType: 'blob',
      }
    );
    return response.data;
  },
};
