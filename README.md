# سخرة شفشاون - Chefchaouen Delivery 🛵🇲🇦

Mobile-first on-demand delivery application specifically designed for the historic blue mountain city of **Chefchaouen, Morocco**. It unites Customers, Couriers, and Admins into a cohesive, realtime system with native support for Moroccan Darija (RTL default), French, MAD currency, and Moroccan telephone numbers (`+212`).

---

## 🌟 Architecture & Key Features

1. **Role-Based Workflows**:
   - **Customer**: Location-first ordering on Chefchaouen interactive map, real-time nearby couriers ranking, request types (*"شري ليا وخلس"* - Buy for me, or *"موصي عليها جيبها"* - Pickup only), atomic courier reservation, live delivery tracking, direct Dial (`tel:`) and WhatsApp (`wa.me`) contact.
   - **Courier**: Admin approval gate, work availability toggle, live GPS location broadcast, custom purchase limit (e.g. 300 MAD max cash fronted), order status lifecycle (Requested ➔ Accepted ➔ At Shop ➔ Purchased ➔ On The Way ➔ Delivered), and cash ledger recording.
   - **Admin Dashboard**: GeoJSON Service-Area Boundary management, polygon verification control, courier verification and approval system, global order monitoring, and financial cash ledger overview (app commission, delivery fees).

2. **Chefchaouen Service-Area Geo-Fencing**:
   - Built-in municipal Chefchaouen delivery polygon enclosing the Medina, Outa El Hammam, Bab Souk, Ras El Maa, Bab El Ain, Sidi Abdelhamid, and Hay El Andalous.
   - Strict Point-In-Polygon (Ray Casting) algorithm validating customer destination and courier coordinates before order acceptance.
   - If boundary is disabled or pin is outside the perimeter, order submission is safely blocked with clear explanation.

3. **Calls & WhatsApp Integration**:
   - **Normal Phone Call**: Launches Android dialer via `tel:+212...`.
   - **WhatsApp Chat**: Opens `wa.me/212...` with an authentic Moroccan Darija or French prefilled message.
   - UI explicitly instructs users that voice calls are started from inside the WhatsApp conversation window.

4. **Data Persistence & Concurrency**:
   - Room local database with reactive Kotlin `Flow` observation.
   - Atomic courier reservation: Prevents double-booking a single courier across simultaneous requests.

---

## 🚀 Replit / Web Deployment Setup

To deploy the backend / full-stack service in Replit:

1. **Environment Variables**:
   Copy `.env.example` to `.env`:
   ```bash
   cp .env.example .env
   ```
   Set `DATABASE_URL` (PostgreSQL with PostGIS extension enabled) if running the remote sync bridge:
   ```sql
   CREATE EXTENSION IF NOT EXISTS postgis;
   ```

2. **Database Migrations**:
   The database tables match the Room entities:
   - `user_profiles` (id, role, name, phone, address, approved)
   - `courier_presence` (courier_id, is_online, current_lat, current_lng, purchase_limit_mad, is_approved)
   - `service_areas` (id, name, geojson_polygon, is_approved)
   - `orders` (id, customer_id, courier_id, shop_name, item_description, request_type, status, delivery_lat, delivery_lng, delivery_fee_mad)
   - `order_events` (id, order_id, status, timestamp)
   - `cash_ledger` (id, order_id, courier_id, item_cost_mad, delivery_fee_mad, app_commission_mad, total_collected)

3. **Courier Approval Process**:
   1. Courier registers in the app with phone number (+212 6... / +212 7...).
   2. Courier status is initially **Pending Approval** (cannot receive or accept orders).
   3. Admin logs into the Admin tab ➔ Navigates to **"طلبات الموافقة على الليفروغ" (Courier Approvals)**.
   4. Admin verifies the identity/vehicle and taps **"موافقة" (Approve)**.
   5. Courier can now toggle availability to "Online" and receive incoming orders.

4. **Service Area Configuration**:
   - Admin tab ➔ **"منطقة الخدمة (Chefchaouen Service Area)"**.
   - GeoJSON polygon vertices can be inspected or edited.
   - Toggle **"Approved Service Area"** on/off to enable or freeze city-wide ordering.

---

## 🧪 Testing

Unit tests for service-area rejection, courier race condition checks, and order lifecycle transitions can be run via:
```bash
gradle :app:testDebugUnitTest
```
