import type { components } from './schema'

// Types generated from catalog-service's OpenAPI contract (npm run gen:api).
export type ProductRequest = components['schemas']['ProductRequest']
type Problem = components['schemas']['Problem']

async function failure(res: Response): Promise<Error> {
  const problem: Problem | null = await res.json().catch(() => null)
  return new Error(problem?.detail ?? problem?.title ?? `catalog-service answered ${res.status}`)
}

// PUT creates the product (201) or replaces it completely (200).
export async function upsertProduct(sku: string, product: ProductRequest): Promise<{ created: boolean }> {
  const res = await fetch(`/api/products/${encodeURIComponent(sku)}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(product),
  })
  if (!res.ok) throw await failure(res)
  return { created: res.status === 201 }
}
