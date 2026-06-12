import { useQuery } from '@tanstack/react-query';
import { documentService, documentsApi } from '@/api';
import { Document } from '@/types/document';
import { DocumentVersion, VersionListResponse } from '@/types/version';

export const useDocumentDetails = (documentId: number, publishedOnly = false) => {
  return useQuery<Document>({
    queryKey: ['document', documentId, { publishedOnly }],
    queryFn: async () => {
      return documentService.getDocument(documentId, publishedOnly);
    },
    staleTime: 1000 * 60 * 5,
    gcTime: 1000 * 60 * 10,
    enabled: documentId > 0,
  });
};

export const useDocumentVersions = (documentId: number, page = 1, pageSize = 10, enabled = true) => {
  return useQuery<VersionListResponse>({
    queryKey: ['documentVersions', documentId, { page, pageSize }],
    queryFn: async () => {
      return documentService.getVersionsPage(documentId, page, pageSize);
    },
    staleTime: 1000 * 60 * 5,
    gcTime: 1000 * 60 * 10,
    enabled: documentId > 0 && enabled,
  });
};

export const usePublishedVersion = (documentId: number, enabled = true) => {
  return useQuery<DocumentVersion | null>({
    queryKey: ['publishedVersion', documentId],
    queryFn: async () => {
      return documentService.getPublishedVersion(documentId);
    },
    staleTime: 1000 * 60 * 5,
    gcTime: 1000 * 60 * 10,
    enabled: documentId > 0 && enabled,
  });
};

export const useDocumentHistory = (documentId: number, page = 1, pageSize = 10) => {
  return useQuery({
    queryKey: ['documentHistory', documentId, { page, pageSize }],
    queryFn: async () => {
      // Backend now returns full list, no pagination params
      const response = await documentsApi.getHistory(documentId);
      const historyItems = response?.content ?? [];
      
      // Implement client-side pagination
      const startIndex = (page - 1) * pageSize;
      const paginatedContent = historyItems.slice(startIndex, startIndex + pageSize);
      
      return {
        ...response,
        content: paginatedContent,
        currentPage: page,
        pageSize,
      };
    },
    staleTime: 1000 * 60 * 5,
    gcTime: 1000 * 60 * 10,
    enabled: documentId > 0,
  });
};
