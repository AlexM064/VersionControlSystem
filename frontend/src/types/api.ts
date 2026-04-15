export interface ApprovalResponse {
  id: number;
  versionId: number;
  reviewerId: number;
  decision: 'APPROVED' | 'REJECTED';
  comment: string | null;
  decidedAt: string;
}

export interface PublishRollbackResponse {
  documentId: number;
  title: string;
  status: 'ACTIVE' | 'ARCHIVED';
  publishedVersionId: number;
  publishedVersionNumber: number;
}

export interface ApprovalRequest {
  comment?: string;
}

export interface ApiError {
  status: number;
  message: string;
  timestamp?: string;
  path?: string;
}

export interface DocumentHistory {
  versionId: number;
  versionNumber: number;
  message: string | null;
  createdAt: string;
  published: boolean;
}

export interface PaginationParams {
  page?: number;
  pageSize?: number;
  sortBy?: string;
  sortOrder?: 'ASC' | 'DESC';
}

export interface ApiResponse<T> {
  data: T;
  meta?: {
    currentPage: number;
    totalPages: number;
    totalElements: number;
    pageSize: number;
  };
  message?: string;
}
