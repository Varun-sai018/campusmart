# Frontend Sprint 1 - Quick Reference Guide

## 🚀 Quick Start

```bash
# Install dependencies
cd campusmart-frontend
npm install

# Configure environment
# Create .env with:
# VITE_API_BASE_URL=http://localhost:8080

# Run dev server
npm run dev

# Build for production
npm run build
```

---

## 📂 File Locations Reference

```
Auth State         → src/store/authStore.js
Auth Hook          → src/hooks/useAuth.js
API Config         → src/api/axiosConfig.js
Validation Utils   → src/utils/validation.js
API Services       → src/services/api.js

Navbar             → src/components/Navbar.jsx
Protected Route    → src/components/ProtectedRoute.jsx

Login Page         → src/pages/LoginPage.jsx
Register Page      → src/pages/RegisterPage.jsx

Routes Config      → src/routes/AppRoutes.jsx
Main App           → src/App.jsx
```

---

## 🎯 Import Examples

### Using Auth Store
```javascript
import { useAuthStore } from '@/store/authStore';

const { token, user, isAuthenticated, login, logout, register } = useAuthStore();
```

### Using Auth Hook (Recommended)
```javascript
import { useAuth } from '@/hooks/useAuth';

const { user, login, logout } = useAuth();
```

### Using API Services
```javascript
import { authAPI, productAPI, cartAPI } from '@/services/api';

await authAPI.login(email, password);
await productAPI.getAllProducts(0, 20);
await cartAPI.addToCart(productId, quantity);
```

---

## 📋 API Endpoints Quick Reference

### Authentication
```
POST /api/auth/login      → { email, password }
POST /api/auth/register   → { firstName, lastName, email, password, phoneNumber, roles }
```

### Products
```
GET    /api/products                          → Get all products
GET    /api/products/:id                      → Get product by ID
GET    /api/products/search                   → Search products
POST   /api/products                          → Create product
PUT    /api/products/:id                      → Update product
DELETE /api/products/:id                      → Delete product
```

### Cart
```
POST   /api/cart                              → Add to cart
GET    /api/cart                              → Get cart
PUT    /api/cart/:productId                   → Update item quantity
DELETE /api/cart/:productId                   → Remove item
DELETE /api/cart                              → Clear cart
```

### Orders
```
POST   /api/orders                            → Create order
GET    /api/orders/buyer                      → Get buyer orders
GET    /api/orders/seller                     → Get seller orders
GET    /api/orders/:id                        → Get order details
```

### Wishlist
```
POST   /api/wishlist                          → Add to wishlist
GET    /api/wishlist                          → Get wishlist
DELETE /api/wishlist/:productId               → Remove from wishlist
```

### Notifications
```
GET    /api/notifications                     → Get notifications
PUT    /api/notifications/:id                 → Mark as read
```

### Reviews
```
POST   /api/products/:id/reviews              → Create review
GET    /api/products/:id/reviews              → Get product reviews
PUT    /api/reviews/:id                       → Update review
DELETE /api/reviews/:id                       → Delete review
```

---

## 🔐 JWT Token Management

### How Token is Used
1. User logs in → Backend returns JWT token
2. Token stored in localStorage automatically
3. Axios interceptor adds to all requests: `Authorization: Bearer <token>`
4. On 401 response → Token cleared, user redirected to login
5. On page reload → Token retrieved from localStorage

### Manual Token Management
```javascript
// Get token
const token = localStorage.getItem('token');

// Clear token
localStorage.removeItem('token');

// Using logout
const { logout } = useAuth();
logout(); // Clears token and user data
```

---

## 🎨 Form Validation Rules

### Login
```
Email:    Must be valid email format
Password: Minimum 6 characters
```

### Register
```
First Name: 2-50 characters
Last Name:  2-50 characters
Email:      Valid email format
Password:   Minimum 6 characters
Phone:      10 digits (auto-formatted)
Roles:      At least one selected
```

---

## 🛣️ Route Structure

```
/login      - Public login page
/register   - Public register page
/           - Protected home page (requires auth)
```

### Protected Route Example
```jsx
<Route path="/dashboard" element={
  <ProtectedRoute>
    <DashboardPage />
  </ProtectedRoute>
} />
```

---

## 💡 Common Code Patterns

### Using useAuth in Component
```jsx
import { useAuth } from '@/hooks/useAuth';

export const MyComponent = () => {
  const { user, isAuthenticated, logout, error } = useAuth();

  if (!isAuthenticated) {
    return <p>Please login</p>;
  }

  return (
    <div>
      Welcome {user?.firstName}
      <button onClick={logout}>Logout</button>
    </div>
  );
};
```

### Calling API
```jsx
import { productAPI } from '@/services/api';

const [products, setProducts] = useState([]);

useEffect(() => {
  const fetchProducts = async () => {
    try {
      const data = await productAPI.getAllProducts(0, 20);
      setProducts(data.content);
    } catch (error) {
      console.error('Error fetching products:', error);
    }
  };
  fetchProducts();
}, []);
```

### Form Validation
```jsx
import { validateLoginForm } from '@/utils/validation';

const handleSubmit = (e) => {
  e.preventDefault();
  const errors = validateLoginForm(email, password);
  
  if (Object.keys(errors).length > 0) {
    setErrors(errors);
    return;
  }
  
  // Submit form
};
```

---

## 🧪 Testing Credentials

After registering, use:
```
Email:    yourname@example.com
Password: your_password_123
Roles:    BUYER, SELLER (or both)
```

---

## 📱 Responsive Breakpoints

```
Mobile:   < 576px  (col-md-* not applied)
Tablet:   576px+   (col-md-* applied)
Desktop:  768px+   (col-lg-* applied)
```

---

## 🔧 Environment Variables

### Development
```env
VITE_API_BASE_URL=http://localhost:8080
```

### Production
```env
VITE_API_BASE_URL=https://api.campusmart.com
```

---

## 📦 Dependencies Installed

```json
{
  "react": "^19.1.0",
  "react-dom": "^19.1.0",
  "react-router-dom": "^7.6.2",
  "axios": "^1.10.0",
  "bootstrap": "^5.3.6",
  "zustand": "^4.4.0"
}
```

---

## ✅ Component Checklist

- [x] Navbar.jsx - Navigation with auth states
- [x] ProtectedRoute.jsx - Route protection
- [x] LoginPage.jsx - Login form
- [x] RegisterPage.jsx - Register form
- [x] authStore.js - State management
- [x] axiosConfig.js - API client
- [x] useAuth.js - Custom hook
- [x] validation.js - Form validation
- [x] api.js - API services

---

## 🐛 Debugging Tips

### Check Auth State
```javascript
// Open browser console
console.log(localStorage.getItem('token'));
console.log(localStorage.getItem('auth-storage')); // Zustand persist
```

### Check Network Requests
```
1. Open DevTools → Network tab
2. Look for API calls
3. Check Authorization header in Request Headers
4. Check Response status and body
```

### Check Redux DevTools (Zustand)
```javascript
// Install: npm install zustand-devtools
// Then view store state in browser DevTools
```

---

## 🚨 Troubleshooting

### Not Authenticated After Login
- [ ] Check network request status code (200?)
- [ ] Check Response contains token
- [ ] Check localStorage has token
- [ ] Check browser console for errors

### Forms Not Submitting
- [ ] Check form validation errors
- [ ] Check console for JavaScript errors
- [ ] Check network request in DevTools
- [ ] Check backend is running

### Token Not Persisting
- [ ] Check localStorage is enabled
- [ ] Check VITE_API_BASE_URL is correct
- [ ] Check page reload
- [ ] Check browser private mode

---

## 📚 Additional Resources

- **Zustand Docs**: https://github.com/pmndrs/zustand
- **React Router Docs**: https://reactrouter.com/
- **Axios Docs**: https://axios-http.com/
- **Bootstrap 5 Docs**: https://getbootstrap.com/docs/5.0/

---

## 🎯 Next Steps

1. **Install Dependencies**: `npm install`
2. **Configure .env**: Set VITE_API_BASE_URL
3. **Start Dev Server**: `npm run dev`
4. **Test Login Flow**: Try registration → login
5. **Explore Code**: Review component files
6. **Build Next Feature**: Start Sprint 2 tasks

---

## 📞 Need Help?

1. Check `AUTH_SETUP.md` for detailed documentation
2. Review component source code comments
3. Check browser console for error messages
4. Verify backend API is running on correct port

---

*Last Updated: 2026-06-19*  
*Sprint 1 Complete ✅*
