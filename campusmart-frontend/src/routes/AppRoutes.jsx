import { Route, Routes, Navigate } from 'react-router-dom';
import MainLayout from '../layouts/MainLayout.jsx';
import HomePage from '../pages/HomePage.jsx';
import ProductListPage from '../pages/ProductListPage.jsx';
import ProductDetailsPage from '../pages/ProductDetailsPage.jsx';
import CategoryPage from '../pages/CategoryPage.jsx';
import WishlistPage from '../pages/WishlistPage.jsx';
import CartPage from '../pages/CartPage.jsx';
import OrdersPage from '../pages/OrdersPage.jsx';
import NotificationsPage from '../pages/NotificationsPage.jsx';
import SellerDashboardPage from '../pages/SellerDashboardPage.jsx';
import MyProductsPage from '../pages/MyProductsPage.jsx';
import AddProductPage from '../pages/AddProductPage.jsx';
import EditProductPage from '../pages/EditProductPage.jsx';
import { LoginPage } from '../pages/LoginPage';
import { RegisterPage } from '../pages/RegisterPage';
import { ProtectedRoute } from '../components/ProtectedRoute';
import { SellerRoute } from '../components/SellerRoute';
import { useAuth } from '../hooks/useAuth';

function AppRoutes() {
  const { isAuthenticated } = useAuth();

  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />

      <Route
        element={
          <ProtectedRoute>
            <MainLayout />
          </ProtectedRoute>
        }
      >
        <Route index element={<HomePage />} />
        <Route path="products" element={<ProductListPage />} />
        <Route path="products/:productId" element={<ProductDetailsPage />} />
        <Route path="categories/:categoryId" element={<CategoryPage />} />
        <Route path="wishlist" element={<WishlistPage />} />
        <Route path="cart" element={<CartPage />} />
        <Route path="orders" element={<OrdersPage />} />
        <Route path="notifications" element={<NotificationsPage />} />
      </Route>

      <Route
        element={
          <SellerRoute>
            <MainLayout />
          </SellerRoute>
        }
      >
        <Route path="seller" element={<SellerDashboardPage />} />
        <Route path="seller/products" element={<MyProductsPage />} />
        <Route path="seller/products/add" element={<AddProductPage />} />
        <Route path="seller/products/:productId/edit" element={<EditProductPage />} />
      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}

export default AppRoutes;

