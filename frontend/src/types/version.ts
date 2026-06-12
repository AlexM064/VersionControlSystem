export enum VersionStatus {
  DRAFT = 'DRAFT',
  IN_REVIEW = 'IN_REVIEW',
  APPROVED = 'APPROVED',
  REJECTED = 'REJECTED',
  PUBLISHED = 'PUBLISHED',
}

export interface DocumentVersion {
  id: number;
  documentId: number;
  versionNumber: number;
  content: string;
  message: string;
  status: VersionStatus;
  createdByUsername: string;
  createdAt: string;
}

export interface CreateVersionRequest {
  content: string;
  message?: string;
}

// Backend /submit endpoint doesn't accept request body

export interface VersionListResponse {
  content: DocumentVersion[];
  totalElements: number;
  totalPages: number;
  currentPage: number;
  pageSize: number;
}

export interface VersionComparisonRequest {
  leftVersionId: number;
  rightVersionId: number;
}

export interface VersionComparisonResponse {
  documentId: number;
  leftVersionId: number;
  leftVersionNumber: number;
  leftContent: string;
  rightVersionId: number;
  rightVersionNumber: number;
  rightContent: string;
  identical: boolean;
}
