import { apiClient } from './axios';
import {
  DocumentVersion,
  CreateVersionRequest,
  VersionListResponse,
} from '../types/version';

export const versionsApi = {
  create: async (documentId: number, data: CreateVersionRequest): Promise<DocumentVersion> => {
    const response = await apiClient.post(`/documents/${documentId}/versions`, data);
    return response.data;
  },

  submit: async (documentId: number, versionId: number): Promise<DocumentVersion> => {
    const response = await apiClient.post(`/documents/${documentId}/versions/${versionId}/submit`);
    return response.data;
  },

  approve: async (versionId: number): Promise<DocumentVersion> => {
    const response = await apiClient.post(`/versions/${versionId}/approve`);
    return response.data;
  },

  reject: async (versionId: number): Promise<DocumentVersion> => {
    const response = await apiClient.post(`/versions/${versionId}/reject`);
    return response.data;
  },

  publish: async (versionId: number): Promise<DocumentVersion> => {
    const response = await apiClient.post(`/versions/${versionId}/publish`);
    return response.data;
  },

  rollback: async (versionId: number): Promise<DocumentVersion> => {
    const response = await apiClient.post(`/versions/${versionId}/rollback`);
    return response.data;
  },

  list: async (
    documentId: number
  ): Promise<VersionListResponse> => {
    // Backend returns List<DocumentVersionResponseDto>, convert to VersionListResponse
    const response = await apiClient.get(`/documents/${documentId}/versions`);
    const versions = response.data;
    
    // Transform list to paginated response format for consistency with frontend expectations
    return {
      content: versions,
      totalElements: versions.length,
      totalPages: 1,
      currentPage: 1,
      pageSize: versions.length,
    };
  },

  getById: async (documentId: number, versionId: number): Promise<DocumentVersion> => {
    const response = await apiClient.get(`/documents/${documentId}/versions/${versionId}`);
    return response.data;
  },
};
