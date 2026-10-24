<script setup lang="ts">
import { computed, onMounted, onUnmounted, reactive, ref } from 'vue'
import { listSkus, outageStatus, setPrice, turnOff, turnOn, type OutageStatus, type Sku } from './api/client'
import AppHeader from './components/AppHeader.vue'

const REFRESH_MS = 3000

const skus = ref<Sku[]>([])
const loaded = ref(false)
const error = ref<string | null>(null)
const filter = ref('')
// what's typed per SKU; kept apart from the list, so the refresh never wipes what you're typing
const drafts = reactive<Record<string, string>>({})
const saving = ref<string | null>(null)
const justSaved = ref<string | null>(null)
const now = ref(Date.now())
const toast = ref<{ text: string; error: boolean } | null>(null)
// the outage demo: off = the price api answers 503; on = it works again, and is empty like after a restart
const outage = ref<OutageStatus>({ down: false, downSince: null })
const confirmingOff = ref(false)
const switching = ref(false)
const cameBackEmpty = ref(false)
let toastTimer: number | undefined
let refreshTimer: number | undefined
let clockTimer: number | undefined

const shown = computed(() => {
  const q = filter.value.trim().toLowerCase()
  const list = q ? skus.value.filter((s) => s.sku.toLowerCase().includes(q)) : skus.value
  return [...list].sort((a, b) => a.sku.localeCompare(b.sku))
})
const pricedCount = computed(() => skus.value.filter((s) => s.price).length)

function notify(text: string, isError = false) {
  toast.value = { text, error: isError }
  window.clearTimeout(toastTimer)
  toastTimer = window.setTimeout(() => (toast.value = null), 3500)
}

async function refresh() {
  try {
    outage.value = await outageStatus()
    if (outage.value.down) {
      error.value = null
      return
    }
    skus.value = await listSkus()
    if (skus.value.length > 0) cameBackEmpty.value = false
    error.value = null
  } catch (e) {
    error.value = `fake-price-service can't be reached: ${e instanceof Error ? e.message : e}`
  } finally {
    loaded.value = true
  }
}

async function switchOff() {
  switching.value = true
  try {
    outage.value = await turnOff()
    confirmingOff.value = false
    cameBackEmpty.value = false
  } catch (e) {
    notify(`Could not turn it off: ${e instanceof Error ? e.message : e}`, true)
  } finally {
    switching.value = false
  }
}

async function switchOn() {
  switching.value = true
  try {
    outage.value = await turnOn()
    cameBackEmpty.value = true
    await refresh()
  } catch (e) {
    notify(`Could not turn it on: ${e instanceof Error ? e.message : e}`, true)
  } finally {
    switching.value = false
  }
}

function downFor() {
  if (!outage.value.downSince) return ''
  const seconds = Math.max(0, Math.round((now.value - new Date(outage.value.downSince).getTime()) / 1000))
  return seconds < 60 ? `${seconds} s` : `${Math.floor(seconds / 60)} min ${seconds % 60} s`
}

async function save(sku: string) {
  const amount = Number(drafts[sku])
  if (!drafts[sku] || !(amount > 0)) {
    notify('Enter an amount greater than 0', true)
    return
  }
  saving.value = sku
  try {
    const saved = await setPrice(sku, amount)
    skus.value = skus.value.map((s) => (s.sku === sku ? { sku, price: saved.price } : s))
    delete drafts[sku]
    justSaved.value = sku
    window.setTimeout(() => justSaved.value === sku && (justSaved.value = null), 2500)
    notify(`${sku} is now ${euro(saved.price.amount)}. The shop picks it up within ~10 seconds.`)
  } catch (e) {
    notify(`Not saved: ${e instanceof Error ? e.message : e}`, true)
  } finally {
    saving.value = null
  }
}

function euro(amount: number) {
  return new Intl.NumberFormat('en-IE', { style: 'currency', currency: 'EUR' }).format(amount)
}

function ago(iso: string) {
  const seconds = Math.max(0, Math.round((now.value - new Date(iso).getTime()) / 1000))
  if (seconds < 60) return `${seconds} s ago`
  const minutes = Math.round(seconds / 60)
  if (minutes < 60) return `${minutes} min ago`
  return new Date(iso).toLocaleString()
}

onMounted(() => {
  refresh()
  // new SKUs from the PIM show up by themselves
  refreshTimer = window.setInterval(refresh, REFRESH_MS)
  clockTimer = window.setInterval(() => (now.value = Date.now()), 1000)
})
onUnmounted(() => {
  window.clearInterval(refreshTimer)
  window.clearInterval(clockTimer)
})
</script>

<template>
  <AppHeader name="Pricing" sub="Price management" title="Prices" subtitle="Pulled by the catalog every ~10 seconds">
    <template #icon>
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <path d="M20.6 13.4 13.4 20.6a2 2 0 0 1-2.8 0L3 13V3h10l7.6 7.6a2 2 0 0 1 0 2.8Z" /><circle cx="8" cy="8" r="1.5" />
      </svg>
    </template>
    <template #badge>
      <span class="badge">External system · demo</span>
    </template>
    <template #actions>
      <template v-if="outage.down">
        <button class="btn btn-primary" :disabled="switching" @click="switchOn">Turn the price system back on</button>
      </template>
      <template v-else-if="confirmingOff">
        <span class="confirm-text">Turn it off? When it comes back, all prices are gone.</span>
        <button class="btn btn-danger" :disabled="switching" @click="switchOff">Turn off</button>
        <button class="btn" @click="confirmingOff = false">Cancel</button>
      </template>
      <button v-else class="btn" @click="confirmingOff = true">Turn the price system off</button>
    </template>
  </AppHeader>

  <main class="page">
    <div v-if="error" class="error-banner">{{ error }}</div>

    <div v-if="outage.down" class="outage-banner">
      <span class="outage-dot"></span>
      <div>
        <p class="outage-title">The price system is off · {{ downFor() }}</p>
        <p>
          It answers every request with <code>503 Service Unavailable</code>. The catalog's sync fails each round,
          logs a warning and keeps its last known prices: check the shop. Turn it back on when you're ready.
        </p>
      </div>
    </div>
    <div v-else-if="cameBackEmpty" class="back-banner">
      <p class="outage-title">The price system is back on, and empty</p>
      <p>
        It kept everything in memory, so all SKUs and prices are gone, like after a real restart. The shop still shows
        its prices. Load the demo products in fake-pim again to register the SKUs, then price them here.
      </p>
    </div>

    <div v-if="loaded && skus.length > 0 && !outage.down" class="toolbar">
      <input v-model="filter" class="input search" type="search" placeholder="Filter by SKU" aria-label="Filter by SKU" />
      <div class="chips">
        <span class="chip"><strong>{{ skus.length }}</strong> SKUs</span>
        <span class="chip chip-accent">{{ pricedCount }} priced</span>
        <span v-if="skus.length > pricedCount" class="chip">{{ skus.length - pricedCount }} without a price</span>
      </div>
    </div>

    <div v-if="loaded && skus.length === 0 && !error && !outage.down" class="card empty">
      <div class="empty-icon">
        <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M20.6 13.4 13.4 20.6a2 2 0 0 1-2.8 0L3 13V3h10l7.6 7.6a2 2 0 0 1 0 2.8Z" /><circle cx="8" cy="8" r="1.5" />
        </svg>
      </div>
      <p class="empty-title">No SKUs yet</p>
      <p>Add products in fake-pim, or load its demo products. Their SKUs show up here by themselves.</p>
    </div>

    <div v-else-if="skus.length > 0" class="card list" :class="{ dimmed: outage.down }">
      <div class="list-head">
        <span>SKU</span><span>Current price</span><span>Last changed</span><span>New price</span>
      </div>
      <form
        v-for="s in shown"
        :key="s.sku"
        class="list-row"
        :class="{ saved: justSaved === s.sku }"
        @submit.prevent="save(s.sku)"
      >
        <span class="sku">{{ s.sku }}</span>
        <span>
          <strong v-if="s.price" class="amount">{{ euro(s.price.amount) }}</strong>
          <span v-else class="chip">No price yet</span>
        </span>
        <span class="muted" :title="s.price ? new Date(s.price.updatedAt).toLocaleString() : ''">
          {{ s.price ? ago(s.price.updatedAt) : '–' }}
        </span>
        <span class="set">
          <span class="money">
            <span class="currency">€</span>
            <input
              v-model="drafts[s.sku]"
              class="input"
              type="number"
              min="0.01"
              step="0.01"
              inputmode="decimal"
              :placeholder="s.price ? String(s.price.amount) : '0.00'"
              :aria-label="`New price for ${s.sku}`"
            />
          </span>
          <button class="btn btn-primary" type="submit" :disabled="saving === s.sku || !drafts[s.sku]">
            {{ saving === s.sku ? 'Saving…' : 'Set price' }}
          </button>
        </span>
      </form>
      <p v-if="shown.length === 0" class="no-match">No SKU matches "{{ filter }}".</p>
    </div>
  </main>

  <Transition name="toast">
    <div v-if="toast" class="toast" :class="{ 'toast-error': toast.error }" role="status">{{ toast.text }}</div>
  </Transition>
</template>

<style scoped>
.toolbar { display: flex; align-items: center; justify-content: space-between; gap: 12px; flex-wrap: wrap; margin-bottom: 16px; }
.search { max-width: 280px; }

.list { overflow: hidden; }
.list-head, .list-row {
  display: grid;
  grid-template-columns: minmax(140px, 1.2fr) 1fr 1fr minmax(240px, 1.4fr);
  gap: 16px;
  align-items: center;
  padding: 12px 20px;
}
.list-head {
  font-size: 12px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  color: var(--muted);
  background: var(--surface-2);
  border-bottom: 1px solid var(--border);
}
.list-row { border-bottom: 1px solid var(--border); transition: background 0.6s; }
.list-row:last-of-type { border-bottom: 0; }
.list-row.saved { background: var(--accent-soft); transition: none; }
.sku { font-family: ui-monospace, SFMono-Regular, Menlo, monospace; font-size: 13px; font-weight: 600; }
.amount { font-size: 16px; }
.muted { color: var(--muted); font-size: 14px; }

.set { display: flex; gap: 8px; }
.money { position: relative; flex: 1; }
.currency { position: absolute; left: 12px; top: 50%; transform: translateY(-50%); color: var(--muted); pointer-events: none; }
.money .input { padding-left: 26px; }
.no-match { padding: 20px; text-align: center; color: var(--muted); }
.dimmed { opacity: 0.35; pointer-events: none; }

.confirm-text { align-self: center; font-size: 13px; font-weight: 600; color: var(--danger); }
.btn-danger { border: 0; color: #fff; background: var(--danger); }

.outage-banner, .back-banner {
  display: flex;
  gap: 14px;
  align-items: flex-start;
  padding: 16px 18px;
  margin-bottom: 16px;
  border-radius: 12px;
  background: var(--surface);
  border: 1px solid var(--border);
  box-shadow: var(--shadow);
  font-size: 14px;
}
.outage-banner { border-left: 4px solid var(--danger); }
.back-banner { display: block; border-left: 4px solid var(--accent); }
.outage-title { font-weight: 700; margin-bottom: 4px; }
.outage-banner code { font-size: 12px; }
.outage-dot {
  flex: none;
  width: 10px;
  height: 10px;
  margin-top: 6px;
  border-radius: 50%;
  background: var(--danger);
  animation: blink 1.2s ease-in-out infinite;
}
@keyframes blink { 50% { opacity: 0.25; } }

@media (max-width: 760px) {
  .list-head { display: none; }
  .list-row { grid-template-columns: 1fr 1fr; row-gap: 8px; }
  .set { grid-column: 1 / -1; }
}
</style>
