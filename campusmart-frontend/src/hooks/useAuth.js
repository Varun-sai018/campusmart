import { useAuthStore } from '../store/authStore';

export const useAuth = () => {
  const {
    token,
    user,
    isAuthenticated,
    error,
    isLoading,
    login,
    logout,
    register,
    setError,
    clearError,
  } = useAuthStore();

  return {
    token,
    user,
    isAuthenticated,
    error,
    isLoading,
    login,
    logout,
    register,
    setError,
    clearError,
  };
};
