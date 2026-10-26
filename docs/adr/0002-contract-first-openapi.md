# 2. Contract-first OpenAPI

**Status:** Accepted

## Context
Other teams depend on my API: the PIM sends products, and the webshop reads them. My catalog also depends on the price system's API. For each connection, both sides need one clear description of the API that they can trust.

## Decision
I first write each API as an OpenAPI file, and then generate code from it.
- `catalog-api.yaml`: the build generates Java interfaces, and my controller implements them. Checks on the request (required fields, what a SKU may look like, page size) come from this file too.
- `price-api.yaml`: the catalog generates its price client from its own copy of this file. The price system belongs to another team, so I keep a fixed copy. This way my build doesn't depend on their files, and if they change their API, I update my copy on purpose.

## Why
- The file is the one source of truth. My controller implements the generated interfaces, so if its URLs, parameters or types don't match the file, the build fails. The status codes and errors are checked by the tests.
- It makes me design the API on purpose, from the user's point of view: the URLs, the status codes, the errors.
- Other teams can start building against the file before my code is ready. The webshop generates its types from it.

## Not chosen
- **Code-first** (the API file is generated from the code): quicker to start, but then the file just follows whatever the code does, and nothing checks it.

## Consequences
- The generated API classes are separate from my domain classes, so the web adapter has to map between them.
- Errors use the standard Problem format (RFC 9457), and that is also described in the file.
