export interface User {
  id: number;
  username: string;
  email: string;
}

export interface Category {
  id: number;
  name: string;
  tasks?: string[];
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
  username: string;
  tasks: Task[];
}

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
  title: string;
}

export interface AuthResponse {
  token: string;
  userId: number;
  username: string;
  email: string;
}
export interface ReminderResponseDto {
  id: number;
  message: string;
  reminderTime: string;
  taskId: number;
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

export interface TaskFilters {
  title?: string;
  dueDate?: string;
  completed?: boolean;
}

export interface ProjectFilters {
  name?: string;
}

export interface Reminder {
  id: number;
  taskId: number;
  message: string;
  reminderTime: string;
}