import { useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams, useSearchParams } from 'react-router-dom';
import { Alert, Button } from '@/components/ui';
import { useDocumentDetails } from '@/hooks/useDocumentDetails';
import { useDocumentVersionList, useCompareVersions } from '@/hooks';

export const CompareVersionsPage = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();

  const documentId = parseInt(id || '0', 10);
  const [leftVersionId, setLeftVersionId] = useState<number>(Number(searchParams.get('leftVersionId') || 0));
  const [rightVersionId, setRightVersionId] = useState<number>(Number(searchParams.get('rightVersionId') || 0));

  const { data: document, isLoading: isLoadingDocument, error: documentError } = useDocumentDetails(documentId);
  const { data: versionsData, isLoading: isLoadingVersions, error: versionsError } = useDocumentVersionList(documentId);

  const versions = useMemo(() => {
    return [...(versionsData?.content || [])].sort((left, right) => left.versionNumber - right.versionNumber);
  }, [versionsData?.content]);

  useEffect(() => {
    if (versions.length < 2) {
      return;
    }

    const hasLeft = versions.some((version) => version.id === leftVersionId);
    const hasRight = versions.some((version) => version.id === rightVersionId);

    if (!hasLeft || !hasRight) {
      const fallbackLeft = versions[Math.max(0, versions.length - 2)]?.id ?? versions[0].id;
      const fallbackRight = versions[versions.length - 1].id;

      setLeftVersionId(fallbackLeft);
      setRightVersionId(fallbackRight);
      setSearchParams(
        {
          leftVersionId: String(fallbackLeft),
          rightVersionId: String(fallbackRight),
        },
        { replace: true }
      );
    }
  }, [leftVersionId, rightVersionId, searchParams, setSearchParams, versions]);

  const comparison = useCompareVersions(documentId, leftVersionId, rightVersionId);

  if (!documentId || isNaN(documentId)) {
    return <Alert type="error" title="Invalid Document" message="Document ID is missing or invalid" dismissible={false} />;
  }

  if (isLoadingDocument || isLoadingVersions) {
    return (
      <div className="flex justify-center items-center min-h-screen">
        <div className="animate-spin">
          <div className="h-8 w-8 border-4 border-slate-300 dark:border-slate-600 border-t-blue-600 rounded-full" />
        </div>
      </div>
    );
  }

  if (documentError || !document) {
    return (
      <div className="space-y-4 p-8">
        <Alert
          type="error"
          title="Failed to load document"
          message={documentError instanceof Error ? documentError.message : 'Document not found'}
          dismissible={false}
        />
      </div>
    );
  }

  if (versionsError) {
    return (
      <div className="space-y-4 p-8">
        <Alert
          type="error"
          title="Failed to load versions"
          message={versionsError instanceof Error ? versionsError.message : 'Could not load document versions'}
          dismissible={false}
        />
      </div>
    );
  }

  const leftVersion = versions.find((version) => version.id === leftVersionId);
  const rightVersion = versions.find((version) => version.id === rightVersionId);

  const compareDisabled = !leftVersionId || !rightVersionId;

  return (
    <div className="space-y-6">
      <div className="flex items-start justify-between gap-4 flex-wrap">
        <div>
          <button
            type="button"
            onClick={() => navigate(`/documents/${documentId}`)}
            className="text-sm text-blue-600 hover:text-blue-700 mb-2 transition-colors duration-150"
          >
            ← Back to document
          </button>
          <h1 className="text-3xl font-bold tracking-tight text-slate-900 dark:text-slate-50">Compare Versions</h1>
          <p className="text-slate-600 dark:text-slate-400 mt-1">{document.title}</p>
        </div>
      </div>

      <div className="bg-white dark:bg-slate-900 rounded-lg border border-slate-200 dark:border-slate-700 p-6 space-y-6 shadow-sm">
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
          <div>
            <label htmlFor="leftVersion" className="block text-sm font-semibold text-slate-700 dark:text-slate-300 mb-2">
              Left Version
            </label>
            <select
              id="leftVersion"
              value={leftVersionId}
              onChange={(event) => setLeftVersionId(Number(event.target.value))}
              className="w-full rounded-lg border border-slate-300 dark:border-slate-600 px-4 py-2.5 bg-white dark:bg-slate-800 text-slate-900 dark:text-slate-50 focus:border-blue-500 focus:ring-2 focus:ring-blue-500 transition-colors duration-150"
            >
              <option value={0}>Select version</option>
              {versions.map((version) => (
                <option key={version.id} value={version.id}>
                  v{version.versionNumber} - #{version.id}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label htmlFor="rightVersion" className="block text-sm font-semibold text-slate-700 dark:text-slate-300 mb-2">
              Right Version
            </label>
            <select
              id="rightVersion"
              value={rightVersionId}
              onChange={(event) => setRightVersionId(Number(event.target.value))}
              className="w-full rounded-lg border border-slate-300 dark:border-slate-600 px-4 py-2.5 bg-white dark:bg-slate-800 text-slate-900 dark:text-slate-50 focus:border-blue-500 focus:ring-2 focus:ring-blue-500 transition-colors duration-150"
            >
              <option value={0}>Select version</option>
              {versions.map((version) => (
                <option key={version.id} value={version.id}>
                  v{version.versionNumber} - #{version.id}
                </option>
              ))}
            </select>
          </div>
        </div>

        <div className="flex flex-wrap gap-3">
          <Button
            type="button"
            variant="primary"
            onClick={() => setSearchParams({ leftVersionId: String(leftVersionId), rightVersionId: String(rightVersionId) })}
            disabled={compareDisabled}
          >
            Compare Selected Versions
          </Button>
          <Button
            type="button"
            variant="secondary"
            onClick={() => {
              if (versions.length >= 2) {
                const fallbackLeft = versions[Math.max(0, versions.length - 2)].id;
                const fallbackRight = versions[versions.length - 1].id;
                setLeftVersionId(fallbackLeft);
                setRightVersionId(fallbackRight);
                setSearchParams({ leftVersionId: String(fallbackLeft), rightVersionId: String(fallbackRight) });
              }
            }}
            disabled={versions.length < 2}
          >
            Compare Latest Versions
          </Button>
        </div>

        {compareDisabled && (
          <Alert
            type="info"
            title="Select two versions"
            message="Choose two different versions to compare them side by side."
            dismissible={false}
          />
        )}

        {comparison.isError && (
          <Alert
            type="error"
            title="Comparison failed"
            message={comparison.error instanceof Error ? comparison.error.message : 'Unable to compare versions'}
            dismissible={false}
          />
        )}

        {comparison.data && (
          <div className="space-y-6">
            {comparison.data.identical && (
              <Alert
                type="info"
                title="Versions are identical"
                message="The selected versions contain the same content."
                dismissible={false}
              />
            )}

            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
              <section className="rounded-lg border border-slate-200 dark:border-slate-700 bg-slate-50 dark:bg-slate-800/40">
                <div className="border-b border-slate-200 dark:border-slate-700 px-4 py-3 bg-white dark:bg-slate-900 rounded-t-lg">
                  <p className="text-xs uppercase tracking-wide text-slate-500 dark:text-slate-400">Left Version</p>
                  <h2 className="text-lg font-semibold text-slate-900 dark:text-slate-50">
                    v{comparison.data.leftVersionNumber}
                    <span className="ml-2 text-sm font-normal text-slate-500 dark:text-slate-400">#{comparison.data.leftVersionId}</span>
                  </h2>
                </div>
                <div className="max-h-[70vh] overflow-auto p-4">
                  <pre className="whitespace-pre-wrap break-words font-mono text-sm leading-6 text-slate-800 dark:text-slate-200">
                    {comparison.data.leftContent || 'No content'}
                  </pre>
                </div>
              </section>

              <section className="rounded-lg border border-slate-200 dark:border-slate-700 bg-slate-50 dark:bg-slate-800/40">
                <div className="border-b border-slate-200 dark:border-slate-700 px-4 py-3 bg-white dark:bg-slate-900 rounded-t-lg">
                  <p className="text-xs uppercase tracking-wide text-slate-500 dark:text-slate-400">Right Version</p>
                  <h2 className="text-lg font-semibold text-slate-900 dark:text-slate-50">
                    v{comparison.data.rightVersionNumber}
                    <span className="ml-2 text-sm font-normal text-slate-500 dark:text-slate-400">#{comparison.data.rightVersionId}</span>
                  </h2>
                </div>
                <div className="max-h-[70vh] overflow-auto p-4">
                  <pre className="whitespace-pre-wrap break-words font-mono text-sm leading-6 text-slate-800 dark:text-slate-200">
                    {comparison.data.rightContent || 'No content'}
                  </pre>
                </div>
              </section>
            </div>

            <div className="text-sm text-slate-600 dark:text-slate-400">
              {leftVersion && rightVersion ? (
                <span>
                  Comparing <span className="font-medium">v{leftVersion.versionNumber}</span> and{' '}
                  <span className="font-medium">v{rightVersion.versionNumber}</span>
                </span>
              ) : null}
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
