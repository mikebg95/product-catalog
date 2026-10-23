<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { getProduct, type Product } from '../api/client'
import AppHeader from './AppHeader.vue'

const props = defineProps<{ sku: string }>()

// Re-read often, so a synced price shows up within seconds during the demo.
const REFRESH_MS = 3000

const product = ref<Product | null>(null)
const notFound = ref(false)
const loaded = ref(false)
const error = ref<string | null>(null)
let timer: number | undefined

async function refresh() {
  try {
    const found = await getProduct(props.sku)
    product.value = found
    notFound.value = found === null
    error.value = null
  } catch (e) {
    error.value = e instanceof Error ? e.message : String(e)
  } finally {
    loaded.value = true
  }
}

const euro = new Intl.NumberFormat('en-IE', { style: 'currency', currency: 'EUR' })
const price = computed(() => (product.value?.price ? euro.format(product.value.price.amount) : null))
const priceTime = computed(() =>
  product.value?.price ? new Date(product.value.price.updatedAt).toLocaleString('en-GB') : null,
)
const attributes = computed(() => Object.entries(product.value?.attributes ?? {}))

// Light up the price when the sync changes it, like on the product cards.
const flash = ref(false)
watch(price, (now, before) => {
  if (before === undefined) return
  flash.value = false
  requestAnimationFrame(() => (flash.value = true))
})

onMounted(() => {
  refresh()
  timer = window.setInterval(refresh, REFRESH_MS)
})
onUnmounted(() => window.clearInterval(timer))
</script>

<template>
  <AppHeader
    name="Catalog"
    sub="Product catalog"
    :title="product?.name ?? (notFound ? 'Product not found' : 'Loading…')"
    :subtitle="sku"
  >
    <template #icon>
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <path d="M6 2 3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4Z" /><path d="M3 6h18" /><path d="M16 10a4 4 0 0 1-8 0" />
      </svg>
    </template>
    <template #badge>
      <span class="badge"><span class="badge-dot" />Live prices</span>
    </template>
    <template #actions>
      <a class="btn" href="#/">← All products</a>
    </template>
  </AppHeader>

  <main class="page">
    <div v-if="error" class="error-banner">Can't load the product: {{ error }}</div>

    <div v-if="loaded && notFound" class="card empty">
      <p class="empty-title">There is no product with SKU {{ sku }}</p>
      <p>The catalog answered 404. Maybe the SKU is wrong, or the PIM hasn't sent it yet.</p>
    </div>

    <div v-if="product" class="detail">
      <section class="card card-pad">
        <div class="detail-top">
          <span class="product-brand">{{ product.brand }}</span>
          <span v-if="product.category" class="chip chip-accent">{{ product.category }}</span>
        </div>
        <p v-if="product.description" class="detail-desc">{{ product.description }}</p>
        <p v-else class="detail-desc muted">No description.</p>

        <h2 class="section-title">Characteristics</h2>
        <dl v-if="attributes.length" class="attrs">
          <template v-for="[key, value] in attributes" :key="key">
            <dt>{{ key }}</dt>
            <dd>{{ value }}</dd>
          </template>
        </dl>
        <p v-else class="muted">No characteristics.</p>
      </section>

      <aside class="card card-pad buy">
        <span class="buy-label">Price</span>
        <span v-if="price" class="buy-price" :class="{ flash }" @animationend="flash = false">{{ price }}</span>
        <span v-else class="buy-price pending">Price pending</span>
        <span v-if="priceTime" class="buy-time">updated {{ priceTime }}</span>
        <span v-else class="buy-time">No price yet: it comes from the next price sync.</span>
        <span class="buy-sku">SKU {{ product.sku }}</span>
      </aside>
    </div>
  </main>
</template>

<style scoped>
.detail { display: grid; grid-template-columns: minmax(0, 1fr) 280px; gap: 16px; align-items: start; }
@media (max-width: 760px) { .detail { grid-template-columns: 1fr; } }
.detail-top { display: flex; justify-content: space-between; align-items: center; gap: 8px; min-height: 24px; margin-bottom: 12px; }
.product-brand { font-size: 12px; font-weight: 700; letter-spacing: 0.06em; text-transform: uppercase; color: var(--muted); }
.detail-desc { font-size: 15px; line-height: 1.6; margin-bottom: 22px; }
.muted { color: var(--muted); }
.attrs { display: grid; grid-template-columns: max-content 1fr; gap: 8px 20px; font-size: 14px; }
.attrs dt { color: var(--muted); }
.attrs dd { font-weight: 600; }
.buy { display: flex; flex-direction: column; gap: 6px; }
.buy-label { font-size: 12px; font-weight: 700; letter-spacing: 0.06em; text-transform: uppercase; color: var(--muted); }
.buy-price { font-size: 32px; font-weight: 750; letter-spacing: -0.02em; border-radius: 8px; padding: 0 6px; margin-left: -6px; width: fit-content; }
.buy-price.pending { font-size: 18px; color: var(--muted); }
.buy-time { font-size: 12px; color: var(--muted); }
.buy-sku { margin-top: 10px; padding-top: 10px; border-top: 1px solid var(--border); font-size: 12px; color: var(--muted); font-family: ui-monospace, SFMono-Regular, Menlo, monospace; }
.flash { animation: flash 1.6s ease-out; }
@keyframes flash {
  0% { background: var(--accent); color: #fff; }
  100% { background: transparent; }
}
</style>
