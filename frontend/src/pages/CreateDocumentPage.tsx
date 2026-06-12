import { DocumentForm } from '@/components/forms/DocumentForm';
import { useCreateDocument } from '@/hooks/useDocumentMutations';
import { usePermission } from '@/hooks';
import { useNavigate } from 'react-router-dom';
import { useEffect } from 'react';
import { createDocumentSchema } from '@/utils/validators';

export const CreateDocumentPage = () => {
  const navigate = useNavigate();
  const { can } = usePermission();
  const { mutateAsync: createDocument, isPending, error } = useCreateDocument();

  // Check permission
  useEffect(() => {
    if (!can('CREATE_DOCUMENT')) {
      navigate('/documents');
    }
  }, [can, navigate]);

  return (
    <div className="space-y-6">
      <DocumentForm
        title="Create Document"
        submitButtonText="Create Document"
        isLoading={isPending}
        error={error?.message}
        schema={createDocumentSchema}
        onSubmit={createDocument}
      />
    </div>
  );
};
