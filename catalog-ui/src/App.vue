<script setup lang="ts">
import { onMounted, onUnmounted, ref } from 'vue'
import { listProducts, type Product } from './api/client'
import AppHeader from './components/AppHeader.vue'
import ProductCard from './components/ProductCard.vue'

// Re-read often, so a synced price shows up within seconds during the demo.
const REFRESH_MS = 3000

const products = ref<Product[]>([])
const total = ref(0)
const loaded = ref(false)
const error = ref<string | null>(null)
let timer: number | undefined

async function refresh() {
  try {
    const page = await listProducts(0, 100)
    products.value = page.content
    total.value = page.totalElements
    error.value = null
  } catch (e) {
    error.value = e instanceof Error ? e.message : String(e)
  } finally {
    loaded.value = true
  }
}

onMounted(() => {
  refresh()
  timer = window.setInterval(refresh, REFRESH_MS)
})
onUnmounted(() => window.clearInterval(timer))
</script>

<template>
  <AppHeader name="Catalog" sub="Product catalog">
    <template #icon>
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <path d="M6 2 3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4Z" /><path d="M3 6h18" /><path d="M16 10a4 4 0 0 1-8 0" />
      </svg>
    </template>
    <span class="badge"><span class="badge-dot" />Live prices</span>
  </AppHeader>

  <main class="page">
    <div class="page-head">
      <div>
        <h1 class="page-title">Products</h1>
        <p class="page-sub">{{ total }} {{ total === 1 ? 'product' : 'products' }}</p>
      </div>
    </div>

    <div v-if="error" class="error-banner">Can't load products: {{ error }}</div>

    <div v-if="loaded && !error && products.length === 0" class="card empty">
      <div class="empty-icon">
        <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M21 8 12 3 3 8v8l9 5 9-5V8Z" /><path d="m3 8 9 5 9-5" /><path d="M12 13v8" />
        </svg>
      </div>
      <p class="empty-title">No products yet</p>
      <p>Load the demo products in the PIM.</p>
    </div>

    <div class="grid">
      <ProductCard v-for="product in products" :key="product.sku" :product="product" />
    </div>
  </main>
</template>

<style scoped>
.grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(260px, 1fr)); gap: 16px; }
</style>
