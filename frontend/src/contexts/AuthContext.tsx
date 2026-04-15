import { createContext, useContext, ReactNode, useState, useCallback, useEffect } from 'react';
import { User, UserRole, LoginRequest, RegisterRequest, AuthResponse } from '@/types/auth';
import { authApi } from '@/api';
import { apiClient } from '@/api/axios';

interface AuthContextType {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;
  login: (credentials: LoginRequest) => Promise<void>;
  register: (data: RegisterRequest) => Promise<void>;
  logout: () => void;
  clearError: () => void;
  hasRole: (role: UserRole | UserRole[]) => boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

const STORAGE_KEYS = {
  TOKEN: 'dvcs_token',
  USER: 'dvcs_user',
};

const isUserRole = (role: string): role is UserRole => {
  return Object.values(UserRole).includes(role as UserRole);
};

const normalizeUser = (candidate: {
  username?: string;
  email?: string;
  roles?: string[];
}): User => ({
  username: candidate.username?.trim() || candidate.email?.trim() || 'User',
  email: candidate.email?.trim() || '',
  roles: (candidate.roles ?? []).filter(isUserRole),
});

const loadStoredUser = (rawUser: string): User => {
  try {
    return normalizeUser(JSON.parse(rawUser) as { username?: string; email?: string; roles?: string[] });
  } catch {
    return { username: 'User', email: '', roles: [] };
  }
};

export interface AuthProviderProps {
  children: ReactNode;
}

export const AuthProvider = ({ children }: AuthProviderProps) => {
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Initialize auth state from localStorage on mount
  useEffect(() => {
    const initializeAuth = async () => {
      try {
        const storedToken = localStorage.getItem(STORAGE_KEYS.TOKEN);
        const storedUser = localStorage.getItem(STORAGE_KEYS.USER);

        if (storedToken && storedUser) {
          // Set token immediately on axios
          apiClient.defaults.headers.common['Authorization'] = `Bearer ${storedToken}`;
          setToken(storedToken);
          setUser(loadStoredUser(storedUser));

          // Verify token is still valid by calling /auth/me
          try {
            const currentUser = await authApi.getCurrentUser();
            setUser(normalizeUser(currentUser));
          } catch (err) {
            // Token expired or invalid, clear it
            localStorage.removeItem(STORAGE_KEYS.TOKEN);
            localStorage.removeItem(STORAGE_KEYS.USER);
            delete apiClient.defaults.headers.common['Authorization'];
            setToken(null);
            setUser(null);
          }
        }
      } catch (err) {
        console.error('Failed to initialize auth:', err);
        // Clear any invalid data
        localStorage.removeItem(STORAGE_KEYS.TOKEN);
        localStorage.removeItem(STORAGE_KEYS.USER);
      } finally {
        setIsLoading(false);
      }
    };

    initializeAuth();
  }, []);

  const login = useCallback(async (credentials: LoginRequest) => {
    setIsLoading(true);
    setError(null);
    try {
      const response: AuthResponse = await authApi.login(credentials);
      const { token: newToken, username, roles } = response;

      // Set token immediately on axios
      apiClient.defaults.headers.common['Authorization'] = `Bearer ${newToken}`;
      setToken(newToken);

      const responseUser: User = normalizeUser({ username, roles });
      setUser(responseUser);

      // Persist to localStorage
      localStorage.setItem(STORAGE_KEYS.TOKEN, newToken);
      localStorage.setItem(STORAGE_KEYS.USER, JSON.stringify(responseUser));

      try {
        const currentUser = await authApi.getCurrentUser();
        const resolvedUser = normalizeUser(currentUser);
        setUser(resolvedUser);
        localStorage.setItem(STORAGE_KEYS.USER, JSON.stringify(resolvedUser));
      } catch {
        // Keep the login response user if /auth/me is unavailable.
      }
    } catch (err: any) {
      const errorMessage =
        err.response?.data?.message ||
        err.response?.data?.error ||
        err.message ||
        'Login failed';
      setError(errorMessage);
      throw err;
    } finally {
      setIsLoading(false);
    }
  }, []);

  const register = useCallback(async (data: RegisterRequest) => {
    setIsLoading(true);
    setError(null);
    try {
      const response: AuthResponse = await authApi.register(data);
      const { token: newToken, username, roles } = response;

      // Set token immediately on axios
      apiClient.defaults.headers.common['Authorization'] = `Bearer ${newToken}`;
      setToken(newToken);
      const userData = normalizeUser({ username, email: data.email, roles });
      setUser(userData);

      // Persist to localStorage
      localStorage.setItem(STORAGE_KEYS.TOKEN, newToken);
      localStorage.setItem(STORAGE_KEYS.USER, JSON.stringify(userData));

      try {
        const currentUser = await authApi.getCurrentUser();
        const resolvedUser = normalizeUser(currentUser);
        setUser(resolvedUser);
        localStorage.setItem(STORAGE_KEYS.USER, JSON.stringify(resolvedUser));
      } catch {
        // Keep the register response user if /auth/me is unavailable.
      }
    } catch (err: any) {
      const errorMessage =
        err.response?.data?.message ||
        err.response?.data?.error ||
        err.message ||
        'Registration failed';
      setError(errorMessage);
      throw err;
    } finally {
      setIsLoading(false);
    }
  }, []);

  const logout = useCallback(() => {
    setUser(null);
    setToken(null);
    setError(null);
    localStorage.removeItem(STORAGE_KEYS.TOKEN);
    localStorage.removeItem(STORAGE_KEYS.USER);
    delete apiClient.defaults.headers.common['Authorization'];
  }, []);

  const clearError = useCallback(() => {
    setError(null);
  }, []);

  const hasRole = useCallback(
    (role: UserRole | UserRole[]): boolean => {
      if (!user) return false;
      if (Array.isArray(role)) {
        return role.some((r) => user.roles.includes(r));
      }
      return user.roles.includes(role);
    },
    [user]
  );

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        isAuthenticated: !!token && !!user,
        isLoading,
        error,
        login,
        register,
        logout,
        clearError,
        hasRole,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
