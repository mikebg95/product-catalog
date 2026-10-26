# 3. Products come in with `PUT /products/{sku}`

**Status:** Accepted

## Context
Products belong to the PIM, a system of another team. The PIM needs a way to add new products and update existing ones, over a network where a call can time out.

## Decision
The PIM sends `PUT /products/{sku}`. If the SKU is new, the product is created (`201`). If the SKU already exists, the product is completely replaced (`200`). The request never contains a price. The SKU is the primary key in the database.

## Why
- The PIM already uses the SKU. It doesn't need to know some ID that my catalog makes up.
- `PUT` is idempotent: sending the same request twice gives the same result. So after a timeout the PIM can just send it again, without creating a duplicate.
- One call for both create and update: the PIM doesn't have to check first whether the product exists.
- Every field has one owner: products come from the PIM, prices only from the price sync. If two systems write the same field, they overwrite each other.

## Not chosen
- **`POST` to create and `PUT /products/{id}` to update:** the PIM would have to store my IDs, and a `POST` sent twice can create a duplicate.
- **A separate ID next to a unique SKU:** an extra key that nobody outside the catalog uses.

## Consequences
- `PUT` replaces the whole product: a field that's left out is emptied. The price is kept: it doesn't belong to the PIM. To update only some fields you would need `PATCH`.
- A `PUT` and a price sync can write the same product at the same moment, and both save the whole product. If the `PUT` writes back an older price, the next sync round fixes it. If the sync writes back the old product details, the `PUT`'s change is lost until the PIM sends that product again.
- Two `PUT`s for the same new SKU at exactly the same moment: one fails on the duplicate key. Sending it again works, as an update.
- No `DELETE`: the assignment doesn't ask for it.
