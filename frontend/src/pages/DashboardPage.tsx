import { useDashboardStats } from '@/hooks';
import { useDocuments } from '@/hooks/useDocuments';
import { useAuth } from '@/contexts/AuthContext';
import { StatsCard, PageShell, PageHeader, SectionCard, StatePanel, Button, Skeleton } from '@/components/ui';
import { FileText, Archive, CheckCircle, Users, AlertCircle } from 'lucide-react';
import { UserRole } from '@/types/auth';
import { DashboardStats } from '@/hooks/useDashboardStats';

export const DashboardPage = () => {
  const { user } = useAuth();

  const isAdmin = user?.roles?.includes(UserRole.ADMIN);
  const isAuthor = user?.roles?.includes(UserRole.AUTHOR);
  const isReviewer = user?.roles?.includes(UserRole.REVIEWER);
  const isReader = user?.roles?.includes(UserRole.READER);

  const nonReaderStatsQuery = useDashboardStats({ enabled: !isReader });
  const readerDocumentsQuery = useDocuments({
    page: 1,
    pageSize: 1000,
    publishedOnly: true,
    enabled: !!isReader,
  });

  const readerDocuments = readerDocumentsQuery.data?.content ?? [];

  const readerStats: DashboardStats = {
    totalDocuments: readerDocuments.length,
    activeDocuments: 0,
    archivedDocuments: 0,
    publishedDocuments: readerDocuments.length,
    versionsInReview: null,
  };

  const stats = isReader ? readerStats : nonReaderStatsQuery.data;
  const isLoading = isReader ? readerDocumentsQuery.isLoading : nonReaderStatsQuery.isLoading;
  const error = isReader ? readerDocumentsQuery.error : nonReaderStatsQuery.error;

  if (error && !isReader) {
    return (
      <PageShell>
        <div className="space-y-6">
          <PageHeader
            title="Dashboard"
            description="A quick view of documents, review activity, and the actions available to your role."
          />
          <StatePanel
            title="Dashboard data unavailable"
            message="Failed to load dashboard data. Please try again later."
            icon={<AlertCircle className="h-5 w-5" />}
          />
        </div>
      </PageShell>
    );
  }

  return (
    <PageShell>
      <div className="space-y-8">
        <PageHeader
          title="Dashboard"
          description={isReader ? 'Welcome back. You are viewing published content in read-only mode.' : `Welcome back, ${user?.username}! Here's an overview of your document management system.`}
        />

        {/* Stats Grid */}
        <div className="grid grid-cols-1 gap-6 md:grid-cols-2 xl:grid-cols-4">
          {isLoading ? (
            Array.from({ length: isReader ? 2 : 4 }).map((_, index) => (
              <SectionCard key={`stats-skeleton-${index}`}>
                <div className="p-6">
                  <Skeleton className="h-3 w-28" />
                  <Skeleton className="mt-3 h-8 w-20" />
                  <Skeleton className="mt-2 h-3 w-32" />
                </div>
              </SectionCard>
            ))
          ) : (
            <>
              <StatsCard title="Total Documents" value={stats?.totalDocuments ?? 0} subtitle="Across your visible scope" icon={<FileText className="h-8 w-8" />} />
              {!isReader && <StatsCard title="Active Documents" value={stats?.activeDocuments ?? 0} subtitle="Currently editable" icon={<FileText className="h-8 w-8 text-green-600" />} />}
              {!isReader && <StatsCard title="Archived Documents" value={stats?.archivedDocuments ?? 0} subtitle="Stored for reference" icon={<Archive className="h-8 w-8 text-slate-600" />} />}
              <StatsCard title="Published Documents" value={stats?.publishedDocuments ?? 0} subtitle={isReader ? 'Visible to readers' : `${stats?.versionsInReview ?? 0} pending reviews`} icon={<CheckCircle className="h-8 w-8 text-blue-600" />} />
            </>
          )}
        </div>

        {isLoading && (
          <SectionCard>
            <div className="space-y-3 p-6">
              <Skeleton className="h-6 w-52" />
              <Skeleton className="h-4 w-full max-w-2xl" />
              <Skeleton className="h-4 w-full max-w-xl" />
              <div className="flex gap-3 pt-2">
                <Skeleton className="h-9 w-36" />
                <Skeleton className="h-9 w-32" />
              </div>
            </div>
          </SectionCard>
        )}

        {/* Role-specific content */}
        {!isLoading && (isAdmin || isReviewer) && (
          <SectionCard className="bg-gradient-to-br from-blue-50 to-white dark:from-blue-950/20 dark:to-slate-900 dark:border-blue-900/30">
            <div className="flex items-start gap-4 p-6">
              <div className="flex h-12 w-12 shrink-0 items-center justify-center rounded-lg bg-blue-100 text-blue-700 ring-1 ring-blue-200 dark:bg-blue-900/40 dark:text-blue-300 dark:ring-blue-900/50">
                <Users className="h-6 w-6" />
              </div>
              <div className="min-w-0 flex-1">
                <h3 className="text-lg font-semibold text-slate-900 dark:text-slate-50">{isAdmin ? 'Admin Dashboard' : 'Reviewer Dashboard'}</h3>
                <p className="mt-2 max-w-3xl text-sm leading-6 text-slate-600 dark:text-slate-400">
                  {isAdmin
                    ? 'As an administrator, you have full access to manage users, documents, and system settings.'
                    : 'As a reviewer, you can review document versions and approve or reject changes.'}
                </p>
                <div className="mt-4 flex flex-wrap gap-3">
                  {isAdmin && <Button variant="secondary" size="sm" onClick={() => (window.location.href = '/admin/users')} className="bg-white dark:bg-slate-800">Manage Users</Button>}
                  {isAdmin && <Button variant="secondary" size="sm" onClick={() => (window.location.href = '/documents')} className="bg-white dark:bg-slate-800">View Documents</Button>}
                  {isReviewer && <Button variant="secondary" size="sm" onClick={() => (window.location.href = '/review-queue')} className="bg-white dark:bg-slate-800">View Review Queue</Button>}
                </div>
              </div>
            </div>
          </SectionCard>
        )}

        {!isLoading && isAuthor && !isAdmin && !isReviewer && (
          <SectionCard className="bg-gradient-to-br from-green-50 to-white dark:from-green-950/20 dark:to-slate-900 dark:border-green-900/30">
            <div className="flex items-start gap-4 p-6">
              <div className="flex h-12 w-12 shrink-0 items-center justify-center rounded-lg bg-green-100 text-green-700 ring-1 ring-green-200 dark:bg-green-900/40 dark:text-green-300 dark:ring-green-900/50">
                <FileText className="h-6 w-6" />
              </div>
              <div className="min-w-0 flex-1">
                <h3 className="text-lg font-semibold text-slate-900 dark:text-slate-50">Author Dashboard</h3>
                <p className="mt-2 max-w-3xl text-sm leading-6 text-slate-600 dark:text-slate-400">
                  As an author, you can create and manage your documents, submit versions for review, and track their status.
                </p>
                <div className="mt-4 flex flex-wrap gap-3">
                  <Button variant="secondary" size="sm" onClick={() => (window.location.href = '/documents/create')} className="bg-white dark:bg-slate-800">Create Document</Button>
                  <Button variant="secondary" size="sm" onClick={() => (window.location.href = '/documents')} className="bg-white dark:bg-slate-800">View Documents</Button>
                </div>
              </div>
            </div>
          </SectionCard>
        )}

        {!isLoading && (isReader ? (
          <SectionCard className="bg-gradient-to-br from-slate-50 to-white dark:from-slate-800/50 dark:to-slate-900 dark:border-slate-700">
            <div className="p-6">
              <h3 className="text-lg font-semibold tracking-tight text-slate-900 dark:text-slate-50">Read-only access</h3>
              <p className="mt-2 max-w-3xl text-sm leading-6 text-slate-600 dark:text-slate-400">
                You can browse published documents and open details, but workflow actions and review tools are disabled for your role.
              </p>
              <div className="mt-4 flex flex-wrap gap-3">
                <Button variant="secondary" size="sm" onClick={() => (window.location.href = '/documents')} className="bg-white dark:bg-slate-800">
                  Browse Documents
                </Button>
              </div>
            </div>
          </SectionCard>
        ) : (
          <SectionCard>
            <div className="p-6">
              <h3 className="text-lg font-semibold tracking-tight text-slate-900 dark:text-slate-50">Quick Actions</h3>
              <div className="mt-4 grid grid-cols-1 gap-4 md:grid-cols-3">
                {(isAuthor || isAdmin) && (
                  <a href="/documents/create" className="group flex items-center rounded-lg border border-slate-200 bg-white p-4 shadow-sm transition-all duration-150 hover:-translate-y-0.5 hover:border-blue-300 hover:bg-blue-50/50 hover:shadow-md dark:border-slate-700 dark:bg-slate-800/50 dark:hover:border-blue-600/50 dark:hover:bg-blue-950/20">
                    <div className="mr-3 flex h-9 w-9 items-center justify-center rounded-lg bg-blue-100 text-blue-600 dark:bg-blue-900/40 dark:text-blue-300">
                      <FileText className="h-5 w-5" />
                    </div>
                    <div>
                      <h4 className="font-medium text-slate-900 dark:text-slate-50">Create Document</h4>
                      <p className="text-sm text-slate-600 dark:text-slate-400">Start a new document</p>
                    </div>
                  </a>
                )}
                <a href="/documents" className="group flex items-center rounded-lg border border-slate-200 bg-white p-4 shadow-sm transition-all duration-150 hover:-translate-y-0.5 hover:border-green-300 hover:bg-green-50/50 hover:shadow-md dark:border-slate-700 dark:bg-slate-800/50 dark:hover:border-green-600/50 dark:hover:bg-green-950/20">
                  <div className="mr-3 flex h-9 w-9 items-center justify-center rounded-lg bg-green-100 text-green-600 dark:bg-green-900/40 dark:text-green-300">
                    <FileText className="h-5 w-5" />
                  </div>
                  <div>
                    <h4 className="font-medium text-slate-900 dark:text-slate-50">Browse Documents</h4>
                    <p className="text-sm text-slate-600 dark:text-slate-400">View all documents</p>
                  </div>
                </a>
                {(isReviewer || isAdmin) && (
                  <a href="/review-queue" className="group flex items-center rounded-lg border border-slate-200 bg-white p-4 shadow-sm transition-all duration-150 hover:-translate-y-0.5 hover:border-orange-300 hover:bg-orange-50/50 hover:shadow-md dark:border-slate-700 dark:bg-slate-800/50 dark:hover:border-orange-600/50 dark:hover:bg-orange-950/20">
                    <div className="mr-3 flex h-9 w-9 items-center justify-center rounded-lg bg-orange-100 text-orange-600 dark:bg-orange-900/40 dark:text-orange-300">
                      <CheckCircle className="h-5 w-5" />
                    </div>
                    <div>
                      <h4 className="font-medium text-slate-900 dark:text-slate-50">Review Queue</h4>
                      <p className="text-sm text-slate-600 dark:text-slate-400">Review pending versions</p>
                    </div>
                  </a>
                )}
              </div>
            </div>
          </SectionCard>
        ))}

        {/* Empty state */}
        {stats && stats.totalDocuments === 0 && !isLoading && (
          <StatePanel
            title={isReader ? 'No published documents yet' : 'No documents yet'}
            message={isReader ? 'There are no published documents available right now.' : 'Get started by creating your first document.'}
            icon={<FileText className="h-6 w-6" />}
            actions={
              !isReader && (isAuthor || isAdmin) ? (
                <Button onClick={() => (window.location.href = '/documents/create')} size="sm">
                  Create Document
                </Button>
              ) : undefined
            }
          />
        )}
      </div>
    </PageShell>
  );
};
