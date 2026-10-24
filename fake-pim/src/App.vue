<script setup lang="ts">
import { ref } from 'vue'
import { upsertProduct } from './api/client'
import { registerSku } from './api/priceClient'
import AppHeader from './components/AppHeader.vue'
import ProductForm from './components/ProductForm.vue'
import { demoProducts } from './demoProducts'
import { loadProducts, storeProduct, type PimProduct } from './store'

const products = ref<PimProduct[]>(loadProducts())
const selected = ref<PimProduct | null>(null)
const loadingDemo = ref(false)
const toast = ref<{ text: string; error: boolean } | null>(null)
let toastTimer: number | undefined

function notify(text: string, error = false) {
  toast.value = { text, error }
  window.clearTimeout(toastTimer)
  toastTimer = window.setTimeout(() => (toast.value = null), 3000)
}

function onSaved(product: PimProduct, created: boolean, registered: boolean) {
  products.value = loadProducts()
  selected.value = product
  const action = `${created ? 'Created' : 'Updated'} ${product.sku} in the catalog`
  if (registered) {
    notify(`${action} and registered it in the price system`)
  } else {
    notify(`${action}, but the price system could not be reached`, true)
  }
}

function onFailed(product: PimProduct, message: string) {
  products.value = loadProducts()
  selected.value = product
  notify(`Saved in the PIM, but not sent to the catalog: ${message}`, true)
}

async function loadDemo() {
  loadingDemo.value = true
  try {
    for (const product of demoProducts) {
      storeProduct(product)
      const { sku, ...request } = product
      await upsertProduct(sku, request)
      await registerSku(sku)
    }
    notify(`Loaded ${demoProducts.length} demo products, sent them to the catalog and registered them in the price system`)
  } catch (e) {
    notify(`Saved in the PIM, but sending failed: ${e instanceof Error ? e.message : e}`, true)
  } finally {
    loadingDemo.value = false
    products.value = loadProducts()
  }
}
</script>

<template>
  <AppHeader name="PIM" sub="Product information" title="Products" subtitle="Sent to the catalog (PUT /products/{sku}) and registered in the price system (PUT /skus/{sku})">
    <template #icon>
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <path d="M21 8 12 3 3 8v8l9 5 9-5V8Z" /><path d="m3 8 9 5 9-5" /><path d="M12 13v8" />
      </svg>
    </template>
    <template #badge>
      <span class="badge">External system · demo</span>
    </template>
    <template #actions>
      <button class="btn" @click="selected = null">New product</button>
      <button class="btn btn-primary" :disabled="loadingDemo" @click="loadDemo">
        {{ loadingDemo ? 'Loading…' : 'Load demo products' }}
      </button>
    </template>
  </AppHeader>

  <main class="page">
    <div class="layout">
      <section class="card list">
        <p v-if="products.length === 0" class="list-empty">No products yet. Load the demo products, or add one.</p>
        <button
          v-for="p in products"
          :key="p.sku"
          class="list-item"
          :class="{ active: selected?.sku === p.sku }"
          @click="selected = p"
        >
          <span class="list-name">{{ p.name }}</span>
          <span class="list-meta">{{ p.sku }}<template v-if="p.brand"> · {{ p.brand }}</template></span>
        </button>
        <p class="list-note">Kept in this browser. The PIM owns its product data and sends it to the catalog.</p>
      </section>

      <ProductForm :product="selected" @saved="onSaved" @failed="onFailed" />
    </div>
  </main>

  <Transition name="toast">
    <div v-if="toast" class="toast" :class="{ 'toast-error': toast.error }">{{ toast.text }}</div>
  </Transition>
</template>

<style scoped>
.layout { display: grid; grid-template-columns: minmax(0, 1fr) minmax(0, 1.25fr); gap: 16px; align-items: start; }
.list { padding: 6px; }
.list-empty { padding: 20px; color: var(--muted); font-size: 14px; }
.list-note { margin: 6px 8px 4px; padding-top: 8px; border-top: 1px solid var(--border, rgba(0, 0, 0, 0.08)); color: var(--muted); font-size: 12px; }
.list-item {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 2px;
  text-align: left;
  padding: 12px 14px;
  border: 0;
  border-radius: 10px;
  background: transparent;
  cursor: pointer;
  transition: background 0.15s;
}
.list-item:hover { background: var(--surface-2); }
.list-item.active { background: var(--accent-soft); }
.list-item.active .list-name { color: var(--accent-text); }
.list-name { font-weight: 600; }
.list-meta { font-size: 12.5px; color: var(--muted); font-family: ui-monospace, SFMono-Regular, Menlo, monospace; }
@media (max-width: 800px) { .layout { grid-template-columns: 1fr; } }
</style>
