import { apiClient } from './axios';
import {
  Document,
  CreateDocumentRequest,
  UpdateDocumentRequest,
  DocumentListResponse,
} from '../types/document';
import { VersionComparisonResponse } from '../types/version';
import { PaginationParams } from '../types/api';

export const documentsApi = {
  list: async (params?: PaginationParams): Promise<DocumentListResponse> => {
    const response = await apiClient.get('/documents', { params });
    // Transform Spring Page to DocumentListResponse format
    // Spring Page uses 'number' (0-based) and 'size', convert to 1-based 'currentPage'
    return {
      content: response.data.content,
      totalElements: response.data.totalElements,
      totalPages: response.data.totalPages,
      currentPage: (response.data.number || 0) + 1, // Convert 0-based to 1-based
      pageSize: response.data.size || response.data.pageSize,
    };
  },

  listPublished: async (): Promise<DocumentListResponse> => {
    const response = await apiClient.get('/documents/published');
    const data = response.data;

    if (Array.isArray(data)) {
      return {
        content: data,
        totalElements: data.length,
        totalPages: data.length > 0 ? 1 : 0,
        currentPage: 1,
        pageSize: data.length,
      };
    }

    // Backward-compatible fallback if endpoint shape changes to paginated in future.
    return {
      content: data.content || [],
      totalElements: data.totalElements || 0,
      totalPages: data.totalPages || 0,
      currentPage: (data.number || 0) + 1,
      pageSize: data.size || data.pageSize || 0,
    };
  },

  getById: async (id: number): Promise<Document> => {
    const response = await apiClient.get(`/documents/${id}`);
    return response.data;
  },

  getPublishedOnly: async (id: number): Promise<Document> => {
    const response = await apiClient.get(`/documents/${id}/published-only`);
    return response.data;
  },

  create: async (data: CreateDocumentRequest): Promise<Document> => {
    const response = await apiClient.post('/documents', data);
    return response.data;
  },

  update: async (id: number, data: UpdateDocumentRequest): Promise<Document> => {
    const response = await apiClient.put(`/documents/${id}`, data);
    return response.data;
  },

  archive: async (id: number): Promise<void> => {
    // Backend returns 204 No Content, just return void
    await apiClient.patch(`/documents/${id}/archive`, {});
  },

  getHistory: async (id: number) => {
    // Backend returns List<DocumentHistoryResponseDto>, convert to pagination format
    const response = await apiClient.get(`/documents/${id}/history`);
    const history = response.data;
    return {
      content: history,
      totalElements: history.length,
      totalPages: 1,
      currentPage: 1,
      pageSize: history.length,
    };
  },

  getPublishedVersion: async (id: number) => {
    const response = await apiClient.get(`/documents/${id}/published-version`);
    return response.data;
  },

  compare: async (leftVersionId: number, rightVersionId: number): Promise<VersionComparisonResponse> => {
    const response = await apiClient.get('/documents/compare', {
      params: { leftVersionId, rightVersionId },
    });
    return response.data;
  },
};
