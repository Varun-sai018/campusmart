import { create } from 'zustand';
import { toast } from 'react-toastify';
import { productAPI, productAttributeAPI, productImageAPI } from '../services/api';

export const useSellerStore = create((set, get) => ({
  products: [],
  loadingProducts: false,
  errorProducts: null,
  loadingSubmit: false,
  errorSubmit: null,
  attributes: [],
  images: [],
  uploadingImages: false,
  errorImages: null,

  fetchSellerProducts: async (sellerId) => {
    set({ loadingProducts: true, errorProducts: null });
    try {
      const response = await productAPI.getProductsBySeller(sellerId);
      set({ products: response.content || [] });
    } catch (error) {
      set({ errorProducts: error.response?.data?.message || 'Failed to load seller products.' });
    } finally {
      set({ loadingProducts: false });
    }
  },

  createProduct: async (productData) => {
    set({ loadingSubmit: true, errorSubmit: null });
    try {
      const product = await productAPI.createProduct(productData);
      toast.success('Product added successfully.');
      return product;
    } catch (error) {
      set({ errorSubmit: error.response?.data?.message || 'Unable to save product.' });
      toast.error(error.response?.data?.message || 'Unable to save product.');
      return null;
    } finally {
      set({ loadingSubmit: false });
    }
  },

  updateProduct: async (productId, productData) => {
    set({ loadingSubmit: true, errorSubmit: null });
    try {
      const product = await productAPI.updateProduct(productId, productData);
      toast.success('Product updated successfully.');
      return product;
    } catch (error) {
      set({ errorSubmit: error.response?.data?.message || 'Unable to update product.' });
      toast.error(error.response?.data?.message || 'Unable to update product.');
      return null;
    } finally {
      set({ loadingSubmit: false });
    }
  },

  deleteProduct: async (productId, sellerId) => {
    try {
      await productAPI.deleteProduct(productId);
      set((state) => ({ products: state.products.filter((item) => item.id !== productId) }));
      toast.success('Product removed successfully.');
    } catch (error) {
      toast.error(error.response?.data?.message || 'Unable to delete product.');
    }
  },

  fetchProductAttributes: async (productId) => {
    try {
      const response = await productAttributeAPI.getProductAttributes(productId);
      set({ attributes: response });
    } catch (error) {
      toast.error(error.response?.data?.message || 'Unable to load product attributes.');
    }
  },

  addProductAttribute: async (productId, attributeData) => {
    try {
      const attribute = await productAttributeAPI.createProductAttribute(productId, attributeData);
      set((state) => ({ attributes: [...state.attributes, attribute] }));
      toast.success('Attribute added successfully.');
      return attribute;
    } catch (error) {
      toast.error(error.response?.data?.message || 'Unable to add attribute.');
      return null;
    }
  },

  deleteProductAttribute: async (attributeId) => {
    try {
      await productAttributeAPI.deleteProductAttribute(attributeId);
      set((state) => ({ attributes: state.attributes.filter((item) => item.id !== attributeId) }));
      toast.success('Attribute removed.');
    } catch (error) {
      toast.error(error.response?.data?.message || 'Unable to remove attribute.');
    }
  },

  fetchProductImages: async (productId) => {
    try {
      const images = await productImageAPI.getProductImages(productId);
      set({ images });
    } catch (error) {
      toast.error(error.response?.data?.message || 'Unable to load product images.');
    }
  },

  uploadProductImages: async (productId, files) => {
    set({ uploadingImages: true, errorImages: null });
    try {
      const uploaded = [];
      for (const file of files) {
        const image = await productImageAPI.uploadImage(productId, file);
        uploaded.push(image);
      }
      set((state) => ({ images: [...state.images, ...uploaded] }));
      toast.success('Images uploaded successfully.');
      return uploaded;
    } catch (error) {
      set({ errorImages: error.response?.data?.message || 'Unable to upload images.' });
      toast.error(error.response?.data?.message || 'Unable to upload images.');
      return null;
    } finally {
      set({ uploadingImages: false });
    }
  },

  deleteProductImage: async (imageId) => {
    try {
      await productImageAPI.deleteProductImage(imageId);
      set((state) => ({ images: state.images.filter((item) => item.id !== imageId) }));
      toast.success('Image removed.');
    } catch (error) {
      toast.error(error.response?.data?.message || 'Unable to remove image.');
    }
  },
}));
