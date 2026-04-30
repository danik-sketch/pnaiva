import type {
  TaskResponseDto,
  TaskRequestDto,
  ProjectResponseDto,
  ProjectRequestDto,
  CategoryResponseDto,
  CategoryRequestDto,
  ReminderResponseDto,
  ReminderRequestDto,
  PageResponse,
  TaskFilters,
} from "./types";

// API Base URL - change this to your backend URL
const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";

class ApiError extends Error {
  constructor(public status: number, message: string) {
    super(message);
    this.name = "ApiError";
  }
}

function getAuthToken(): string | null {
  if (typeof window !== "undefined") {
    return localStorage.getItem("auth_token");
  }
  return null;
}

async function fetchApi<T>(
  endpoint: string,
  options: RequestInit = {}
): Promise<T> {
  const url = `${API_BASE_URL}${endpoint}`;
  const token = getAuthToken();
  
  const headers: HeadersInit = {
    "Content-Type": "application/json",
    ...options.headers,
  };
  
  if (token) {
    (headers as Record<string, string>)["Authorization"] = `Bearer ${token}`;
  }
  
  const response = await fetch(url, {
    ...options,
    headers,
  });

  if (!response.ok) {
    throw new ApiError(response.status, `API Error: ${response.statusText}`);
  }

  // Handle 204 No Content
  if (response.status === 204) {
    return undefined as T;
  }

  return response.json();
}

// Tasks API
export const tasksApi = {
  getAll: (
    filters?: TaskFilters,
    page = 0,
    size = 10
  ): Promise<PageResponse<TaskResponseDto>> => {
    const params = new URLSearchParams();
    params.append("page", page.toString());
    params.append("size", size.toString());
    
    if (filters?.title) params.append("title", filters.title);
    if (filters?.description) params.append("description", filters.description);
    if (filters?.dueDate) params.append("dueDate", filters.dueDate);
    if (filters?.completed !== undefined) params.append("completed", filters.completed.toString());
    
    return fetchApi(`/api/tasks?${params.toString()}`);
  },

  getById: (id: number): Promise<TaskResponseDto> => {
    return fetchApi(`/api/tasks/${id}`);
  },

  create: (task: TaskRequestDto): Promise<TaskResponseDto> => {
    return fetchApi("/api/tasks", {
      method: "POST",
      body: JSON.stringify(task),
    });
  },

  update: (id: number, task: TaskRequestDto): Promise<TaskResponseDto> => {
    return fetchApi(`/api/tasks/${id}`, {
      method: "PUT",
      body: JSON.stringify(task),
    });
  },

  delete: (id: number): Promise<void> => {
    return fetchApi(`/api/tasks/${id}`, {
      method: "DELETE",
    });
  },
};

// Projects API
export const projectsApi = {
  getAll: (page = 0, size = 10): Promise<PageResponse<ProjectResponseDto>> => {
    return fetchApi(`/api/projects?page=${page}&size=${size}`);
  },

  getById: (id: number): Promise<ProjectResponseDto> => {
    return fetchApi(`/api/projects/${id}`);
  },

  search: (params: {
    projectName?: string;
    taskTitle?: string;
    completed?: boolean;
    dueFrom?: string;
    dueTo?: string;
    page?: number;
    size?: number;
  }): Promise<PageResponse<ProjectResponseDto>> => {
    const searchParams = new URLSearchParams();
    if (params.projectName) searchParams.append("projectName", params.projectName);
    if (params.taskTitle) searchParams.append("taskTitle", params.taskTitle);
    if (params.completed !== undefined) searchParams.append("completed", params.completed.toString());
    if (params.dueFrom) searchParams.append("dueFrom", params.dueFrom);
    if (params.dueTo) searchParams.append("dueTo", params.dueTo);
    searchParams.append("page", (params.page || 0).toString());
    searchParams.append("size", (params.size || 10).toString());
    
    return fetchApi(`/api/projects/search?${searchParams.toString()}`);
  },

  create: (project: ProjectRequestDto): Promise<ProjectResponseDto> => {
    return fetchApi("/api/projects", {
      method: "POST",
      body: JSON.stringify(project),
    });
  },

  update: (id: number, project: ProjectRequestDto): Promise<ProjectResponseDto> => {
    return fetchApi(`/api/projects/${id}`, {
      method: "PUT",
      body: JSON.stringify(project),
    });
  },

  delete: (id: number): Promise<void> => {
    return fetchApi(`/api/projects/${id}`, {
      method: "DELETE",
    });
  },
};

// Categories API
export const categoriesApi = {
  getAll: (): Promise<CategoryResponseDto[]> => {
    return fetchApi("/api/categories");
  },

  getById: (id: number): Promise<CategoryResponseDto> => {
    return fetchApi(`/api/categories/${id}`);
  },

  create: (category: CategoryRequestDto): Promise<CategoryResponseDto> => {
    return fetchApi("/api/categories", {
      method: "POST",
      body: JSON.stringify(category),
    });
  },

  update: (id: number, category: CategoryRequestDto): Promise<CategoryResponseDto> => {
    return fetchApi(`/api/categories/${id}`, {
      method: "PUT",
      body: JSON.stringify(category),
    });
  },

  delete: (id: number): Promise<void> => {
    return fetchApi(`/api/categories/${id}`, {
      method: "DELETE",
    });
  },
};

// Reminders API
export const remindersApi = {
  getAll: (): Promise<ReminderResponseDto[]> => {
    return fetchApi("/api/reminders");
  },

  getById: (id: number): Promise<ReminderResponseDto> => {
    return fetchApi(`/api/reminders/${id}`);
  },

  create: (reminder: ReminderRequestDto): Promise<ReminderResponseDto> => {
    return fetchApi("/api/reminders", {
      method: "POST",
      body: JSON.stringify(reminder),
    });
  },

  update: (id: number, reminder: ReminderRequestDto): Promise<ReminderResponseDto> => {
    return fetchApi(`/api/reminders/${id}`, {
      method: "PUT",
      body: JSON.stringify(reminder),
    });
  },

  delete: (id: number): Promise<void> => {
    return fetchApi(`/api/reminders/${id}`, {
      method: "DELETE",
    });
  },
};

// SWR fetchers
export const fetchers = {
  tasks: (url: string) => fetchApi<PageResponse<TaskResponseDto>>(url),
  projects: (url: string) => fetchApi<PageResponse<ProjectResponseDto>>(url),
  categories: (url: string) => fetchApi<CategoryResponseDto[]>(url),
  reminders: (url: string) => fetchApi<ReminderResponseDto[]>(url),
};
