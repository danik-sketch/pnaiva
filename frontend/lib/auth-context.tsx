"use client"

import { createContext, useContext, useEffect, useState, ReactNode } from "react"

interface User {
  id: number
  username: string
  email: string
}

interface AuthResponse {
  token: string
  userId: number
  username: string
  email: string
}

interface AuthContextType {
  user: User | null
  token: string | null
  isLoading: boolean
  login: (login: string, password: string) => Promise<void>
  register: (username: string, email: string, password: string) => Promise<void>
  logout: () => void
}

const AuthContext = createContext<AuthContextType | undefined>(undefined)

const API_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080"

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null)
  const [token, setToken] = useState<string | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    const storedToken = localStorage.getItem("auth_token")
    const storedUser = localStorage.getItem("auth_user")
    
    if (storedToken && storedUser) {
      setToken(storedToken)
      setUser(JSON.parse(storedUser))
    }
    setIsLoading(false)
  }, [])

  const login = async (loginValue: string, password: string) => {
    const response = await fetch(`${API_URL}/api/auth/login`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ login: loginValue, password }),
    })

    if (!response.ok) {
      const error = await response.json().catch(() => ({ message: "Login failed" }))
      throw new Error(error.message || "Login failed")
    }

    const data: AuthResponse = await response.json()
    
    const userData: User = {
      id: data.userId,
      username: data.username,
      email: data.email,
    }

    setToken(data.token)
    setUser(userData)
    localStorage.setItem("auth_token", data.token)
    localStorage.setItem("auth_user", JSON.stringify(userData))
  }

  const register = async (username: string, email: string, password: string) => {
    const response = await fetch(`${API_URL}/api/auth/register`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ username, email, password }),
    })

    if (!response.ok) {
      const error = await response.json().catch(() => ({ message: "Registration failed" }))
      throw new Error(error.message || "Registration failed")
    }

    const data: AuthResponse = await response.json()
    
    const userData: User = {
      id: data.userId,
      username: data.username,
      email: data.email,
    }

    setToken(data.token)
    setUser(userData)
    localStorage.setItem("auth_token", data.token)
    localStorage.setItem("auth_user", JSON.stringify(userData))
  }

  const logout = () => {
    setToken(null)
    setUser(null)
    localStorage.removeItem("auth_token")
    localStorage.removeItem("auth_user")
  }

  return (
    <AuthContext.Provider value={{ user, token, isLoading, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (context === undefined) {
    throw new Error("useAuth must be used within an AuthProvider")
  }
  return context
}
