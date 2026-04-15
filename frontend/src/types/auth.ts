export enum UserRole {
  ADMIN = 'ADMIN',
  AUTHOR = 'AUTHOR',
  REVIEWER = 'REVIEWER',
  READER = 'READER',
}

export interface User {
  username: string;
  email: string;
  roles: UserRole[];
}

export interface AuthResponse {
  token: string;
  username: string;
  roles: string[];
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
}

export interface AuthState {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;
}
