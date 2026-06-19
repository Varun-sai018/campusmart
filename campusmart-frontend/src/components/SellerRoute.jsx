import { Navigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';

export const SellerRoute = ({ children }) => {
  const { isAuthenticated, user } = useAuth();
  const isSeller = user?.roles?.includes('SELLER');

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (!isSeller) {
    return <Navigate to="/" replace />;
  }

  return children;
};
