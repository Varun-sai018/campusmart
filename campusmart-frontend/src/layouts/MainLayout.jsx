import { Outlet } from 'react-router-dom';

function MainLayout() {
  return (
    <main className="app-page-shell">
      <div className="app-page-container">
        <Outlet />
      </div>
    </main>
  );
}

export default MainLayout;

