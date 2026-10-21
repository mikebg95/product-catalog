<script setup lang="ts">
defineProps<{ name: string; sub: string; title: string; subtitle: string }>()
</script>

<template>
  <header class="topbar">
    <div class="topbar-inner">
      <div class="brand">
        <div class="logo"><slot name="icon" /></div>
        <div>
          <div class="brand-name">{{ name }}</div>
          <div class="brand-sub">{{ sub }}</div>
        </div>
      </div>
      <slot name="badge" />
    </div>
  </header>
  <div class="page-head">
    <div>
      <h1 class="page-title">{{ title }}</h1>
      <p class="page-sub">{{ subtitle }}</p>
    </div>
    <div class="actions"><slot name="actions" /></div>
  </div>
</template>
