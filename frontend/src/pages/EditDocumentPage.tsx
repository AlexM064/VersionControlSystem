import { useParams, useNavigate } from 'react-router-dom';
import { useEffect } from 'react';
import { DocumentForm } from '@/components/forms/DocumentForm';
import { useGetDocument, useUpdateDocument } from '@/hooks/useDocumentMutations';
import { usePermission } from '@/hooks';
import { Alert } from '@/components/ui';
import { updateDocumentSchema } from '@/utils/validators';

export const EditDocumentPage = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { can } = usePermission();
  const documentId = parseInt(id || '0', 10);

  const { data: document, isLoading: isLoadingDocument, error: loadError } = useGetDocument(documentId);
  const { mutateAsync: updateDocument, isPending, error: updateError } = useUpdateDocument(documentId);

  // Check permission
  useEffect(() => {
    if (!can('EDIT_DOCUMENT')) {
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
      <div className="flex justify-center items-center min-h-screen">
        <div className="animate-spin">
          <div className="h-8 w-8 border-4 border-slate-300 dark:border-slate-600 border-t-blue-600 rounded-full" />
        </div>
      </div>
    );
  }

  if (loadError || !document) {
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

  return (
    <div className="space-y-6">
      <DocumentForm
        initialData={document}
        title="Edit Document"
        submitButtonText="Update Document"
        isLoading={isPending}
        error={updateError?.message}
        schema={updateDocumentSchema}
        onSubmit={updateDocument}
      />
    </div>
  );
};
