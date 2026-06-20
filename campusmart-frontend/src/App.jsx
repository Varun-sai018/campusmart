import AppRoutes from './routes/AppRoutes.jsx';
import AppNavbar from './components/AppNavbar.jsx';

function App() {
  return (
    <div className="app-shell">
      <AppNavbar />
      <AppRoutes />
    </div>
  );
}

export default App;

