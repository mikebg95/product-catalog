import type { components } from './schema'

// Types generated from catalog-service's OpenAPI contract (npm run gen:api).
export type Product = components['schemas']['Product']
export type ProductPage = components['schemas']['ProductPage']
type Problem = components['schemas']['Problem']

export async function listProducts(page: number, size: number): Promise<ProductPage> {
  const res = await fetch(`/api/products?page=${page}&size=${size}`)
  if (!res.ok) {
    const problem: Problem | null = await res.json().catch(() => null)
    throw new Error(problem?.detail ?? problem?.title ?? `catalog-service answered ${res.status}`)
  }
  return res.json()
}
