import { Outlet } from 'react-router-dom';

function MainLayout() {
  return (
    <main className="container py-4">
      <Outlet />
    </main>
  );
}

export default MainLayout;

