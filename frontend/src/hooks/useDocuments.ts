import { useQuery } from '@tanstack/react-query';
import { documentsApi } from '@/api';
import { DocumentListResponse, DocumentStatus } from '@/types/document';

export interface UseDocumentsParams {
  page?: number;
  pageSize?: number;
  title?: string;
  status?: DocumentStatus | '';
  sortBy?: string;
  sortOrder?: 'ASC' | 'DESC';
  publishedOnly?: boolean;
  enabled?: boolean;
}

export const useDocuments = ({
  page = 1,
  pageSize = 10,
  title,
  status,
  sortBy = 'createdAt',
  sortOrder = 'DESC',
  publishedOnly = false,
  enabled = true,
}: UseDocumentsParams) => {
  return useQuery<DocumentListResponse>({
    queryKey: ['documents', { page, pageSize, title, status, sortBy, sortOrder, publishedOnly }],
    queryFn: async () => {
      if (publishedOnly) {
        const response = await documentsApi.listPublished();
        const publishedDocuments = response?.content ?? [];
        const normalizedTitle = title?.trim().toLowerCase();
        const filteredContent = publishedDocuments.filter((document) => {
          if (normalizedTitle && !document.title.toLowerCase().includes(normalizedTitle)) {
            return false;
          }

          return true;
        });

        const totalElements = filteredContent.length;
        const totalPages = Math.max(1, Math.ceil(totalElements / pageSize));
        const startIndex = (page - 1) * pageSize;

        return {
          content: filteredContent.slice(startIndex, startIndex + pageSize),
          totalElements,
          totalPages,
          currentPage: page,
          pageSize,
        };
      }

      const params: Record<string, any> = {
        page: page - 1, // Backend uses 0-based pagination
        pageSize,
        sortBy,
        sortOrder,
      };

      if (title) {
        params.title = title;
      }

      if (status) {
        params.status = status;
      }

      const response = await documentsApi.list(params);

      return {
        ...response,
        content: response?.content ?? [],
        totalElements: response?.totalElements ?? 0,
        totalPages: response?.totalPages ?? 0,
        currentPage: response?.currentPage ?? page,
        pageSize: response?.pageSize ?? pageSize,
      };
    },
    staleTime: 1000 * 60 * 5, // 5 minutes
    gcTime: 1000 * 60 * 10, // 10 minutes (formerly cacheTime)
    enabled,
  });
};
