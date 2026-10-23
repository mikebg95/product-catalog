<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { listProducts, type Product } from '../api/client'
import AppHeader from './AppHeader.vue'
import ProductCard from './ProductCard.vue'

// Re-read often, so a synced price shows up within seconds during the demo.
const REFRESH_MS = 3000
// Search while typing: wait for a pause, and only search from 2 characters on.
const DEBOUNCE_MS = 300
const MIN_CHARS = 2

const products = ref<Product[]>([])
const total = ref(0)
const loaded = ref(false)
const error = ref<string | null>(null)
let timer: number | undefined

// The search lives in the url (#/?q=…), so the browser's back button returns to the same results.
const query = ref(new URLSearchParams(window.location.hash.split('?')[1] ?? '').get('q') ?? '')
const searchTerm = computed(() => (query.value.trim().length >= MIN_CHARS ? query.value.trim() : ''))
// The search the shown products belong to, and how long the backend took.
const shownTerm = ref('')
const tookMs = ref<number | null>(null)
let inFlight: AbortController | null = null
let debounce: number | undefined

async function refresh() {
  // A newer request always wins: cancel the one still on its way, so an old answer can't replace a new one.
  inFlight?.abort()
  const request = new AbortController()
  inFlight = request
  const term = searchTerm.value
  const started = performance.now()
  try {
    const page = await listProducts(0, 100, term, request.signal)
    products.value = page.content
    total.value = page.totalElements
    shownTerm.value = term
    tookMs.value = Math.round(performance.now() - started)
    error.value = null
  } catch (e) {
    if (request.signal.aborted) return
    error.value = e instanceof Error ? e.message : String(e)
  } finally {
    if (!request.signal.aborted) loaded.value = true
  }
}

watch(searchTerm, (term) => {
  window.clearTimeout(debounce)
  debounce = window.setTimeout(() => {
    history.replaceState(null, '', term ? `#/?q=${encodeURIComponent(term)}` : '#/')
    refresh()
  }, DEBOUNCE_MS)
})

function clearSearch() {
  query.value = ''
}

const subtitle = computed(() => {
  if (!shownTerm.value) return `${total.value} ${total.value === 1 ? 'product' : 'products'}`
  const took = tookMs.value === null ? '' : ` · ${tookMs.value} ms`
  return `${total.value} ${total.value === 1 ? 'result' : 'results'} for "${shownTerm.value}"${took}`
})

onMounted(() => {
  refresh()
  timer = window.setInterval(refresh, REFRESH_MS)
})
onUnmounted(() => {
  window.clearInterval(timer)
  window.clearTimeout(debounce)
  inFlight?.abort()
})
</script>

<template>
  <AppHeader name="Catalog" sub="Product catalog" title="Products" :subtitle="subtitle">
    <template #icon>
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <path d="M6 2 3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4Z" /><path d="M3 6h18" /><path d="M16 10a4 4 0 0 1-8 0" />
      </svg>
    </template>
    <template #badge>
      <span class="badge"><span class="badge-dot" />Live prices</span>
    </template>
    <template #actions>
      <div class="search">
        <svg class="search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <circle cx="11" cy="11" r="7" /><path d="m20 20-3.5-3.5" />
        </svg>
        <input
          v-model="query"
          class="input search-input"
          type="search"
          maxlength="100"
          placeholder="Search products, e.g. red sho"
          aria-label="Search products"
          autofocus
          @keydown.esc="clearSearch"
        />
      </div>
    </template>
  </AppHeader>

  <main class="page">
    <div v-if="error" class="error-banner">Can't load products: {{ error }}</div>

    <div v-if="loaded && !error && products.length === 0 && shownTerm" class="card empty">
      <div class="empty-icon">
        <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <circle cx="11" cy="11" r="7" /><path d="m20 20-3.5-3.5" />
        </svg>
      </div>
      <p class="empty-title">Nothing matches "{{ shownTerm }}"</p>
      <p>Every word has to match, and typos aren't recognized. Try fewer or shorter words.</p>
      <button class="btn search-clear" @click="clearSearch">Clear search</button>
    </div>

    <div v-else-if="loaded && !error && products.length === 0" class="card empty">
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
.search { position: relative; width: min(360px, 100%); }
.search-icon { position: absolute; left: 12px; top: 50%; width: 16px; height: 16px; transform: translateY(-50%); color: var(--muted); pointer-events: none; }
.search-input { padding-left: 36px; }
.search-clear { margin-top: 16px; }
@media (max-width: 640px) {
  .search { width: 100%; }
}
</style>
