import type {
  AuthResponse,
  LoginRequest,
  RegisterRequest,
  Project,
  ProjectRequest,
  Task,
  TaskRequest,
  Category,
  CategoryRequest,
  PageResponse,
  TaskFilters,
  ProjectFilters,
} from "./types";

const API_BASE = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080/api";

class ApiClient {
  private token: string | null = null;

  constructor() {
    if (typeof window !== "undefined") {
      this.token = localStorage.getItem("token");
    }
  }

  setToken(token: string | null) {
    this.token = token;
    if (typeof window !== "undefined") {
      if (token) {
        localStorage.setItem("token", token);
      } else {
        localStorage.removeItem("token");
      }
    }
  }

  getToken() {
    return this.token;
  }

  private async request<T>(
    endpoint: string,
    options: RequestInit = {}
  ): Promise<T> {
    const headers: HeadersInit = {
      "Content-Type": "application/json",
      ...options.headers,
    };

    if (this.token) {
      (headers as Record<string, string>)["Authorization"] = `Bearer ${this.token}`;
    }

    const response = await fetch(`${API_BASE}${endpoint}`, {
      ...options,
      headers,
    });

    if (!response.ok) {
      const error = await response.text();
      throw new Error(error || `HTTP error! status: ${response.status}`);
    }

    if (response.status === 204) {
      return undefined as T;
    }

    return response.json();
  }

  // Auth
  async login(data: LoginRequest): Promise<AuthResponse> {
    console.log("[v0] Login request to:", `${API_BASE}/auth/login`);
    console.log("[v0] Login data:", JSON.stringify(data));
    const response = await this.request<AuthResponse>("/auth/login", {
      method: "POST",
      body: JSON.stringify(data),
    });
    console.log("[v0] Login response:", response);
    this.setToken(response.token);
    return response;
  }

  async register(data: RegisterRequest): Promise<AuthResponse> {
    const response = await this.request<AuthResponse>("/auth/register", {
      method: "POST",
      body: JSON.stringify(data),
    });
    this.setToken(response.token);
    return response;
  }

  logout() {
    this.setToken(null);
  }

  // Projects
  async getProjects(
    page = 0,
    size = 10,
    filters?: ProjectFilters
  ): Promise<PageResponse<Project>> {
    const params = new URLSearchParams({
      page: String(page),
      size: String(size),
    });
    if (filters?.name) params.append("name", filters.name);
    return this.request<PageResponse<Project>>(`/projects?${params}`);
  }

  async getProject(id: number): Promise<Project> {
    return this.request<Project>(`/projects/${id}`);
  }

  async createProject(data: ProjectRequest): Promise<Project> {
    return this.request<Project>("/projects", {
      method: "POST",
      body: JSON.stringify(data),
    });
  }

  async updateProject(id: number, data: ProjectRequest): Promise<Project> {
    return this.request<Project>(`/projects/${id}`, {
      method: "PUT",
      body: JSON.stringify(data),
    });
  }

  async deleteProject(id: number): Promise<void> {
    return this.request<void>(`/projects/${id}`, {
      method: "DELETE",
    });
  }

  // Tasks
  async getTasks(
    page = 0,
    size = 10,
    filters?: TaskFilters
  ): Promise<PageResponse<Task>> {
    const params = new URLSearchParams({
      page: String(page),
      size: String(size),
    });
    if (filters?.title) params.append("title", filters.title);
    if (filters?.dueDate) params.append("dueDate", filters.dueDate);
    if (filters?.completed !== undefined)
      params.append("completed", String(filters.completed));
    return this.request<PageResponse<Task>>(`/tasks?${params}`);
  }

  async getTask(id: number): Promise<Task> {
    return this.request<Task>(`/tasks/${id}`);
  }

  async createTask(data: TaskRequest): Promise<Task> {
    return this.request<Task>("/tasks", {
      method: "POST",
      body: JSON.stringify(data),
    });
  }

  async updateTask(id: number, data: TaskRequest): Promise<Task> {
    return this.request<Task>(`/tasks/${id}`, {
      method: "PUT",
      body: JSON.stringify(data),
    });
  }

  async deleteTask(id: number): Promise<void> {
    return this.request<void>(`/tasks/${id}`, {
      method: "DELETE",
    });
  }

  // Categories
  async getCategories(): Promise<Category[]> {
    return this.request<Category[]>("/categories");
  }

  async createCategory(data: CategoryRequest): Promise<Category> {
    return this.request<Category>("/categories", {
      method: "POST",
      body: JSON.stringify(data),
    });
  }

  async updateCategory(id: number, data: CategoryRequest): Promise<Category> {
    return this.request<Category>(`/categories/${id}`, {
      method: "PUT",
      body: JSON.stringify(data),
    });
  }

  async deleteCategory(id: number): Promise<void> {
    return this.request<void>(`/categories/${id}`, {
      method: "DELETE",
    });
  }
}

export const api = new ApiClient();
