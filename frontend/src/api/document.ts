import { documentsApi } from './documentsApi';
import { versionsApi } from './versionsApi';
import { Document } from '@/types/document';
import { DocumentVersion, VersionListResponse } from '@/types/version';

export const documentService = {
  getDocument: async (documentId: number, publishedOnly = false): Promise<Document> => {
    return publishedOnly ? documentsApi.getPublishedOnly(documentId) : documentsApi.getById(documentId);
  },

  getVersionsPage: async (documentId: number, page = 1, pageSize = 10): Promise<VersionListResponse> => {
    const response = await versionsApi.list(documentId);
    const versionItems = response?.content ?? [];
    const startIndex = (page - 1) * pageSize;

    return {
      ...response,
      content: versionItems.slice(startIndex, startIndex + pageSize),
      currentPage: page,
      pageSize,
      totalElements: response?.totalElements ?? versionItems.length,
      totalPages: Math.max(1, Math.ceil(versionItems.length / pageSize)),
    };
  },

  getVersionById: async (documentId: number, versionId: number): Promise<DocumentVersion> => {
    return versionsApi.getById(documentId, versionId);
  },

  getPublishedVersion: async (documentId: number): Promise<DocumentVersion> => {
    return documentsApi.getPublishedVersion(documentId);
  },
};
