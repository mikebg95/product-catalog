import type { ProductRequest } from './api/client'

// The PIM owns its product data, like a real PIM: it keeps the products itself (in this browser)
// and only sends them to the catalog. It never reads them back from the catalog.
export type PimProduct = { sku: string } & ProductRequest

const KEY = 'fake-pim.products'

export function loadProducts(): PimProduct[] {
  try {
    const list = JSON.parse(localStorage.getItem(KEY) ?? '[]')
    return Array.isArray(list) ? list : []
  } catch {
    return []
  }
}

// Create or replace by SKU, the same way PUT /products/{sku} works in the catalog.
export function storeProduct(product: PimProduct): void {
  const list = loadProducts().filter((p) => p.sku !== product.sku)
  list.push(product)
  list.sort((a, b) => a.name.localeCompare(b.name))
  try {
    localStorage.setItem(KEY, JSON.stringify(list))
  } catch {
    // Storage full or blocked: the list just isn't kept after a reload.
  }
}

// Forgets the PIM's own list (only in this browser). The catalog and the price system are reset
// with scripts/reset-demo.sh; the PIM can't and shouldn't delete anything there.
export function clearProducts(): void {
  try {
    localStorage.removeItem(KEY)
  } catch {
    // Storage blocked: nothing to clear.
  }
}
