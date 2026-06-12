import { useQuery } from '@tanstack/react-query';
import { documentsApi, versionsApi } from '@/api';
import { VersionComparisonResponse, VersionListResponse } from '@/types/version';

export const useDocumentVersionList = (documentId: number) => {
  return useQuery<VersionListResponse>({
    queryKey: ['documentVersionList', documentId],
    queryFn: async () => {
      return versionsApi.list(documentId);
    },
    staleTime: 1000 * 60 * 5,
    gcTime: 1000 * 60 * 10,
    enabled: documentId > 0,
  });
};

export const useCompareVersions = (
  documentId: number,
  leftVersionId: number,
  rightVersionId: number
) => {
  return useQuery<VersionComparisonResponse>({
    queryKey: ['versionComparison', documentId, leftVersionId, rightVersionId],
    queryFn: async () => {
      return documentsApi.compare(leftVersionId, rightVersionId);
    },
    staleTime: 1000 * 60 * 5,
    gcTime: 1000 * 60 * 10,
    enabled: documentId > 0 && leftVersionId > 0 && rightVersionId > 0,
  });
};