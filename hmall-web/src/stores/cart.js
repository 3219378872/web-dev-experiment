import { defineStore } from 'pinia';
import { getCart, addToCart, updateCartItem, deleteCartItem } from '@/api/cart';

export const useCartStore = defineStore('cart', {
  state: () => ({ items: [], selectedIds: [] }),
  getters: {
    selectedItems: (state) => state.items.filter((i) => state.selectedIds.includes(i.id)),
    totalAmount: (state) => state.selectedItems.reduce((sum, i) => sum + i.price * i.num, 0),
    totalCount: (state) => state.selectedIds.length,
  },
  actions: {
    async fetchCart() {
      this.items = await getCart();
      this.selectedIds = this.items.map((i) => i.id);
    },
    async addItem(form) {
      await addToCart(form);
      await this.fetchCart();
    },
    async updateItem(id, data) {
      await updateCartItem(id, data);
      await this.fetchCart();
    },
    async removeItem(id) {
      await deleteCartItem(id);
      await this.fetchCart();
    },
    toggleSelect(id) {
      const idx = this.selectedIds.indexOf(id);
      if (idx >= 0) this.selectedIds.splice(idx, 1);
      else this.selectedIds.push(id);
    },
    toggleAll() {
      if (this.selectedIds.length === this.items.length) this.selectedIds = [];
      else this.selectedIds = this.items.map((i) => i.id);
    },
  },
});
