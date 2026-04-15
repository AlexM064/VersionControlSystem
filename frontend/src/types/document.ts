export enum DocumentStatus {
  ACTIVE = 'ACTIVE',
  ARCHIVED = 'ARCHIVED',
}

export interface Document {
  id: number;
  title: string;
  description: string;
  status: DocumentStatus;
  publishedVersionId: number | null;
  ownerUsername: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateDocumentRequest {
  title: string;
  description?: string;
}

export interface UpdateDocumentRequest {
  title: string;
  description?: string;
}

export interface DocumentListResponse {
  content: Document[];
  totalElements: number;
  totalPages: number;
  currentPage: number;
  pageSize: number;
}
