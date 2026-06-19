import { useSellerStore } from '../store/sellerStore';

export const useSeller = () => {
  const {
    products,
    loadingProducts,
    errorProducts,
    loadingSubmit,
    errorSubmit,
    attributes,
    images,
    uploadingImages,
    errorImages,
    fetchSellerProducts,
    createProduct,
    updateProduct,
    deleteProduct,
    fetchProductAttributes,
    addProductAttribute,
    deleteProductAttribute,
    fetchProductImages,
    uploadProductImages,
    deleteProductImage,
  } = useSellerStore();

  return {
    products,
    loadingProducts,
    errorProducts,
    loadingSubmit,
    errorSubmit,
    attributes,
    images,
    uploadingImages,
    errorImages,
    fetchSellerProducts,
    createProduct,
    updateProduct,
    deleteProduct,
    fetchProductAttributes,
    addProductAttribute,
    deleteProductAttribute,
    fetchProductImages,
    uploadProductImages,
    deleteProductImage,
  };
};
