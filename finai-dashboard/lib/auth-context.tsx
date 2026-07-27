"use client";

import { createContext, useContext, useEffect, useState, ReactNode } from "react";
import { AuthResponse, LoginRequest, RegisterRequest, User } from "@/lib/types";
import { clearToken, getStoredUserId, getStoredUsername, getToken } from "@/lib/api";
import { loginAction, logoutAction, registerAction } from "@/app/actions/auth";

interface AuthContextValue {
  token: string | null;
  userId: number | null;
  username: string | null;
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (payload: LoginRequest) => Promise<AuthResponse>;
  register: (payload: RegisterRequest) => Promise<AuthResponse>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setTokenState] = useState<string | null>(null);
  const [userId, setUserId] = useState<number | null>(null);
  const [username, setUsername] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const t = getToken();
    const uid = getStoredUserId();
    const uname = getStoredUsername();
    setTokenState(t);
    setUserId(uid);
    setUsername(uname);
    setIsLoading(false);
  }, []);

  const login = async (payload: LoginRequest): Promise<AuthResponse> => {
    const response = await loginAction(payload);
    setTokenState(response.token);
    setUserId(response.userId);
    setUsername(response.username);
    return response;
  };

  const register = async (payload: RegisterRequest): Promise<AuthResponse> => {
    const response = await registerAction(payload);
    setTokenState(response.token);
    setUserId(response.userId);
    setUsername(response.username);
    return response;
  };

  const logout = () => {
    clearToken();
    logoutAction();
    setTokenState(null);
    setUserId(null);
    setUsername(null);
  };

  const value: AuthContextValue = {
    token,
    userId,
    username,
    user: null,
    isAuthenticated: !!token,
    isLoading,
    login,
    register,
    logout,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
}
