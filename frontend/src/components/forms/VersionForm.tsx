import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useNavigate } from 'react-router-dom';
import { z } from 'zod';
import { DocumentVersion } from '@/types/version';
import { Alert, Button } from '@/components/ui';
import { AlertCircle } from 'lucide-react';
import { createVersionSchema } from '@/utils/validators';
import { useToast } from '@/contexts/ToastContext';

interface VersionFormProps {
  documentTitle: string;
  isLoading?: boolean;
  error?: string | null;
  onSubmit: (data: z.infer<typeof createVersionSchema>) => Promise<DocumentVersion>;
}

export const VersionForm = ({
  documentTitle,
  isLoading = false,
  error: externalError = null,
  onSubmit,
}: VersionFormProps) => {
  const navigate = useNavigate();
  const toast = useToast();
  const [internalError, setInternalError] = useState('');

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<z.infer<typeof createVersionSchema>>({
    resolver: zodResolver(createVersionSchema),
    defaultValues: {
      content: '',
      message: '',
    },
  });

  const handleFormSubmit = async (data: z.infer<typeof createVersionSchema>) => {
    try {
      setInternalError('');

      const result = await onSubmit(data);

      toast.success('Version created successfully.');

      // Navigate back to document details
      navigate(`/documents/${result.documentId}`);
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
      toast.error(errorMessage, 'Unable to create version');
    }
  };

  const displayError = externalError || internalError;
  const isSubmittingForm = isSubmitting || isLoading;

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h1 className="text-3xl font-bold text-slate-900 dark:text-slate-50">Create New Version</h1>
        <p className="text-slate-600 dark:text-slate-400 mt-1">
          Add a new version to <span className="font-semibold">"{documentTitle}"</span>
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
          {/* Content Field */}
          <div>
            <label htmlFor="content" className="block text-sm font-semibold text-slate-700 dark:text-slate-300 mb-2">
              Content <span className="text-red-600">*</span>
            </label>
            <textarea
              id="content"
              placeholder="Enter the version content..."
              rows={20}
              disabled={isSubmittingForm}
              className={`w-full px-4 py-2.5 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent focus-visible:outline-none transition-all duration-150 dark:bg-slate-800 dark:text-slate-50 resize-none font-mono text-sm ${
                errors.content ? 'border-red-500 dark:border-red-600' : 'border-slate-300 dark:border-slate-600'
              }`}
              {...register('content')}
            />
            {errors.content && (
              <p className="mt-1 text-sm text-red-600 flex items-center gap-1">
                <AlertCircle size={14} />
                {errors.content.message}
              </p>
            )}
            <p className="mt-1 text-xs text-slate-500 dark:text-slate-400">Maximum 100,000 characters</p>
          </div>

          {/* Message Field */}
          <div>
            <label htmlFor="message" className="block text-sm font-semibold text-slate-700 dark:text-slate-300 mb-2">
              Version Message
            </label>
            <input
              id="message"
              type="text"
              placeholder="e.g., Fixed bug in calculation logic"
              disabled={isSubmittingForm}
              className={`w-full px-4 py-2.5 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent focus-visible:outline-none transition-all duration-150 dark:bg-slate-800 dark:text-slate-50 ${
                errors.message ? 'border-red-500 dark:border-red-600' : 'border-slate-300 dark:border-slate-600'
              }`}
              {...register('message')}
            />
            {errors.message && (
              <p className="mt-1 text-sm text-red-600 flex items-center gap-1">
                <AlertCircle size={14} />
                {errors.message.message}
              </p>
            )}
            <p className="mt-1 text-xs text-slate-500 dark:text-slate-400">Optional description of changes (maximum 500 characters)</p>
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
              Create Version
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