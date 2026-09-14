import { createContext, useCallback, useContext, useEffect, useState } from 'react';
import { api, clearTokens, getCurrentUser, setTokens } from './api';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => getCurrentUser());

  useEffect(() => {
    const handleExpired = () => setUser(null);
    window.addEventListener('with-dog:session-expired', handleExpired);
    return () => window.removeEventListener('with-dog:session-expired', handleExpired);
  }, []);

  const login = useCallback(async (email, password) => {
    const data = await api.login({ email, password });
    setTokens(data);
    setUser(getCurrentUser());
    return data;
  }, []);

  const signup = useCallback(async (form) => {
    return api.signup(form);
  }, []);

  const logout = useCallback(async () => {
    try {
      await api.logout();
    } catch {
      // 토큰이 이미 만료됐어도 로컬 상태는 정리한다
    }
    clearTokens();
    setUser(null);
  }, []);

  return <AuthContext.Provider value={{ user, login, signup, logout }}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  return useContext(AuthContext);
}
