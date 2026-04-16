import { useDocumentDetails } from './useDocumentDetails';

export const useDocument = (documentId: number, publishedOnly = false) => {
  return useDocumentDetails(documentId, publishedOnly);
};
