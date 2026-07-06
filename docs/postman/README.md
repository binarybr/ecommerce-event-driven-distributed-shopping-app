# ShopSphere — Postman End-to-End Tests

An importable Postman collection that exercises the **entire** application through the
API Gateway, with test scripts that **auto-chain** variables (login token → product →
cart → order → payment → review) so you can run it start-to-finish with no manual
copy-paste.

## Files
- `ShopSphere.postman_collection.json` — the collection (7 folders, ~30 requests).
- `ShopSphere.local.postman_environment.json` — sets `baseUrl` to `http://localhost:8080`.

## Import
1. Open Postman → **Import** → drop both files.
2. (Optional) Select the **ShopSphere - Local** environment (top-right). The collection
   also has a built-in default `baseUrl`, so it works without an environment too.

## Prerequisites
- The backend stack is running and registered with Eureka (give it ~60–90s after start).
- For the payment step to **succeed**, set a real `STRIPE_API_KEY` (`sk_test_…`) in
  `deployment/docker/.env`. Without it, the order still places but the charge returns a
  4xx (the collection handles both cases).

## How to run

### Option A — Collection Runner (recommended, true end-to-end)
1. Click the collection → **Run**.
2. Keep the folder order (1 → 7), keep "save responses" on.
3. **Run ShopSphere - End to End.** Every request passes its captured values to the next.

### Option B — Manually, in order
Run folders **top to bottom**. The important ordering rule: run **1. Auth** first
(captures `token` + `adminToken`), then **2. Catalog → List Products** (captures
`productId`/`productPrice`) before the cart/order steps.

## What the flow covers

| Folder | What it tests |
|---|---|
| 1. Auth | Register customer (unique email each run), login customer, login seeded admin, optional key-gated admin registration |
| 2. Catalog | List / get / search products (public) — captures a product to use downstream |
| 3. Admin Setup | Create a product + seed its inventory (ADMIN) |
| 4. Cart | Add to cart, get cart (captures item + total), update quantity |
| 5. Order & Payment | Place order, get order, pay via Stripe `tok_visa`, payment history |
| 6. Reviews & Recommendations | Create review, list reviews + summary, my reviews, recommendations, trending |
| 7. Admin Dashboard | Stats, recent orders, update order status, low stock, users, notifications |

## Notable assertions baked in
- **Security demo:** the *Place Order* request deliberately sends `price: 0.01`, and the
  test asserts the server **recomputed** the price to `unitPrice × quantity` — proving
  client prices are ignored.
- **Auth chaining:** customer requests use `Bearer {{token}}`, admin requests use
  `Bearer {{adminToken}}` — captured automatically at login.
- **Payment tolerance:** the payment test passes whether Stripe succeeds (200 →
  asserts `COMPLETED`) or fails because the key isn't set (logs a hint) — so a missing
  key doesn't break the run.

## Testing against AWS instead of local
Create a new environment (or edit the local one) and set:
```
baseUrl = http://<your-ec2-public-ip>:8080
```
Note: the AWS guide only exposes port **3000** publicly by default — to hit the gateway
(8080) directly from Postman, either add an inbound rule for 8080 (restricted to your
IP) or run Postman from somewhere that can reach the box.

## Default credentials used
- **Customer:** generated per run (`cust_<timestamp>@test.com` / `Passw0rd!`).
- **Admin (seeded):** `admin@shopsphere.com` / `Admin@1234`.
- **Admin registration key:** `ShopSphere@Admin2024`.
