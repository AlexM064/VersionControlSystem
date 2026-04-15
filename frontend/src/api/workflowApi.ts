import { apiClient } from './axios';
import { ApprovalResponse, PublishRollbackResponse, ApprovalRequest } from '../types/api';

export const workflowApi = {
  approve: async (versionId: number, data: ApprovalRequest): Promise<ApprovalResponse> => {
    const response = await apiClient.post(`/versions/${versionId}/approve`, data);
    return response.data;
  },

  reject: async (versionId: number, data: ApprovalRequest): Promise<ApprovalResponse> => {
    const response = await apiClient.post(`/versions/${versionId}/reject`, data);
    return response.data;
  },

  publish: async (versionId: number): Promise<PublishRollbackResponse> => {
    const response = await apiClient.post(`/versions/${versionId}/publish`, {});
    return response.data;
  },

  rollback: async (versionId: number): Promise<PublishRollbackResponse> => {
    const response = await apiClient.post(`/versions/${versionId}/rollback`, {});
    return response.data;
  },
};
