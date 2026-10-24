import type { components } from './schema'

// Types generated from fake-price-service's OpenAPI contract (npm run gen:api).
export type Sku = components['schemas']['Sku']
export type SkuPrice = components['schemas']['SkuPrice']
type SkuList = components['schemas']['SkuList']
type Problem = components['schemas']['Problem']

async function failure(res: Response): Promise<Error> {
  const problem: Problem | null = await res.json().catch(() => null)
  return new Error(problem?.detail ?? problem?.title ?? `fake-price-service answered ${res.status}`)
}

// Every SKU the PIM registered, with its price if it has one.
export async function listSkus(): Promise<Sku[]> {
  const res = await fetch('/api/skus')
  if (!res.ok) throw await failure(res)
  const body: SkuList = await res.json()
  return body.skus
}

// The price system sets updatedAt itself; the answer has the new price with that time.
export async function setPrice(sku: string, amount: number): Promise<SkuPrice> {
  const res = await fetch(`/api/prices/${encodeURIComponent(sku)}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ amount }),
  })
  if (!res.ok) throw await failure(res)
  return res.json()
}

// Demo only, not part of price-api.yaml: turn the price api off and on, for the outage demo.
export type OutageStatus = { down: boolean; downSince: string | null }

export async function outageStatus(): Promise<OutageStatus> {
  const res = await fetch('/api/demo/outage')
  if (!res.ok) throw await failure(res)
  return res.json()
}

export async function turnOff(): Promise<OutageStatus> {
  const res = await fetch('/api/demo/outage/off', { method: 'POST' })
  if (!res.ok) throw await failure(res)
  return res.json()
}

// Comes back empty, like a real restart of a system that keeps its data in memory.
export async function turnOn(): Promise<OutageStatus> {
  const res = await fetch('/api/demo/outage/on', { method: 'POST' })
  if (!res.ok) throw await failure(res)
  return res.json()
}
