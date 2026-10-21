<script setup lang="ts">
import AppHeader from './components/AppHeader.vue'
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
  </AppHeader>

  <main class="page">
    <!-- Comes in Phase 5, once fake-price-service has its API. -->
    <div class="card empty">
      <div class="empty-icon">
        <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M20.6 13.4 13.4 20.6a2 2 0 0 1-2.8 0L3 13V3h10l7.6 7.6a2 2 0 0 1 0 2.8Z" /><circle cx="8" cy="8" r="1.5" />
        </svg>
      </div>
      <p class="empty-title">No prices yet</p>
      <p>Prices show up here once fake-price-service has its API.</p>
    </div>
  </main>
</template>
