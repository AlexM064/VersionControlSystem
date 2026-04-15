import { z } from 'zod';

export const loginSchema = z.object({
  username: z
    .string()
    .min(1, 'Username is required')
    .min(3, 'Username must be at least 3 characters'),
  password: z
    .string()
    .min(1, 'Password is required')
    .min(6, 'Password must be at least 6 characters'),
});

export const registerSchema = z
  .object({
    username: z
      .string()
      .min(1, 'Username is required')
      .min(3, 'Username must be at least 3 characters')
      .max(50, 'Username must not exceed 50 characters'),
    email: z
      .string()
      .min(1, 'Email is required')
      .email('Please enter a valid email'),
    password: z
      .string()
      .min(1, 'Password is required')
      .min(6, 'Password must be at least 6 characters'),
    confirmPassword: z.string().min(1, 'Please confirm your password'),
  })
  .refine((data) => data.password === data.confirmPassword, {
    message: "Passwords don't match",
    path: ['confirmPassword'],
  });

export const createDocumentSchema = z.object({
  title: z
    .string()
    .min(1, 'Title is required')
    .min(3, 'Title must be at least 3 characters')
    .max(255, 'Title must not exceed 255 characters'),
  description: z
    .string()
    .max(5000, 'Description must not exceed 5000 characters')
    .optional(),
});

export const updateDocumentSchema = z.object({
  title: z
    .string()
    .min(1, 'Title is required')
    .min(3, 'Title must be at least 3 characters')
    .max(255, 'Title must not exceed 255 characters'),
  description: z
    .string()
    .max(5000, 'Description must not exceed 5000 characters')
    .optional(),
});

export const createVersionSchema = z.object({
  content: z
    .string()
    .min(1, 'Content is required')
    .max(100000, 'Content is too large'),
  message: z.string().max(500, 'Message must not exceed 500 characters').optional(),
});

export const approvalActionSchema = z.object({
  comment: z.string().max(1000, 'Comment must not exceed 1000 characters').optional(),
});

export type LoginFormData = z.infer<typeof loginSchema>;
export type RegisterFormData = z.infer<typeof registerSchema>;
export type CreateDocumentFormData = z.infer<typeof createDocumentSchema>;
export type UpdateDocumentFormData = z.infer<typeof updateDocumentSchema>;
export type CreateVersionFormData = z.infer<typeof createVersionSchema>;
export type ApprovalActionFormData = z.infer<typeof approvalActionSchema>;
