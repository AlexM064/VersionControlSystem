import { useDocumentVersions } from './useDocumentDetails';

export const useVersions = (documentId: number, page = 1, pageSize = 10, enabled = true) => {
  return useDocumentVersions(documentId, page, pageSize, enabled);
};
