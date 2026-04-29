// API Types based on Spring Boot backend

export interface User {
  id: number;
  username: string;
  email: string;
}

export interface Category {
  id: number;
  name: string;
}

export interface Reminder {
  id: number;
  remindAt: string;
  sent: boolean;
}

export interface Task {
  id: number;
  title: string;
  description: string;
  dueDate: string;
  completed: boolean;
  projectName: string;
  categories: Category[];
  reminders: Reminder[];
}

export interface Project {
  id: number;
  name: string;
  description: string;
  userId: number;
  tasks: Task[];
}

// Request DTOs
export interface LoginRequest {
  login: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
}

export interface TaskRequest {
  title: string;
  description: string;
  dueDate: string;
  completed: boolean;
  projectId: number;
  categoryIds: number[];
}

export interface ProjectRequest {
  name: string;
  description: string;
}

export interface CategoryRequest {
  name: string;
}

// Response DTOs
export interface AuthResponse {
  accessToken: string;
  tokenType: string;
}

export interface PageResponse<T> {
  content: T[];
  pageable: {
    pageNumber: number;
    pageSize: number;
  };
  totalElements: number;
  totalPages: number;
  last: boolean;
  first: boolean;
}

// Filter types
export interface TaskFilters {
  title?: string;
  dueDate?: string;
  completed?: boolean;
}

export interface ProjectFilters {
  name?: string;
}
