"use client";

import {
  createContext,
  useContext,
  useState,
  useEffect,
  type ReactNode,
} from "react";
import { api } from "./api";
import type { LoginRequest, RegisterRequest } from "./types";

interface AuthContextType {
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (data: LoginRequest) => Promise<void>;
  register: (data: RegisterRequest) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    // Проверяем сохранённый токен при инициализации
    if (typeof window !== "undefined") {
      const token = localStorage.getItem("token");
      if (token) {
        api.setToken(token);
        setIsAuthenticated(true);
      }
    }
    setIsLoading(false);
  }, []);

  const login = async (data: LoginRequest) => {
    await api.login(data);
    setIsAuthenticated(true);
  };

  const register = async (data: RegisterRequest) => {
    await api.register(data);
    setIsAuthenticated(true);
  };

  const logout = () => {
    api.logout();
    setIsAuthenticated(false);
  };

  return (
    <AuthContext.Provider
      value={{ isAuthenticated, isLoading, login, register, logout }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
}
