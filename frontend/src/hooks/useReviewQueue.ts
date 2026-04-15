import { useQuery } from '@tanstack/react-query';
import { documentsApi, versionsApi } from '@/api';
import { VersionStatus } from '@/types/version';

export interface ReviewQueueItem {
  documentId: number;
  documentTitle: string;
  documentOwnerUsername: string;
  versionId: number;
  versionNumber: number;
  message: string;
  createdByUsername: string;
  createdAt: string;
}

export const useReviewQueue = () => {
  return useQuery<ReviewQueueItem[]>({
    queryKey: ['reviewQueue'],
    queryFn: async () => {
      const documentsResponse = await documentsApi.list({
        page: 0,
        pageSize: 1000,
        sortBy: 'updatedAt',
        sortOrder: 'DESC',
      });
      const documents = documentsResponse?.content ?? [];

      const queueByDocument = await Promise.all(
        documents.map(async (document) => {
          const versions = await versionsApi.list(document.id);
          const versionItems = versions?.content ?? [];

          return versionItems
            .filter((version) => version.status === VersionStatus.IN_REVIEW)
            .map((version) => ({
              documentId: document.id,
              documentTitle: document.title,
              documentOwnerUsername: document.ownerUsername,
              versionId: version.id,
              versionNumber: version.versionNumber,
              message: version.message || '',
              createdByUsername: version.createdByUsername,
              createdAt: version.createdAt,
            }));
        })
      );

      return queueByDocument
        .flat()
        .sort((left, right) => new Date(right.createdAt).getTime() - new Date(left.createdAt).getTime());
    },
    staleTime: 1000 * 60 * 3,
    gcTime: 1000 * 60 * 10,
  });
};