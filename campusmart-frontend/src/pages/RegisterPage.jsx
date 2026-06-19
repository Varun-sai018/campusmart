import { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { validateRegisterForm } from '../utils/validation';

export const RegisterPage = () => {
  const navigate = useNavigate();
  const { register, isAuthenticated, isLoading, error } = useAuth();
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    phoneNumber: '',
    roles: [],
  });
  const [errors, setErrors] = useState({});

  useEffect(() => {
    if (isAuthenticated) {
      navigate('/');
    }
  }, [isAuthenticated, navigate]);

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;

    if (type === 'checkbox') {
      setFormData((prev) => ({
        ...prev,
        roles: checked
          ? [...prev.roles, value]
          : prev.roles.filter((role) => role !== value),
      }));
    } else {
      setFormData((prev) => ({
        ...prev,
        [name]: value,
      }));
    }

    // Clear error for this field when user starts typing
    if (errors[name]) {
      setErrors((prev) => ({
        ...prev,
        [name]: '',
      }));
    }
  };

  const handlePhoneChange = (e) => {
    const { value } = e.target;
    // Allow only digits and formatting characters
    const cleaned = value.replace(/\D/g, '').slice(0, 10);
    const formatted = cleaned.replace(/(\d{3})(\d{3})(\d{4})/, '$1-$2-$3').replace(/-+$/, '');

    setFormData((prev) => ({
      ...prev,
      phoneNumber: formatted,
    }));

    if (errors.phoneNumber) {
      setErrors((prev) => ({
        ...prev,
        phoneNumber: '',
      }));
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    const validationErrors = validateRegisterForm(formData);
    if (Object.keys(validationErrors).length > 0) {
      setErrors(validationErrors);
      return;
    }

    const success = await register(formData);
    if (success) {
      navigate('/');
    }
  };

  return (
    <div className="min-vh-100 d-flex align-items-center justify-content-center" style={{ backgroundColor: '#f8f9fa' }}>
      <div className="container">
        <div className="row justify-content-center">
          <div className="col-md-8 col-lg-6">
            <div className="card shadow-lg border-0 rounded-4">
              <div className="card-body p-5">
                <div className="text-center mb-4">
                  <h1 style={{ fontSize: '2.5rem' }}>🎓</h1>
                  <h2 className="h3 fw-bold text-primary mb-2">CampusMart</h2>
                  <p className="text-muted">Join Our Student Marketplace</p>
                </div>

                <h3 className="h4 fw-bold mb-4 text-center">Create Your Account</h3>

                {error && (
                  <div className="alert alert-danger alert-dismissible fade show" role="alert">
                    <strong>Error!</strong> {error}
                    <button type="button" className="btn-close" onClick={() => setErrors({})}></button>
                  </div>
                )}

                <form onSubmit={handleSubmit}>
                  <div className="row">
                    <div className="col-md-6 mb-3">
                      <label htmlFor="firstName" className="form-label fw-semibold">
                        First Name
                      </label>
                      <input
                        type="text"
                        className={`form-control ${errors.firstName ? 'is-invalid' : ''}`}
                        id="firstName"
                        name="firstName"
                        placeholder="John"
                        value={formData.firstName}
                        onChange={handleChange}
                        disabled={isLoading}
                      />
                      {errors.firstName && (
                        <div className="invalid-feedback d-block">{errors.firstName}</div>
                      )}
                    </div>

                    <div className="col-md-6 mb-3">
                      <label htmlFor="lastName" className="form-label fw-semibold">
                        Last Name
                      </label>
                      <input
                        type="text"
                        className={`form-control ${errors.lastName ? 'is-invalid' : ''}`}
                        id="lastName"
                        name="lastName"
                        placeholder="Doe"
                        value={formData.lastName}
                        onChange={handleChange}
                        disabled={isLoading}
                      />
                      {errors.lastName && (
                        <div className="invalid-feedback d-block">{errors.lastName}</div>
                      )}
                    </div>
                  </div>

                  <div className="mb-3">
                    <label htmlFor="email" className="form-label fw-semibold">
                      Email Address
                    </label>
                    <input
                      type="email"
                      className={`form-control ${errors.email ? 'is-invalid' : ''}`}
                      id="email"
                      name="email"
                      placeholder="john@example.com"
                      value={formData.email}
                      onChange={handleChange}
                      disabled={isLoading}
                    />
                    {errors.email && (
                      <div className="invalid-feedback d-block">{errors.email}</div>
                    )}
                  </div>

                  <div className="mb-3">
                    <label htmlFor="password" className="form-label fw-semibold">
                      Password
                    </label>
                    <input
                      type="password"
                      className={`form-control ${errors.password ? 'is-invalid' : ''}`}
                      id="password"
                      name="password"
                      placeholder="At least 6 characters"
                      value={formData.password}
                      onChange={handleChange}
                      disabled={isLoading}
                    />
                    {errors.password && (
                      <div className="invalid-feedback d-block">{errors.password}</div>
                    )}
                  </div>

                  <div className="mb-3">
                    <label htmlFor="phoneNumber" className="form-label fw-semibold">
                      Phone Number
                    </label>
                    <input
                      type="tel"
                      className={`form-control ${errors.phoneNumber ? 'is-invalid' : ''}`}
                      id="phoneNumber"
                      name="phoneNumber"
                      placeholder="123-456-7890"
                      value={formData.phoneNumber}
                      onChange={handlePhoneChange}
                      disabled={isLoading}
                    />
                    {errors.phoneNumber && (
                      <div className="invalid-feedback d-block">{errors.phoneNumber}</div>
                    )}
                  </div>

                  <div className="mb-4">
                    <label className="form-label fw-semibold">Select Your Role(s)</label>
                    <div className="form-check">
                      <input
                        className="form-check-input"
                        type="checkbox"
                        value="BUYER"
                        id="buyerRole"
                        checked={formData.roles.includes('BUYER')}
                        onChange={handleChange}
                        disabled={isLoading}
                      />
                      <label className="form-check-label" htmlFor="buyerRole">
                        Buyer - Browse and purchase items
                      </label>
                    </div>
                    <div className="form-check">
                      <input
                        className="form-check-input"
                        type="checkbox"
                        value="SELLER"
                        id="sellerRole"
                        checked={formData.roles.includes('SELLER')}
                        onChange={handleChange}
                        disabled={isLoading}
                      />
                      <label className="form-check-label" htmlFor="sellerRole">
                        Seller - List and sell items
                      </label>
                    </div>
                    {errors.roles && (
                      <div className="text-danger small mt-2">{errors.roles}</div>
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
                        Creating Account...
                      </>
                    ) : (
                      'Register'
                    )}
                  </button>
                </form>

                <hr className="my-4" />

                <div className="text-center">
                  <p className="text-muted mb-0">
                    Already have an account?{' '}
                    <Link to="/login" className="text-primary fw-semibold text-decoration-none">
                      Login here
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
