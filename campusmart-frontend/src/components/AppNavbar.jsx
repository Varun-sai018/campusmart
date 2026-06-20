import { useEffect, useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { Container, Navbar, Nav, Form, Button, Badge } from 'react-bootstrap';
import { Home, Heart, ShoppingCart, Bell, Search, Layers } from 'lucide-react';
import { useBuyer } from '../hooks/useBuyer';
import { useAuth } from '../hooks/useAuth';

export default function AppNavbar() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const [query, setQuery] = useState(searchParams.get('search') || '');
  const { cartSummary, notifications, fetchCart, fetchNotifications } = useBuyer();
  const { isAuthenticated, user, logout } = useAuth();

  useEffect(() => {
    fetchCart();
    fetchNotifications();
  }, [fetchCart, fetchNotifications]);

  const cartCount = cartSummary?.totalItems || 0;
  const unreadCount = notifications?.filter((item) => !item.read)?.length || 0;

  const handleSearchSubmit = (event) => {
    event.preventDefault();
    const trimmed = query.trim();
    if (trimmed.length > 0) {
      navigate(`/products?search=${encodeURIComponent(trimmed)}`);
    } else {
      navigate('/products');
    }
  };

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <Navbar expand="lg" className="app-navbar" sticky="top">
      <Container fluid className="app-navbar-inner">
        <Navbar.Brand as={Link} to="/" className="app-brand">
          <span className="brand-mark">CM</span>
          <div>
            <div className="brand-name">CampusMart</div>
            <div className="brand-tagline">Campus marketplace for students</div>
          </div>
        </Navbar.Brand>

        <Navbar.Toggle aria-controls="market-navbar" />
        <Navbar.Collapse id="market-navbar">
          <Nav className="me-auto nav-links">
            <Nav.Link as={Link} to="/" className="nav-link-icon">
              <Home size={18} /> Home
            </Nav.Link>
            <Nav.Link as={Link} to="/products" className="nav-link-icon">
              <Layers size={18} /> Marketplace
            </Nav.Link>
            <Nav.Link as={Link} to="/wishlist" className="nav-link-icon">
              <Heart size={18} /> Wishlist
            </Nav.Link>
            <Nav.Link as={Link} to="/orders" className="nav-link-icon">
              Orders
            </Nav.Link>
          </Nav>

          <Form className="search-form" onSubmit={handleSearchSubmit}>
            <div className="search-input-wrapper">
              <Search size={16} className="search-icon" />
              <Form.Control
                type="search"
                placeholder="Search products, categories..."
                value={query}
                onChange={(event) => setQuery(event.target.value)}
                className="search-input"
              />
            </div>
            <Button type="submit" className="search-button">
              Search
            </Button>
          </Form>

          <div className="navbar-actions">
            <Button as={Link} to="/notifications" variant="ghost" className="icon-button">
              <Bell size={20} />
              {unreadCount > 0 && <Badge bg="danger" pill className="badge-count">{unreadCount}</Badge>}
            </Button>
            <Button as={Link} to="/cart" variant="ghost" className="icon-button">
              <ShoppingCart size={20} />
              {cartCount > 0 && <Badge bg="danger" pill className="badge-count">{cartCount}</Badge>}
            </Button>
            {isAuthenticated ? (
              <div className="account-chip">
                <span>{user?.firstName || 'Buyer'}</span>
                <Button variant="ghost" className="logout-button" onClick={handleLogout}>
                  Logout
                </Button>
              </div>
            ) : (
              <div className="auth-actions">
                <Button as={Link} to="/login" variant="ghost" className="auth-link">
                  Login
                </Button>
                <Button as={Link} to="/register" variant="primary" className="auth-link">
                  Sign up
                </Button>
              </div>
            )}
          </div>
        </Navbar.Collapse>
      </Container>
    </Navbar>
  );
}
