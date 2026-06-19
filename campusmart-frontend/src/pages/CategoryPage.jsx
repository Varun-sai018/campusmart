import { useParams } from 'react-router-dom';
import ProductListPage from './ProductListPage.jsx';

function CategoryPage() {
  const { categoryId } = useParams();
  const parsedCategoryId = Number(categoryId);

  return <ProductListPage categoryId={Number.isNaN(parsedCategoryId) ? null : parsedCategoryId} />;
}

export default CategoryPage;
