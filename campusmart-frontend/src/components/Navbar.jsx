import { useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';

export const Navbar = () => {
  const { isAuthenticated, user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <nav className="navbar navbar-expand-lg navbar-dark bg-primary">
      <div className="container-fluid">
        <div className="navbar-brand fw-bold" style={{ cursor: 'pointer' }} onClick={() => navigate('/')}> 
          <span style={{ fontSize: '1.5rem' }}>🎓 CampusMart</span>
        </div>

        <button
          className="navbar-toggler"
          type="button"
          data-bs-toggle="collapse"
          data-bs-target="#navbarNav"
          aria-controls="navbarNav"
          aria-expanded="false"
          aria-label="Toggle navigation"
        >
          <span className="navbar-toggler-icon"></span>
        </button>

        <div className="collapse navbar-collapse" id="navbarNav">
          <ul className="navbar-nav ms-auto">
            {isAuthenticated && (
              <>
                <li className="nav-item">
                  <button
                    className="nav-link btn btn-link text-white"
                    onClick={() => navigate('/')}
                  >
                    Marketplace
                  </button>
                </li>
                <li className="nav-item">
                  <button
                    className="nav-link btn btn-link text-white"
                    onClick={() => navigate('/wishlist')}
                  >
                    Wishlist
                  </button>
                </li>
                <li className="nav-item">
                  <button
                    className="nav-link btn btn-link text-white"
                    onClick={() => navigate('/cart')}
                  >
                    Cart
                  </button>
                </li>
                <li className="nav-item">
                  <button
                    className="nav-link btn btn-link text-white"
                    onClick={() => navigate('/orders')}
                  >
                    Orders
                  </button>
                </li>
                {user?.roles?.includes('SELLER') && (
                  <li className="nav-item">
                    <button
                      className="nav-link btn btn-link text-white"
                      onClick={() => navigate('/seller')}
                    >
                      Seller Dashboard
                    </button>
                  </li>
                )}
                <li className="nav-item">
                  <button
                    className="nav-link btn btn-link text-white"
                    onClick={() => navigate('/notifications')}
                  >
                    Notifications
                  </button>
                </li>
              </>
            )}
            {!isAuthenticated ? (
              <>
                <li className="nav-item">
                  <button
                    className="nav-link btn btn-link text-white"
                    onClick={() => navigate('/login')}
                  >
                    Login
                  </button>
                </li>
                <li className="nav-item">
                  <button
                    className="nav-link btn btn-link text-white"
                    onClick={() => navigate('/register')}
                  >
                    Register
                  </button>
                </li>
              </>
            ) : (
              <>
                <li className="nav-item">
                  <span className="nav-link text-white">
                    Welcome, {user?.firstName} {user?.lastName}
                  </span>
                </li>
                <li className="nav-item">
                  <button
                    className="nav-link btn btn-link text-white"
                    onClick={() => navigate('/profile')}
                  >
                    Profile
                  </button>
                </li>
                <li className="nav-item">
                  <button
                    className="nav-link btn btn-link text-danger"
                    onClick={handleLogout}
                  >
                    Logout
                  </button>
                </li>
              </>
            )}
          </ul>
        </div>
      </div>
    </nav>
  );
};
