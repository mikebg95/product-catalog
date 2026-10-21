<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { listProducts, upsertProduct, type Product } from './api/client'
import AppHeader from './components/AppHeader.vue'
import ProductForm from './components/ProductForm.vue'
import { demoProducts } from './demoProducts'

const products = ref<Product[]>([])
const selected = ref<Product | null>(null)
const loadingDemo = ref(false)
const toast = ref<{ text: string; error: boolean } | null>(null)
let toastTimer: number | undefined

function notify(text: string, error = false) {
  toast.value = { text, error }
  window.clearTimeout(toastTimer)
  toastTimer = window.setTimeout(() => (toast.value = null), 3000)
}

async function refresh() {
  try {
    products.value = (await listProducts(0, 100)).content
  } catch (e) {
    notify(`Can't load products: ${e instanceof Error ? e.message : e}`, true)
  }
}

async function onSaved(sku: string, created: boolean) {
  notify(`${created ? 'Created' : 'Updated'} ${sku}`)
  await refresh()
  selected.value = products.value.find((p) => p.sku === sku) ?? null
}

async function loadDemo() {
  loadingDemo.value = true
  try {
    for (const { sku, ...product } of demoProducts) await upsertProduct(sku, product)
    notify(`Loaded ${demoProducts.length} demo products`)
  } catch (e) {
    notify(`Loading failed: ${e instanceof Error ? e.message : e}`, true)
  } finally {
    loadingDemo.value = false
    await refresh()
  }
}

onMounted(refresh)
</script>

<template>
  <AppHeader name="PIM" sub="Product information">
    <template #icon>
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <path d="M21 8 12 3 3 8v8l9 5 9-5V8Z" /><path d="m3 8 9 5 9-5" /><path d="M12 13v8" />
      </svg>
    </template>
    <span class="badge">External system · demo</span>
  </AppHeader>

  <main class="page">
    <div class="page-head">
      <div>
        <h1 class="page-title">Products</h1>
        <p class="page-sub">Sent to the catalog with PUT /products/{sku}</p>
      </div>
      <div class="actions">
        <button class="btn" @click="selected = null">New product</button>
        <button class="btn btn-primary" :disabled="loadingDemo" @click="loadDemo">
          {{ loadingDemo ? 'Loading…' : 'Load demo products' }}
        </button>
      </div>
    </div>

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
      </section>

      <ProductForm :product="selected" @saved="onSaved" @failed="(m) => notify(m, true)" />
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
