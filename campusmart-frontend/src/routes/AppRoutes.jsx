import { Route, Routes } from 'react-router-dom';
import MainLayout from '../layouts/MainLayout.jsx';
import HomePage from '../pages/HomePage.jsx';

function AppRoutes() {
  return (
    <Routes>
      <Route element={<MainLayout />}>
        <Route index element={<HomePage />} />
      </Route>
    </Routes>
  );
}

export default AppRoutes;

