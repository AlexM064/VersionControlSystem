import { useQuery } from '@tanstack/react-query';
import { documentsApi } from '@/api';
import { useAuth } from '@/contexts/AuthContext';
import { DocumentStatus } from '@/types/document';
import { UserRole } from '@/types/auth';

export interface DashboardStats {
  totalDocuments: number;
  activeDocuments: number;
  archivedDocuments: number;
  publishedDocuments: number;
  versionsInReview: number | null; // null means not calculated due to performance
}

export interface UseDashboardStatsOptions {
  enabled?: boolean;
}

export const useDashboardStats = ({ enabled = true }: UseDashboardStatsOptions = {}) => {
  const { user } = useAuth();
  const isReader = user?.roles.includes(UserRole.READER);

  return useQuery<DashboardStats>({
    queryKey: ['dashboard-stats', { isReader }],
    queryFn: async () => {
      // Fetch all documents with a large page size to get complete stats
      // Note: In a real app, you might want to add pagination or server-side aggregation
      const response = isReader
        ? await documentsApi.listPublished()
        : await documentsApi.list({
            page: 0,
            pageSize: 1000, // Large page size to get all documents
          });

      const documents = response?.content ?? [];

      const stats: DashboardStats = {
        totalDocuments: response?.totalElements ?? documents.length,
        activeDocuments: documents.filter(doc => doc.status === DocumentStatus.ACTIVE).length,
        archivedDocuments: documents.filter(doc => doc.status === DocumentStatus.ARCHIVED).length,
        publishedDocuments: documents.filter(doc => doc.publishedVersionId != null).length,
        versionsInReview: null, // Not calculated to avoid expensive API calls
      };

      return stats;
    },
    enabled: !!user && enabled,
  });
};