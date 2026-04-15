import { useParams, useNavigate } from 'react-router-dom';
import { useEffect } from 'react';
import { useCreateVersion } from '@/hooks/useVersionMutations';
import { usePermission } from '@/hooks';
import { useGetDocument } from '@/hooks/useDocumentMutations';
import { Alert } from '@/components/ui';
import { VersionForm } from '@/components/forms/VersionForm';

export const CreateVersionPage = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { can } = usePermission();
  const documentId = parseInt(id || '0', 10);

  const { data: document, isLoading: isLoadingDocument, error: loadError } = useGetDocument(documentId);
  const { mutateAsync: createVersion, isPending, error: createError } = useCreateVersion(documentId);

  // Check permission
  useEffect(() => {
    if (!can('CREATE_VERSION')) {
      navigate('/documents');
    }
  }, [can, navigate]);

  if (!documentId || isNaN(documentId)) {
    return (
      <div className="space-y-4">
        <Alert
          type="error"
          title="Invalid Document"
          message="Document ID is missing or invalid"
          dismissible={false}
        />
      </div>
    );
  }

  if (isLoadingDocument) {
    return (
      <div className="space-y-6">
        <div className="flex items-center justify-center py-12">
          <div className="animate-spin">
            <div className="h-8 w-8 border-4 border-slate-300 dark:border-slate-600 border-t-blue-600 rounded-full" />
          </div>
        </div>
      </div>
    );
  }

  if (loadError) {
    return (
      <div className="space-y-4">
        <Alert
          type="error"
          title="Failed to load document"
          message={loadError instanceof Error ? loadError.message : 'Document not found'}
          dismissible={false}
        />
      </div>
    );
  }

  if (!document) {
    return (
      <div className="space-y-4">
        <Alert
          type="error"
          title="Document not found"
          message="The document you're trying to create a version for does not exist"
          dismissible={false}
        />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <VersionForm
        documentTitle={document.title}
        isLoading={isPending}
        error={createError?.message}
        onSubmit={createVersion}
      />
    </div>
  );
};
