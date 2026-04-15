import { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useNavigate } from 'react-router-dom';
import { z } from 'zod';
import { Document } from '@/types/document';
import { Alert, Button } from '@/components/ui';
import { AlertCircle } from 'lucide-react';
import { createDocumentSchema, updateDocumentSchema } from '@/utils/validators';
import { useToast } from '@/contexts/ToastContext';

interface DocumentFormProps<T extends z.ZodSchema> {
  initialData?: Document;
  isLoading?: boolean;
  error?: string | null;
  schema: T;
  onSubmit: (data: z.infer<T>) => Promise<Document>;
  title: string;
  submitButtonText?: string;
}

type FormData = z.infer<typeof createDocumentSchema | typeof updateDocumentSchema>;

export const DocumentForm = <T extends z.ZodSchema>({
  initialData,
  isLoading = false,
  error: externalError = null,
  schema,
  onSubmit,
  title,
  submitButtonText = 'Create Document',
}: DocumentFormProps<T>) => {
  const navigate = useNavigate();
  const toast = useToast();
  const [internalError, setInternalError] = useState('');

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
    setValue,
  } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: {
      title: initialData?.title || '',
      description: initialData?.description || '',
    },
  });

  // Pre-fill form when in edit mode
  useEffect(() => {
    if (initialData) {
      setValue('title', initialData.title);
      setValue('description', initialData.description);
    }
  }, [initialData, setValue]);

  const handleFormSubmit = async (data: FormData) => {
    try {
      setInternalError('');

      const result = await onSubmit(data);

      toast.success(initialData ? 'Document updated successfully.' : 'Document created successfully.');

      // Navigate immediately to document details
      navigate(`/documents/${result.id}`);
    } catch (err: any) {
      // Handle backend validation errors
      let errorMessage = 'An error occurred';

      if (err.response?.data) {
        const errorData = err.response.data;

        // Handle structured validation errors
        if (errorData.errors && Array.isArray(errorData.errors)) {
          errorMessage = errorData.errors.map((error: any) => error.message || error).join(', ');
        } else if (errorData.message) {
          errorMessage = errorData.message;
        } else if (errorData.error) {
          errorMessage = errorData.error;
        } else if (typeof errorData === 'string') {
          errorMessage = errorData;
        }
      } else if (err.message) {
        errorMessage = err.message;
      }

      setInternalError(errorMessage);
      toast.error(errorMessage, 'Unable to save document');
    }
  };

  const displayError = externalError || internalError;
  const isSubmittingForm = isSubmitting || isLoading;

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h1 className="text-3xl font-bold text-slate-900 dark:text-slate-50">{title}</h1>
        <p className="text-slate-600 dark:text-slate-400 mt-1">
          {initialData
            ? 'Update document details'
            : 'Create a new document and add versions'}
        </p>
      </div>

      {/* Error Alert */}
      {displayError && (
        <Alert
          type="error"
          title="Error"
          message={displayError}
          onClose={() => setInternalError('')}
          dismissible={true}
        />
      )}

      {/* Form Card */}
      <div className="bg-white dark:bg-slate-900 rounded-lg border border-slate-200 dark:border-slate-700 p-6 md:p-8 shadow-sm">
        <form onSubmit={handleSubmit(handleFormSubmit)} className="space-y-6">
          {/* Title Field */}
          <div>
            <label htmlFor="title" className="block text-sm font-semibold text-slate-700 dark:text-slate-300 mb-2">
              Title <span className="text-red-600">*</span>
            </label>
            <input
              id="title"
              type="text"
              placeholder="e.g., Project Charter 2026"
              disabled={isSubmittingForm}
              className={`w-full px-4 py-2.5 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent focus-visible:outline-none transition-all duration-150 dark:bg-slate-800 dark:text-slate-50 ${
                errors.title ? 'border-red-500 dark:border-red-600' : 'border-slate-300 dark:border-slate-600'
              }`}
              {...register('title')}
            />
            {errors.title && (
              <p className="mt-1 text-sm text-red-600 flex items-center gap-1">
                <AlertCircle size={14} />
                {errors.title.message}
              </p>
            )}
            <p className="mt-1 text-xs text-slate-500 dark:text-slate-400">Maximum 255 characters</p>
          </div>

          {/* Description Field */}
          <div>
            <label htmlFor="description" className="block text-sm font-semibold text-slate-700 dark:text-slate-300 mb-2">
              Description
            </label>
            <textarea
              id="description"
              placeholder="Describe the purpose and scope of this document..."
              rows={8}
              disabled={isSubmittingForm}
              className={`w-full px-4 py-2.5 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent focus-visible:outline-none transition-all duration-150 dark:bg-slate-800 dark:text-slate-50 resize-none ${
                errors.description ? 'border-red-500 dark:border-red-600' : 'border-slate-300 dark:border-slate-600'
              }`}
              {...register('description')}
            />
            {errors.description && (
              <p className="mt-1 text-sm text-red-600 flex items-center gap-1">
                <AlertCircle size={14} />
                {errors.description.message}
              </p>
            )}
            <p className="mt-1 text-xs text-slate-500 dark:text-slate-400">Maximum 5000 characters</p>
          </div>

          {/* Form Actions */}
          <div className="flex gap-3 pt-6 border-t border-slate-200 dark:border-slate-700">
            <Button
              type="submit"
              variant="primary"
              size="md"
              loading={isSubmittingForm}
              disabled={isSubmittingForm}
            >
              {submitButtonText}
            </Button>
            <Button
              type="button"
              variant="secondary"
              size="md"
              onClick={() => navigate(-1)}
              disabled={isSubmittingForm}
            >
              Cancel
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
};
