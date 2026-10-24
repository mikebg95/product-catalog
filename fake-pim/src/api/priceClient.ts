import type { components } from './price-schema'

// Types generated from fake-price-service's OpenAPI contract (npm run gen:api).
type Problem = components['schemas']['Problem']

// Makes the SKU known to the price system, without a price, so it can be priced there.
// Registering a SKU that already exists changes nothing (204), so it's safe to send on every save.
export async function registerSku(sku: string): Promise<void> {
  const res = await fetch(`/price-api/skus/${encodeURIComponent(sku)}`, { method: 'PUT' })
  if (!res.ok) {
    const problem: Problem | null = await res.json().catch(() => null)
    throw new Error(problem?.detail ?? problem?.title ?? `fake-price-service answered ${res.status}`)
  }
}
