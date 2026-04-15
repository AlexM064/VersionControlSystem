import { useMutation, useQuery } from '@tanstack/react-query';
import { documentsApi } from '@/api';
import { Document, CreateDocumentRequest, UpdateDocumentRequest } from '@/types/document';

export const useCreateDocument = () => {
  return useMutation({
    mutationFn: async (data: CreateDocumentRequest) => {
      return documentsApi.create(data);
    },
  });
};

export const useUpdateDocument = (documentId: number) => {
  return useMutation({
    mutationFn: async (data: UpdateDocumentRequest) => {
      return documentsApi.update(documentId, data);
    },
  });
};

export const useArchiveDocument = () => {
  return useMutation({
    mutationFn: async (documentId: number) => {
      await documentsApi.archive(documentId);
    },
  });
};

export const useGetDocument = (documentId: number) => {
  return useQuery<Document>({
    queryKey: ['document', documentId],
    queryFn: async () => {
      return documentsApi.getById(documentId);
    },
    staleTime: 1000 * 60 * 5, // 5 minutes
    gcTime: 1000 * 60 * 10, // 10 minutes
  });
};
