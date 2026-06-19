# CampusMart Frontend - Sprint 1: Authentication Module

## Overview
This document describes the complete authentication module implementation for CampusMart frontend, including scalable folder structure, components, state management, and API integration.

---

## Folder Structure

```
campusmart-frontend/
├── src/
│   ├── api/
│   │   ├── axiosConfig.js          # Axios instance with JWT interceptor
│   │   └── httpClient.js           # Legacy HTTP client (optional)
│   │
│   ├── components/
│   │   ├── Navbar.jsx              # Navigation bar with auth state
│   │   ├── ProtectedRoute.jsx       # Route protection component
│   │   └── ...other components
│   │
│   ├── hooks/
│   │   ├── useAuth.js              # Custom hook for auth context
│   │   └── ...other hooks
│   │
│   ├── layouts/
│   │   ├── MainLayout.jsx          # Main layout wrapper
│   │   └── ...other layouts
│   │
│   ├── pages/
│   │   ├── LoginPage.jsx           # Login page with form validation
│   │   ├── RegisterPage.jsx        # Register page with form validation
│   │   ├── HomePage.jsx            # Home page (protected)
│   │   └── ...other pages
│   │
│   ├── routes/
│   │   └── AppRoutes.jsx           # Route configuration
│   │
│   ├── services/
│   │   └── api.js                  # API endpoints wrapper
│   │
│   ├── store/
│   │   └── authStore.js            # Zustand auth store
│   │
│   ├── utils/
│   │   └── validation.js           # Form validation utilities
│   │
│   ├── App.jsx                     # Main app component
│   ├── main.jsx                    # Vite entry point
│   └── styles.css                  # Global styles
│
├── .env                            # Environment variables
├── .env.example                    # Environment template
├── package.json                    # Dependencies configuration
└── vite.config.js                  # Vite configuration
```

---

## Installation & Setup

### 1. Install Dependencies

```bash
cd campusmart-frontend
npm install
```

This installs all required packages including the newly added Zustand:
- React 19
- Vite
- React Router 7
- Axios
- Bootstrap 5
- Zustand

### 2. Configure Environment Variables

Create `.env` file in the project root:

```env
VITE_API_BASE_URL=http://localhost:8080
```

For production:
```env
VITE_API_BASE_URL=https://api.campusmart.com
```

### 3. Run Development Server

```bash
npm run dev
```

Access at: `http://localhost:5173`

---

## Component Architecture

### 1. Auth Store (Zustand)

**Location:** `src/store/authStore.js`

**Features:**
- Centralized state management for authentication
- Automatic localStorage persistence
- Async login/register actions
- Error handling
- Loading states

**API:**
```javascript
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
  clearError 
} = useAuthStore();
```

**Usage:**
```javascript
import { useAuthStore } from '@/store/authStore';

const MyComponent = () => {
  const { user, logout } = useAuthStore();
  
  return <div>Hello {user?.firstName}</div>;
};
```

---

### 2. Axios Configuration

**Location:** `src/api/axiosConfig.js`

**Features:**
- Automatic JWT token injection in headers
- Base URL from environment variables
- Response interceptor for 401 handling
- Automatic redirect to login on token expiration

**Request Interceptor:**
```javascript
// Automatically adds: Authorization: Bearer <token>
```

**Response Interceptor:**
```javascript
// On 401: Clears token and redirects to /login
```

---

### 3. Protected Route Component

**Location:** `src/components/ProtectedRoute.jsx`

**Purpose:** Restricts access to authenticated routes

**Usage:**
```jsx
<Route path="/dashboard" element={
  <ProtectedRoute>
    <DashboardPage />
  </ProtectedRoute>
} />
```

---

### 4. Navigation Bar

**Location:** `src/components/Navbar.jsx`

**Features:**
- Conditional rendering based on auth state
- Guest view: Login, Register buttons
- Authenticated view: User name, Profile link, Logout button
- Bootstrap styling
- Mobile responsive with hamburger menu

---

### 5. Login Page

**Location:** `src/pages/LoginPage.jsx`

**Features:**
- Email and password fields
- Form validation
- Error display
- Loading state with spinner
- Link to register page
- Auto-redirect if already authenticated
- Bootstrap responsive design

**Validation:**
- Email format validation
- Password minimum 6 characters
- Real-time error clearing on input

---

### 6. Register Page

**Location:** `src/pages/RegisterPage.jsx`

**Features:**
- First Name, Last Name fields
- Email and password fields
- Phone number with formatting (auto-formats to XXX-XXX-XXXX)
- Role selection (BUYER, SELLER)
- Form validation
- Error display
- Loading state with spinner
- Link to login page
- Auto-redirect if already authenticated
- Bootstrap responsive design

**Role Selection:**
- Multiple roles can be selected
- Both BUYER and SELLER roles can be selected
- Sent as array to backend

---

## Route Structure

```
/login                 # Login page (public)
/register              # Register page (public)
/                      # Home page (protected)
/*                     # Redirect to home
```

---

## API Integration

### Authentication Endpoints

**POST /api/auth/login**
```javascript
Request: { email, password }
Response: { 
  token, 
  id, 
  firstName, 
  lastName, 
  email, 
  roles 
}
```

**POST /api/auth/register**
```javascript
Request: { 
  firstName, 
  lastName, 
  email, 
  password, 
  phoneNumber, 
  roles: ["BUYER", "SELLER"] 
}
Response: { 
  token, 
  id, 
  firstName, 
  lastName, 
  email, 
  roles 
}
```

### JWT Token Management

**Storage:**
- JWT token stored in `localStorage` under key `token`
- Automatically retrieved on page reload

**Interceptor:**
- Token automatically added to all requests
- Header format: `Authorization: Bearer <token>`

**Expiration:**
- On 401 response: Token cleared, user redirected to login
- Manual logout: Token removed from localStorage

---

## Validation Rules

### Login Form
- Email: Valid email format required
- Password: Minimum 6 characters

### Register Form
- First Name: 2-50 characters
- Last Name: 2-50 characters
- Email: Valid email format required
- Password: Minimum 6 characters
- Phone Number: 10 digits
- Roles: At least one role required

### Real-time Validation
- Errors cleared as user types
- Form-level validation on submit

---

## API Services

**Location:** `src/services/api.js`

Complete API wrapper for all endpoints:

```javascript
// Auth
authAPI.login(email, password)
authAPI.register(formData)

// Products
productAPI.getAllProducts(page, size)
productAPI.getProductById(id)
productAPI.searchProducts(keyword, categoryId, sellerId, page, size)
productAPI.createProduct(data)
productAPI.updateProduct(id, data)
productAPI.deleteProduct(id)

// Cart
cartAPI.addToCart(productId, quantity)
cartAPI.getCart()
cartAPI.updateCartItem(productId, quantity)
cartAPI.removeFromCart(productId)
cartAPI.clearCart()

// Orders
orderAPI.createOrder()
orderAPI.getOrdersForBuyer(page, size)
orderAPI.getOrdersForSeller(page, size)
orderAPI.getOrderById(id)

// Wishlist
wishlistAPI.addToWishlist(productId)
wishlistAPI.getWishlist()
wishlistAPI.removeFromWishlist(productId)

// Notifications
notificationAPI.getNotifications(page, size)
notificationAPI.markAsRead(id)

// Reviews
reviewAPI.createReview(productId, data)
reviewAPI.getProductReviews(productId)
reviewAPI.updateReview(id, data)
reviewAPI.deleteReview(id)

// Categories
categoryAPI.getAllCategories()
categoryAPI.getCategoryById(id)
```

---

## Custom Hooks

### useAuth Hook

**Location:** `src/hooks/useAuth.js`

Convenient wrapper around Zustand store:

```javascript
import { useAuth } from '@/hooks/useAuth';

const MyComponent = () => {
  const { 
    user, 
    token, 
    isAuthenticated, 
    error, 
    isLoading,
    login, 
    logout, 
    register 
  } = useAuth();

  return (
    // Component JSX
  );
};
```

---

## Form Validation Utilities

**Location:** `src/utils/validation.js`

Available functions:
```javascript
validateEmail(email)              // Returns boolean
validatePassword(password)        // Returns boolean
validatePhoneNumber(phone)        // Returns boolean
validateFirstName(name)           // Returns boolean
validateLastName(name)            // Returns boolean
validateLoginForm(email, password) // Returns errors object
validateRegisterForm(formData)     // Returns errors object
```

---

## State Management Flow

```
User Action (Login/Register)
         ↓
Form Validation
         ↓
API Request (via Axios)
         ↓
Store Update (Zustand)
         ↓
localStorage Update
         ↓
Component Re-render
```

---

## Error Handling

### Types of Errors:
1. **Validation Errors**: Displayed below each field
2. **API Errors**: Displayed in alert box at top of form
3. **Network Errors**: Handled by Axios interceptor
4. **Authentication Errors**: Trigger redirect to login

### Error Flow:
```
API Response Error
         ↓
Axios Interceptor (401 → Clear token & redirect)
         ↓
Store Error Update
         ↓
Display in Component
```

---

## Security Features

1. **JWT Token Storage**: Secure localStorage with Bearer scheme
2. **Automatic Token Injection**: Added to all requests automatically
3. **Token Expiration Handling**: Auto-logout on 401 response
4. **Protected Routes**: Access restricted to authenticated users
5. **Password Security**: Transmitted over HTTPS (production)
6. **Form Validation**: Client-side validation before submission

---

## Responsive Design

- **Mobile**: Full-width forms, stacked layout
- **Tablet**: Optimized spacing, readable fonts
- **Desktop**: Centered containers, max-width constraints

Bootstrap breakpoints used:
- `col-md-*`: Tablet and up
- `col-lg-*`: Desktop and up

---

## Testing Workflow

### Login Flow:
1. Go to `http://localhost:5173/login`
2. Enter email: `test@example.com`
3. Enter password: `password123`
4. Click "Login"
5. Redirected to home page
6. Navbar shows user name

### Register Flow:
1. Go to `http://localhost:5173/register`
2. Fill in all fields
3. Select at least one role
4. Click "Register"
5. Redirected to home page
6. Navbar shows user name

### Protected Route:
1. Logout (click Logout in navbar)
2. Try to access home page
3. Redirected to login page

---

## Performance Optimizations

1. **Code Splitting**: Routes lazy-loaded by Vite
2. **State Persistence**: localStorage reduces API calls
3. **Interceptor Optimization**: Single instance for all requests
4. **Component Memoization**: Prevents unnecessary re-renders
5. **Bundle Optimization**: Bootstrap CSS imported once

---

## Browser Support

- Chrome (latest)
- Firefox (latest)
- Safari (latest)
- Edge (latest)

---

## Troubleshooting

### Issue: Token not persisting after refresh
**Solution:** Check that localStorage is enabled and VITE_API_BASE_URL is correct

### Issue: CORS errors
**Solution:** Ensure backend has CORS configured and frontend uses correct base URL

### Issue: 401 errors on every request
**Solution:** Verify JWT token format is correct and backend validates properly

### Issue: Forms not validating
**Solution:** Check browser console for validation error objects

---

## Next Steps (Sprint 2+)

- Product listing page
- Product detail page
- Shopping cart management
- Order management
- Wishlist features
- Review and rating system
- User profile management
- Notification system
- Search and filter functionality

---

## Files Created/Modified

### New Files Created:
✅ `src/store/authStore.js`
✅ `src/api/axiosConfig.js`
✅ `src/utils/validation.js`
✅ `src/hooks/useAuth.js`
✅ `src/components/ProtectedRoute.jsx`
✅ `src/components/Navbar.jsx`
✅ `src/pages/LoginPage.jsx`
✅ `src/pages/RegisterPage.jsx`
✅ `src/services/api.js`
✅ `.env`

### Files Modified:
✅ `src/routes/AppRoutes.jsx`
✅ `src/App.jsx`
✅ `package.json`

---

## Summary

The authentication module provides a production-ready, scalable foundation for user authentication in CampusMart. It includes:

- ✅ Centralized state management with Zustand
- ✅ Secure JWT token handling
- ✅ Form validation and error handling
- ✅ Protected routes
- ✅ Responsive UI with Bootstrap 5
- ✅ Automatic token persistence
- ✅ Comprehensive API integration
- ✅ Reusable hooks and components

The architecture is designed to scale easily for additional features in future sprints.

