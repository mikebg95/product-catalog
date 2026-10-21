<script setup lang="ts">
defineProps<{ name: string; sub: string }>()
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
      <slot />
    </div>
  </header>
</template>
