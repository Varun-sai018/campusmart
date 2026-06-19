import axiosInstance from '../api/axiosConfig';

// Auth API endpoints
export const authAPI = {
  login: async (email, password) => {
    const response = await axiosInstance.post('/api/auth/login', {
      email,
      password,
    });
    return response.data;
  },

  register: async (formData) => {
    const response = await axiosInstance.post('/api/auth/register', formData);
    return response.data;
  },
};

// Product API endpoints
export const productAPI = {
  getAllProducts: async (page = 0, size = 20) => {
    const response = await axiosInstance.get('/api/products', {
      params: { page, size },
    });
    return response.data;
  },

  getProductById: async (id) => {
    const response = await axiosInstance.get(`/api/products/${id}`);
    return response.data;
  },

  searchProducts: async (keyword, categoryId, sellerId, page = 0, size = 20) => {
    const response = await axiosInstance.get('/api/products/search', {
      params: { keyword, categoryId, sellerId, page, size },
    });
    return response.data;
  },

  createProduct: async (productData) => {
    const response = await axiosInstance.post('/api/products', productData);
    return response.data;
  },

  updateProduct: async (id, productData) => {
    const response = await axiosInstance.put(`/api/products/${id}`, productData);
    return response.data;
  },

  deleteProduct: async (id) => {
    const response = await axiosInstance.delete(`/api/products/${id}`);
    return response.data;
  },
};

// Category API endpoints
export const categoryAPI = {
  getAllCategories: async () => {
    const response = await axiosInstance.get('/api/categories');
    return response.data;
  },

  getCategoryById: async (id) => {
    const response = await axiosInstance.get(`/api/categories/${id}`);
    return response.data;
  },
};

// Product image API endpoints
export const productImageAPI = {
  getProductImages: async (productId) => {
    const response = await axiosInstance.get(`/api/products/${productId}/images`);
    return response.data;
  },
};

// Order API endpoints
export const orderAPI = {
  createOrder: async () => {
    const response = await axiosInstance.post('/api/orders');
    return response.data;
  },

  getOrders: async () => {
    const response = await axiosInstance.get('/api/orders');
    return response.data;
  },

  getOrderById: async (id) => {
    const response = await axiosInstance.get(`/api/orders/${id}`);
    return response.data;
  },
};

// Cart API endpoints
export const cartAPI = {
  addToCart: async (productId, quantity = 1) => {
    const response = await axiosInstance.post(`/api/cart/${productId}`, {
      quantity,
    });
    return response.data;
  },

  getCart: async () => {
    const response = await axiosInstance.get('/api/cart');
    return response.data;
  },

  updateCartItem: async (productId, quantity) => {
    const response = await axiosInstance.put(`/api/cart/${productId}`, {
      quantity,
    });
    return response.data;
  },

  removeFromCart: async (productId) => {
    const response = await axiosInstance.delete(`/api/cart/${productId}`);
    return response.data;
  },

  clearCart: async () => {
    const response = await axiosInstance.delete('/api/cart');
    return response.data;
  },
};

// Wishlist API endpoints
export const wishlistAPI = {
  addToWishlist: async (productId) => {
    const response = await axiosInstance.post(`/api/wishlist/${productId}`);
    return response.data;
  },

  getWishlist: async () => {
    const response = await axiosInstance.get('/api/wishlist');
    return response.data;
  },

  removeFromWishlist: async (productId) => {
    const response = await axiosInstance.delete(`/api/wishlist/${productId}`);
    return response.data;
  },
};

// Notification API endpoints
export const notificationAPI = {
  getNotifications: async () => {
    const response = await axiosInstance.get('/api/notifications');
    return response.data;
  },

  markAsRead: async (id) => {
    const response = await axiosInstance.post(`/api/notifications/${id}/read`);
    return response.data;
  },
};

// Review API endpoints
export const reviewAPI = {
  createReview: async (productId, reviewData) => {
    const response = await axiosInstance.post(`/api/products/${productId}/reviews`, reviewData);
    return response.data;
  },

  getProductReviews: async (productId) => {
    const response = await axiosInstance.get(`/api/products/${productId}/reviews`);
    return response.data;
  },

  updateReview: async (reviewId, reviewData) => {
    const response = await axiosInstance.put(`/api/reviews/${reviewId}`, reviewData);
    return response.data;
  },

  deleteReview: async (reviewId) => {
    const response = await axiosInstance.delete(`/api/reviews/${reviewId}`);
    return response.data;
  },

  getRatingSummary: async (productId) => {
    const response = await axiosInstance.get(`/api/products/${productId}/rating-summary`);
    return response.data;
  },
};
