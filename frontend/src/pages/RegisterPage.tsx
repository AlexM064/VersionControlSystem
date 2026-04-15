import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useNavigate, Link } from 'react-router-dom';
import { registerSchema } from '@/utils/validators';
import { useAuth } from '@/contexts/AuthContext';
import { Alert, Button } from '@/components/ui';
import { z } from 'zod';
import { useEffect } from 'react';
import logo from '@/assets/logo.svg';

type RegisterFormData = z.infer<typeof registerSchema>;

export const RegisterPage = () => {
  const navigate = useNavigate();
  const { register: registerUser, error: authError, isLoading, isAuthenticated } = useAuth();

  // Redirect to dashboard if already authenticated
  useEffect(() => {
    if (isAuthenticated) {
      navigate('/dashboard', { replace: true });
    }
  }, [isAuthenticated, navigate]);

  const {
    register,
    handleSubmit,
    formState: { errors },
    setError,
  } = useForm<RegisterFormData>({
    resolver: zodResolver(registerSchema),
  });

  const onSubmit = async (data: RegisterFormData) => {
    try {
      await registerUser({
        username: data.username,
        email: data.email,
        password: data.password,
      });
      // Navigation will happen via useEffect above
    } catch (err: any) {
      const errorMessage =
        err.response?.data?.message ||
        err.response?.data?.error ||
        'Registration failed. Please try again.';
      setError('root', { message: errorMessage });
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-slate-900 to-slate-800 px-4 py-8">
      <div className="w-full max-w-md">
        {/* Card */}
        <div className="rounded-xl border border-slate-100 bg-white p-8 shadow-lg dark:border-slate-800 dark:bg-slate-900 dark:shadow-2xl dark:shadow-black/50">
          {/* Logo */}
          <div className="mb-8 text-center">
            <img src={logo} alt="DVCS Logo" className="mx-auto mb-3 h-14 w-auto" />
            <p className="mt-2 text-lg font-semibold tracking-tight bg-gradient-to-r from-blue-500 to-indigo-500 bg-clip-text text-transparent">Document Version Control System</p>
            <p className="mt-1 text-xs uppercase tracking-wider text-slate-500 dark:text-slate-400">Secure Document Workflows</p>
          </div>

          {/* Heading */}
          <h2 className="mb-6 text-center text-2xl font-bold tracking-tight text-slate-900 dark:text-slate-50">Create Account</h2>

          {/* Error Alert */}
          {(authError || errors.root) && (
            <div className="mb-6">
              <Alert
                type="error"
                message={authError || errors.root?.message || 'An error occurred'}
                dismissible={false}
              />
            </div>
          )}

          {/* Form */}
          <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">
            {/* Username */}
            <div>
              <label htmlFor="username" className="block text-sm font-semibold text-slate-700 dark:text-slate-300 mb-2">
                Username
              </label>
              <input
                id="username"
                type="text"
                placeholder="Choose a username"
                className={`w-full px-4 py-2.5 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-150 dark:bg-slate-800 dark:text-slate-50 ${
                  errors.username ? 'border-red-500 dark:border-red-600' : 'border-slate-300 dark:border-slate-600'
                }`}
                {...register('username')}
                disabled={isLoading}
              />
              {errors.username && (
                <p className="text-sm text-red-600 dark:text-red-400 mt-1.5">{errors.username.message}</p>
              )}
            </div>

            {/* Email */}
            <div>
              <label htmlFor="email" className="block text-sm font-semibold text-slate-700 dark:text-slate-300 mb-2">
                Email Address
              </label>
              <input
                id="email"
                type="email"
                placeholder="Enter your email"
                className={`w-full px-4 py-2.5 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-150 dark:bg-slate-800 dark:text-slate-50 ${
                  errors.email ? 'border-red-500 dark:border-red-600' : 'border-slate-300 dark:border-slate-600'
                }`}
                {...register('email')}
                disabled={isLoading}
              />
              {errors.email && (
                <p className="text-sm text-red-600 dark:text-red-400 mt-1.5">{errors.email.message}</p>
              )}
            </div>

            {/* Password */}
            <div>
              <label htmlFor="password" className="block text-sm font-semibold text-slate-700 dark:text-slate-300 mb-2">
                Password
              </label>
              <input
                id="password"
                type="password"
                placeholder="Create a password"
                className={`w-full px-4 py-2.5 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-150 dark:bg-slate-800 dark:text-slate-50 ${
                  errors.password ? 'border-red-500 dark:border-red-600' : 'border-slate-300 dark:border-slate-600'
                }`}
                {...register('password')}
                disabled={isLoading}
              />
              {errors.password && (
                <p className="text-sm text-red-600 dark:text-red-400 mt-1.5">{errors.password.message}</p>
              )}
            </div>

            {/* Confirm Password */}
            <div>
              <label htmlFor="confirmPassword" className="block text-sm font-semibold text-slate-700 dark:text-slate-300 mb-2">
                Confirm Password
              </label>
              <input
                id="confirmPassword"
                type="password"
                placeholder="Confirm your password"
                className={`w-full px-4 py-2.5 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-150 dark:bg-slate-800 dark:text-slate-50 ${
                  errors.confirmPassword ? 'border-red-500 dark:border-red-600' : 'border-slate-300 dark:border-slate-600'
                }`}
                {...register('confirmPassword')}
                disabled={isLoading}
              />
              {errors.confirmPassword && (
                <p className="text-sm text-red-600 dark:text-red-400 mt-1.5">{errors.confirmPassword.message}</p>
              )}
            </div>

            {/* Submit Button */}
            <Button
              type="submit"
              variant="primary"
              size="md"
              loading={isLoading}
              className="w-full mt-6"
            >
              Create Account
            </Button>
          </form>

          {/* Divider */}
          <div className="my-6 flex items-center">
            <div className="flex-1 border-t border-slate-200 dark:border-slate-700"></div>
            <span className="px-3 text-sm text-slate-500 dark:text-slate-400">or</span>
            <div className="flex-1 border-t border-slate-200 dark:border-slate-700"></div>
          </div>

          {/* Login Link */}
          <p className="text-center text-slate-600 dark:text-slate-400 text-sm">
            Already have an account?{' '}
            <Link to="/login" className="text-blue-600 dark:text-blue-400 hover:text-blue-700 dark:hover:text-blue-300 font-semibold transition-colors">
              Sign In
            </Link>
          </p>
        </div>

        {/* Footer */}
        <p className="text-center text-slate-400 dark:text-slate-500 text-xs mt-6">
          © 2026 Document Version Control System. All rights reserved.
        </p>
      </div>
    </div>
  );
};
