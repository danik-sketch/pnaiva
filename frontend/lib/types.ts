// API Response Types based on Java DTOs

export interface ReminderResponseDto {
  id: number;
  reminderTime: string; // ISO-8601 LocalDateTime
  message: string;
  taskId: number;
}

export interface TaskResponseDto {
  id: number;
  title: string;
  description: string;
  dueDate: string; // ISO-8601 LocalDateTime
  completed: string | null; // ISO-8601 LocalDateTime
  projectName: string | null;
  categories: string[];
  reminders: ReminderResponseDto[];
}

export interface ProjectResponseDto {
  id: number;
  name: string;
  description: string;
  userId: number;
  username: string;
  tasks: TaskResponseDto[];
}

export interface CategoryResponseDto {
  id: number;
  title: string;
  tasks: string[]; // Task titles
}

// API Request Types

export interface ReminderRequestDto {
  reminderTime: string; // ISO-8601 LocalDateTime
  message: string;
  taskId?: number;
}

export interface TaskRequestDto {
  title: string;
  description?: string;
  dueDate: string; // ISO-8601 LocalDateTime
  completed?: string | null;
  projectId?: number;
  categoryIds?: number[];
  reminders?: ReminderRequestDto[];
}

export interface ProjectRequestDto {
  name: string;
  description?: string;
  userId: number;
  tasks?: TaskRequestDto[];
}

export interface CategoryRequestDto {
  title: string;
}

// Paginated Response
export interface PageResponse<T> {
  content: T[];
  pageable: {
    pageNumber: number;
    pageSize: number;
  };
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

// Filter types
export interface TaskFilters {
  title?: string;
  description?: string;
  dueDate?: string;
  completed?: boolean;
}
