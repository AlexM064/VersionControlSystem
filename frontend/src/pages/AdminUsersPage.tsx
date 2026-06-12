import { FormEvent, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useMutation } from '@tanstack/react-query';
import { Alert, Button } from '@/components/ui';
import { usersApi } from '@/api';
import { UserRole } from '@/types/auth';
import { usePermission } from '@/hooks';
import { useToast } from '@/contexts/ToastContext';

export const AdminUsersPage = () => {
  const navigate = useNavigate();
  const { can } = usePermission();
  const toast = useToast();

  const [userId, setUserId] = useState('');
  const [role, setRole] = useState<UserRole>(UserRole.AUTHOR);
  const [successMessage, setSuccessMessage] = useState('');
  const [errorMessage, setErrorMessage] = useState('');

  useEffect(() => {
    if (!can('MANAGE_USERS')) {
      navigate('/documents');
    }
  }, [can, navigate]);

  const changeRoleMutation = useMutation({
    mutationFn: async ({ targetUserId, nextRole }: { targetUserId: number; nextRole: UserRole }) => {
      return usersApi.changeUserRole(targetUserId, nextRole);
    },
    onSuccess: (_, variables) => {
      setErrorMessage('');
      const message = `User #${variables.targetUserId} role updated to ${variables.nextRole}.`;
      setSuccessMessage(message);
      toast.success(message, 'Role updated');
      setUserId('');
    },
    onError: (error: any) => {
      setSuccessMessage('');
      const message =
        error?.response?.data?.message ||
        error?.response?.data?.error ||
        error?.message ||
        'Failed to update user role.';
      setErrorMessage(message);
      toast.error(message, 'Update failed');
    },
  });

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    const parsedUserId = Number(userId);

    if (!userId || !Number.isInteger(parsedUserId) || parsedUserId <= 0) {
      setSuccessMessage('');
      setErrorMessage('Please enter a valid positive user ID.');
      return;
    }

    await changeRoleMutation.mutateAsync({ targetUserId: parsedUserId, nextRole: role });
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold text-slate-900 dark:text-slate-50">User Management</h1>
        <p className="text-slate-600 dark:text-slate-400 mt-1">Change user roles by user ID</p>
      </div>

      {successMessage && (
        <Alert
          type="success"
          title="Role Updated"
          message={successMessage}
          onClose={() => setSuccessMessage('')}
        />
      )}

      {errorMessage && (
        <Alert
          type="error"
          title="Update Failed"
          message={errorMessage}
          onClose={() => setErrorMessage('')}
        />
      )}

      <div className="bg-white dark:bg-slate-900 rounded-lg border border-slate-200 dark:border-slate-700 p-6 md:p-8 max-w-2xl shadow-sm">
        <form onSubmit={handleSubmit} className="space-y-6">
          <div>
            <label htmlFor="userId" className="block text-sm font-semibold text-slate-700 dark:text-slate-300 mb-2">
              User ID
            </label>
            <input
              id="userId"
              type="number"
              min={1}
              step={1}
              value={userId}
              onChange={(event) => setUserId(event.target.value)}
              className="w-full px-4 py-2.5 border border-slate-300 dark:border-slate-600 dark:bg-slate-800 dark:text-slate-50 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-150"
              placeholder="e.g., 42"
              disabled={changeRoleMutation.isPending}
            />
          </div>

          <div>
            <label htmlFor="role" className="block text-sm font-semibold text-slate-700 dark:text-slate-300 mb-2">
              Role
            </label>
            <select
              id="role"
              value={role}
              onChange={(event) => setRole(event.target.value as UserRole)}
              className="w-full px-4 py-2.5 border border-slate-300 dark:border-slate-600 dark:bg-slate-800 dark:text-slate-50 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-150"
              disabled={changeRoleMutation.isPending}
            >
              <option value={UserRole.ADMIN}>ADMIN</option>
              <option value={UserRole.AUTHOR}>AUTHOR</option>
              <option value={UserRole.REVIEWER}>REVIEWER</option>
              <option value={UserRole.READER}>READER</option>
            </select>
          </div>

          <div className="flex gap-3 pt-2">
            <Button
              type="submit"
              variant="primary"
              size="md"
              loading={changeRoleMutation.isPending}
              disabled={changeRoleMutation.isPending}
            >
              Update Role
            </Button>
            <Button
              type="button"
              variant="secondary"
              size="md"
              disabled={changeRoleMutation.isPending}
              onClick={() => {
                setUserId('');
                setRole(UserRole.AUTHOR);
                setErrorMessage('');
                setSuccessMessage('');
              }}
            >
              Reset
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
};
