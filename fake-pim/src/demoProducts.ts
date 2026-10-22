import type { ProductRequest } from './api/client'

// The demo data set. Each SKU is also registered at fake-price-service once fake-pim does that (Phase 5).
export const demoProducts: Array<{ sku: string } & ProductRequest> = [
  {
    sku: 'SHOE-RUN-42',
    name: 'Trail Runner 2',
    brand: 'Stride',
    category: 'Shoes',
    description: 'Lightweight trail running shoe with a grippy outsole.',
    attributes: { color: 'red', size: 42, waterproof: true },
  },
  {
    sku: 'LAPTOP-PRO-14',
    name: 'AeroBook Pro 14',
    brand: 'Nimbus',
    category: 'Laptops',
    description: '14-inch laptop for developers, with all-day battery life.',
    attributes: { ramGb: 16, storageGb: 512, color: 'silver' },
  },
  {
    sku: 'HEADPHONES-NC-1',
    name: 'Quiet One',
    brand: 'Sonora',
    category: 'Headphones',
    description: 'Over-ear wireless headphones with noise cancelling.',
    attributes: { wireless: true, batteryHours: 30, color: 'black' },
  },
  {
    sku: 'JACKET-RAIN-M',
    name: 'Stormshell Jacket',
    brand: 'Northline',
    category: 'Jackets',
    description: 'Packable rain jacket for hiking and the city.',
    attributes: { size: 'M', waterproof: true, color: 'blue' },
  },
  {
    sku: 'COFFEE-ESPRESSO-1',
    name: 'Barista Mini',
    brand: 'Crema',
    category: 'Coffee machines',
    description: 'Compact espresso machine with a milk frother.',
    attributes: { pressureBar: 15, color: 'white' },
  },
  {
    sku: 'BACKPACK-DAY-20',
    name: 'Daypack 20',
    brand: 'Northline',
    category: 'Backpacks',
    description: '20-litre backpack with a padded laptop sleeve.',
    attributes: { volumeLitres: 20, waterproof: false, color: 'green' },
  },
  {
    sku: 'WATCH-SPORT-1',
    name: 'Pulse Sport',
    brand: 'Tempo',
    category: 'Watches',
    description: 'GPS sports watch with a heart-rate monitor.',
    attributes: { gps: true, batteryDays: 7, color: 'black' },
  },
  {
    sku: 'LAMP-DESK-1',
    name: 'Halo Desk Lamp',
    brand: 'Lumen',
    category: 'Lighting',
    description: 'Dimmable LED desk lamp with warm and cool light.',
    attributes: { dimmable: true, wattage: 8, color: 'white' },
  },
]
