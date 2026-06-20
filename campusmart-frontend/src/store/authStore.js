import { create } from 'zustand';
import { persist } from 'zustand/middleware';

export const useAuthStore = create(
  persist(
    (set, get) => ({
      token: null,
      user: null,
      isAuthenticated: false,
      error: null,
      isLoading: false,

      setToken: (token) => {
        if (token) {
          localStorage.setItem('token', token);
          set({ token, isAuthenticated: true });
        } else {
          localStorage.removeItem('token');
          set({ token: null, isAuthenticated: false });
        }
      },

      setUser: (user) => set({ user }),

      setError: (error) => set({ error }),

      setIsLoading: (isLoading) => set({ isLoading }),

      login: async (email, password) => {
        set({ isLoading: true, error: null });
        try {
          const { default: axiosInstance } = await import('../api/axiosConfig');
          const response = await axiosInstance.post('/api/auth/login', {
            email,
            password,
          });

          const { token, id, firstName, lastName, email: userEmail, roles } = response.data;

          get().setToken(token);
          get().setUser({
            id,
            firstName,
            lastName,
            email: userEmail,
            roles,
          });

          set({ isLoading: false });
          return true;
        } catch (err) {
          const errorMessage = err.response?.data?.message || 'Login failed';
          set({ error: errorMessage, isLoading: false });
          return false;
        }
      },

      register: async (formData) => {
        set({ isLoading: true, error: null });
        try {
          const { default: axiosInstance } = await import('../api/axiosConfig');
          const response = await axiosInstance.post('/api/auth/register', formData);

          const { token, id, firstName, lastName, email, roles } = response.data;

          get().setToken(token);
          get().setUser({
            id,
            firstName,
            lastName,
            email,
            roles,
          });

          set({ isLoading: false });
          return true;
        } catch (err) {
          const errorResponse = err.response?.data;
          const errorMessage = errorResponse?.validationErrors
            ? Object.values(errorResponse.validationErrors).join(' ')
            : errorResponse?.message || 'Registration failed';
          set({ error: errorMessage, isLoading: false });
          return false;
        }
      },

      logout: () => {
        get().setToken(null);
        set({ user: null, error: null });
      },

      clearError: () => set({ error: null }),
    }),
    {
      name: 'auth-storage',
      partialize: (state) => ({
        token: state.token,
        user: state.user,
        isAuthenticated: state.isAuthenticated,
      }),
    }
  )
);
