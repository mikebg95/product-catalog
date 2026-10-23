import type { components } from './schema'

// Types generated from catalog-service's OpenAPI contract (npm run gen:api).
export type Product = components['schemas']['Product']
export type ProductPage = components['schemas']['ProductPage']
type Problem = components['schemas']['Problem']

async function failure(res: Response): Promise<Error> {
  const problem: Problem | null = await res.json().catch(() => null)
  return new Error(problem?.detail ?? problem?.title ?? `catalog-service answered ${res.status}`)
}

// With q: only the products that match, best match first. Without: all products, sorted by SKU.
export async function listProducts(page: number, size: number, q = '', signal?: AbortSignal): Promise<ProductPage> {
  const params = new URLSearchParams({ page: String(page), size: String(size) })
  if (q) params.set('q', q)
  const res = await fetch(`/api/products?${params}`, { signal })
  if (!res.ok) throw await failure(res)
  return res.json()
}

// null when there is no product with this SKU (404)
export async function getProduct(sku: string): Promise<Product | null> {
  const res = await fetch(`/api/products/${encodeURIComponent(sku)}`)
  if (res.status === 404) return null
  if (!res.ok) throw await failure(res)
  return res.json()
}
