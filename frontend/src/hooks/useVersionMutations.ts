import { useMutation, useQueryClient } from '@tanstack/react-query';
import { versionsApi } from '@/api';
import { CreateVersionRequest } from '@/types/version';

const invalidateVersionQueries = async (queryClient: ReturnType<typeof useQueryClient>, documentId: number) => {
  await queryClient.invalidateQueries({ queryKey: ['document', documentId] });
  await queryClient.invalidateQueries({ queryKey: ['documentVersions', documentId] });
  await queryClient.invalidateQueries({ queryKey: ['publishedVersion', documentId] });
};

export const useCreateVersion = (documentId: number) => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (data: CreateVersionRequest) => {
      return versionsApi.create(documentId, data);
    },
    onSuccess: async () => {
      await invalidateVersionQueries(queryClient, documentId);
    },
  });
};

export const useSubmitVersion = (documentId: number) => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (versionId: number) => {
      return versionsApi.submit(documentId, versionId);
    },
    onSuccess: async () => {
      await invalidateVersionQueries(queryClient, documentId);
    },
  });
};

export const useApproveVersion = (documentId: number) => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (versionId: number) => {
      return versionsApi.approve(versionId);
    },
    onSuccess: async () => {
      await invalidateVersionQueries(queryClient, documentId);
    },
  });
};

export const useRejectVersion = (documentId: number) => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (versionId: number) => {
      return versionsApi.reject(versionId);
    },
    onSuccess: async () => {
      await invalidateVersionQueries(queryClient, documentId);
    },
  });
};

export const usePublishVersion = (documentId: number) => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (versionId: number) => {
      return versionsApi.publish(versionId);
    },
    onSuccess: async () => {
      await invalidateVersionQueries(queryClient, documentId);
    },
  });
};

export const useRollbackVersion = (documentId: number) => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (versionId: number) => {
      return versionsApi.rollback(versionId);
    },
    onSuccess: async () => {
      await invalidateVersionQueries(queryClient, documentId);
    },
  });
};