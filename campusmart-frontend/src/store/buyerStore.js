import { create } from 'zustand';
import { toast } from 'react-toastify';
import { cartAPI, notificationAPI, orderAPI, wishlistAPI } from '../services/api';

export const useBuyerStore = create((set, get) => ({
  wishlist: [],
  cartSummary: null,
  orders: [],
  notifications: [],
  loadingWishlist: false,
  loadingCart: false,
  loadingOrders: false,
  loadingNotifications: false,
  errorWishlist: null,
  errorCart: null,
  errorOrders: null,
  errorNotifications: null,

  fetchWishlist: async () => {
    set({ loadingWishlist: true, errorWishlist: null });
    try {
      const wishlist = await wishlistAPI.getWishlist();
      set({ wishlist });
    } catch (error) {
      set({ errorWishlist: error.response?.data?.message || 'Failed to load wishlist.' });
    } finally {
      set({ loadingWishlist: false });
    }
  },

  removeFromWishlist: async (productId) => {
    try {
      await wishlistAPI.removeFromWishlist(productId);
      set((state) => ({
        wishlist: state.wishlist.filter((item) => item.productId !== productId),
      }));
      toast.success('Removed item from your wishlist.');
    } catch (error) {
      toast.error(error.response?.data?.message || 'Unable to remove item.');
    }
  },

  fetchCart: async () => {
    set({ loadingCart: true, errorCart: null });
    try {
      const cartSummary = await cartAPI.getCart();
      set({ cartSummary });
    } catch (error) {
      set({ errorCart: error.response?.data?.message || 'Failed to load cart.' });
    } finally {
      set({ loadingCart: false });
    }
  },

  updateCartItemQuantity: async (productId, quantity) => {
    try {
      await cartAPI.updateCartItem(productId, { quantity });
      await get().fetchCart();
      toast.success('Cart updated successfully.');
    } catch (error) {
      toast.error(error.response?.data?.message || 'Unable to update cart item.');
    }
  },

  removeCartItem: async (productId) => {
    try {
      await cartAPI.removeFromCart(productId);
      await get().fetchCart();
      toast.success('Item removed from cart.');
    } catch (error) {
      toast.error(error.response?.data?.message || 'Unable to remove item from cart.');
    }
  },

  clearCart: async () => {
    try {
      await cartAPI.clearCart();
      await get().fetchCart();
      toast.success('Cart cleared successfully.');
    } catch (error) {
      toast.error(error.response?.data?.message || 'Unable to clear cart.');
    }
  },

  placeOrder: async () => {
    try {
      const order = await orderAPI.createOrder();
      await get().fetchOrders();
      await get().fetchCart();
      toast.success('Order placed successfully.');
      return order;
    } catch (error) {
      toast.error(error.response?.data?.message || 'Unable to place order.');
      return null;
    }
  },

  fetchOrders: async () => {
    set({ loadingOrders: true, errorOrders: null });
    try {
      const orders = await orderAPI.getOrders();
      set({ orders });
    } catch (error) {
      set({ errorOrders: error.response?.data?.message || 'Failed to load orders.' });
    } finally {
      set({ loadingOrders: false });
    }
  },

  fetchNotifications: async () => {
    set({ loadingNotifications: true, errorNotifications: null });
    try {
      const notifications = await notificationAPI.getNotifications();
      set({ notifications });
    } catch (error) {
      set({ errorNotifications: error.response?.data?.message || 'Failed to load notifications.' });
    } finally {
      set({ loadingNotifications: false });
    }
  },

  markNotificationRead: async (notificationId) => {
    try {
      await notificationAPI.markAsRead(notificationId);
      set((state) => ({
        notifications: state.notifications.map((notification) =>
          notification.id === notificationId ? { ...notification, read: true } : notification,
        ),
      }));
      toast.success('Marked notification as read.');
    } catch (error) {
      toast.error(error.response?.data?.message || 'Unable to mark notification as read.');
    }
  },

  markAllNotificationsRead: async () => {
    const notifications = get().notifications;
    const unreadIds = notifications.filter((item) => !item.read).map((item) => item.id);
    if (unreadIds.length === 0) {
      toast.info('All notifications are already read.');
      return;
    }

    try {
      await Promise.all(unreadIds.map((id) => notificationAPI.markAsRead(id)));
      await get().fetchNotifications();
      toast.success('All notifications marked as read.');
    } catch (error) {
      toast.error(error.response?.data?.message || 'Unable to mark all as read.');
    }
  },

  deleteNotification: async (notificationId) => {
    set((state) => ({
      notifications: state.notifications.filter((notification) => notification.id !== notificationId),
    }));
    toast.success('Notification removed locally.');
  },
}));
