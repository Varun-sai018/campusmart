import { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { validateLoginForm } from '../utils/validation';

export const LoginPage = () => {
  const navigate = useNavigate();
  const { login, isAuthenticated, isLoading, error } = useAuth();
  const [formData, setFormData] = useState({
    email: '',
    password: '',
  });
  const [errors, setErrors] = useState({});

  useEffect(() => {
    if (isAuthenticated) {
      navigate('/');
    }
  }, [isAuthenticated, navigate]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
    // Clear error for this field when user starts typing
    if (errors[name]) {
      setErrors((prev) => ({
        ...prev,
        [name]: '',
      }));
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    const validationErrors = validateLoginForm(formData.email, formData.password);
    if (Object.keys(validationErrors).length > 0) {
      setErrors(validationErrors);
      return;
    }

    const success = await login(formData.email, formData.password);
    if (success) {
      navigate('/');
    }
  };

  return (
    <div className="min-vh-100 d-flex align-items-center justify-content-center" style={{ backgroundColor: '#f8f9fa' }}>
      <div className="container">
        <div className="row justify-content-center">
          <div className="col-md-6 col-lg-5">
            <div className="card shadow-lg border-0 rounded-4">
              <div className="card-body p-5">
                <div className="text-center mb-4">
                  <h1 style={{ fontSize: '2.5rem' }}>🎓</h1>
                  <h2 className="h3 fw-bold text-primary mb-2">CampusMart</h2>
                  <p className="text-muted">Student Marketplace</p>
                </div>

                <h3 className="h4 fw-bold mb-4 text-center">Login to Your Account</h3>

                {error && (
                  <div className="alert alert-danger alert-dismissible fade show" role="alert">
                    <strong>Error!</strong> {error}
                    <button type="button" className="btn-close" onClick={() => setErrors({})}></button>
                  </div>
                )}

                <form onSubmit={handleSubmit}>
                  <div className="mb-3">
                    <label htmlFor="email" className="form-label fw-semibold">
                      Email Address
                    </label>
                    <input
                      type="email"
                      className={`form-control form-control-lg ${errors.email ? 'is-invalid' : ''}`}
                      id="email"
                      name="email"
                      placeholder="Enter your email"
                      value={formData.email}
                      onChange={handleChange}
                      disabled={isLoading}
                    />
                    {errors.email && (
                      <div className="invalid-feedback d-block">{errors.email}</div>
                    )}
                  </div>

                  <div className="mb-4">
                    <label htmlFor="password" className="form-label fw-semibold">
                      Password
                    </label>
                    <input
                      type="password"
                      className={`form-control form-control-lg ${errors.password ? 'is-invalid' : ''}`}
                      id="password"
                      name="password"
                      placeholder="Enter your password"
                      value={formData.password}
                      onChange={handleChange}
                      disabled={isLoading}
                    />
                    {errors.password && (
                      <div className="invalid-feedback d-block">{errors.password}</div>
                    )}
                  </div>

                  <button
                    type="submit"
                    className="btn btn-primary btn-lg w-100 fw-semibold mb-3"
                    disabled={isLoading}
                  >
                    {isLoading ? (
                      <>
                        <span className="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                        Logging in...
                      </>
                    ) : (
                      'Login'
                    )}
                  </button>
                </form>

                <hr className="my-4" />

                <div className="text-center">
                  <p className="text-muted mb-0">
                    Don't have an account?{' '}
                    <Link to="/register" className="text-primary fw-semibold text-decoration-none">
                      Register here
                    </Link>
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
