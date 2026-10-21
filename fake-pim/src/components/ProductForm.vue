<script setup lang="ts">
import { reactive, ref, watch } from 'vue'
import { upsertProduct, type Product, type ProductRequest } from '../api/client'

// null = a new product
const props = defineProps<{ product: Product | null }>()
const emit = defineEmits<{ saved: [sku: string, created: boolean]; failed: [message: string] }>()

type Row = { key: string; value: string }
const form = reactive({ sku: '', name: '', brand: '', category: '', description: '', attributes: [] as Row[] })
const saving = ref(false)

watch(
  () => props.product,
  (p) => {
    form.sku = p?.sku ?? ''
    form.name = p?.name ?? ''
    form.brand = p?.brand ?? ''
    form.category = p?.category ?? ''
    form.description = p?.description ?? ''
    form.attributes = Object.entries(p?.attributes ?? {}).map(([key, value]) => ({ key, value: String(value) }))
  },
  { immediate: true },
)

// Attribute values are flat: true/false become booleans, numbers become numbers, the rest stays text.
function parse(value: string): string | number | boolean {
  const v = value.trim()
  if (v === 'true' || v === 'false') return v === 'true'
  if (v !== '' && !Number.isNaN(Number(v))) return Number(v)
  return v
}

async function save() {
  const request: ProductRequest = {
    name: form.name.trim(),
    brand: form.brand.trim() || undefined,
    category: form.category.trim() || undefined,
    description: form.description.trim() || undefined,
    attributes: Object.fromEntries(
      form.attributes.filter((row) => row.key.trim()).map((row) => [row.key.trim(), parse(row.value)]),
    ),
  }
  saving.value = true
  try {
    const { created } = await upsertProduct(form.sku.trim(), request)
    emit('saved', form.sku.trim(), created)
  } catch (e) {
    emit('failed', e instanceof Error ? e.message : String(e))
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <form class="card card-pad" @submit.prevent="save">
    <h2 class="section-title">{{ product ? `Edit ${product.sku}` : 'New product' }}</h2>

    <div class="field">
      <label for="sku">SKU <span class="hint">letters, digits and dashes</span></label>
      <input id="sku" v-model="form.sku" class="input" required pattern="[A-Za-z0-9\-]{1,64}" :disabled="!!product" />
    </div>
    <div class="field">
      <label for="name">Name</label>
      <input id="name" v-model="form.name" class="input" required />
    </div>
    <div class="row">
      <div class="field">
        <label for="brand">Brand</label>
        <input id="brand" v-model="form.brand" class="input" />
      </div>
      <div class="field">
        <label for="category">Category</label>
        <input id="category" v-model="form.category" class="input" />
      </div>
    </div>
    <div class="field">
      <label for="description">Description</label>
      <textarea id="description" v-model="form.description" class="input" />
    </div>

    <div class="field">
      <label>Attributes <span class="hint">text, number or true/false</span></label>
      <div v-for="(row, i) in form.attributes" :key="i" class="attr-row">
        <input v-model="row.key" class="input" placeholder="key" aria-label="Attribute key" />
        <input v-model="row.value" class="input" placeholder="value" aria-label="Attribute value" />
        <button type="button" class="btn icon" aria-label="Remove attribute" @click="form.attributes.splice(i, 1)">×</button>
      </div>
      <button type="button" class="btn add" @click="form.attributes.push({ key: '', value: '' })">+ Add attribute</button>
    </div>

    <button type="submit" class="btn btn-primary save" :disabled="saving">
      {{ saving ? 'Saving…' : 'Save product' }}
    </button>
  </form>
</template>

<style scoped>
.attr-row { display: grid; grid-template-columns: 1fr 1fr 40px; gap: 8px; }
.icon { padding: 0; height: 40px; font-size: 18px; color: var(--muted); }
.add { align-self: flex-start; height: 34px; font-size: 13px; }
.save { width: 100%; height: 42px; margin-top: 6px; }
</style>
