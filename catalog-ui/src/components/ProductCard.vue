<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import type { Product } from '../api/client'

const props = defineProps<{ product: Product }>()

const euro = new Intl.NumberFormat('en-IE', { style: 'currency', currency: 'EUR' })
const price = computed(() => (props.product.price ? euro.format(props.product.price.amount) : null))
const attributes = computed(() => Object.entries(props.product.attributes))

// Light up the price when the sync changes it, so the demo shows it happening.
const flash = ref(false)
watch(price, () => {
  flash.value = false
  requestAnimationFrame(() => (flash.value = true))
})
</script>

<template>
  <article class="card product">
    <div class="product-top">
      <span class="product-brand">{{ product.brand }}</span>
      <span v-if="product.category" class="chip chip-accent">{{ product.category }}</span>
    </div>
    <h2 class="product-name">{{ product.name }}</h2>
    <p v-if="product.description" class="product-desc">{{ product.description }}</p>
    <div v-if="attributes.length" class="chips">
      <span v-for="[key, value] in attributes" :key="key" class="chip">
        {{ key }} <strong>{{ value }}</strong>
      </span>
    </div>
    <div class="product-bottom">
      <span class="product-sku">{{ product.sku }}</span>
      <span v-if="price" class="product-price" :class="{ flash }" @animationend="flash = false">{{ price }}</span>
      <span v-else class="product-price pending">Price pending</span>
    </div>
  </article>
</template>

<style scoped>
.product { padding: 20px; display: flex; flex-direction: column; gap: 10px; transition: transform 0.15s, box-shadow 0.15s; }
.product:hover { transform: translateY(-2px); box-shadow: 0 10px 30px color-mix(in srgb, var(--accent) 12%, transparent); }
.product-top { display: flex; justify-content: space-between; align-items: center; gap: 8px; min-height: 24px; }
.product-brand { font-size: 12px; font-weight: 700; letter-spacing: 0.06em; text-transform: uppercase; color: var(--muted); }
.product-name { font-size: 17px; font-weight: 700; letter-spacing: -0.01em; }
.product-desc {
  color: var(--muted);
  font-size: 14px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.product-bottom {
  margin-top: auto;
  padding-top: 12px;
  border-top: 1px solid var(--border);
  display: flex;
  justify-content: space-between;
  align-items: baseline;
}
.product-sku { font-size: 12px; color: var(--muted); font-family: ui-monospace, SFMono-Regular, Menlo, monospace; }
.product-price { font-size: 20px; font-weight: 750; letter-spacing: -0.01em; border-radius: 8px; padding: 0 6px; margin-right: -6px; }
.product-price.pending { font-size: 13px; font-weight: 600; color: var(--muted); }
.flash { animation: flash 1.6s ease-out; }
@keyframes flash {
  0% { background: var(--accent); color: #fff; }
  100% { background: transparent; }
}
</style>
