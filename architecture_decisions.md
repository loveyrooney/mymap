# Architecture Decisions & Constraints

## 1. Station Identification Strategy
- **DB Integrity (Physical Key)**: `station_id` (9-digit NODE_ID) is the strict Primary Key for evaluating global uniqueness across regions.
- **Business Logic (Logical Key)**: `arsid` (Local 5-digit ID) is the absolute core key for all business logic, path registration, and map automation.

## 2. Denormalization in `station_order` Table
- **Justification**: Users and map APIs interface strictly via `arsid`. The `station_order` table exists primarily to calculate opposite-direction "pair stations" (which physically reside on opposite sides of the street and possess entirely distinct `arsid`s).
- **Constraint**: `(route_id, arsid)` isolates a specific physical geography. Importantly, a route *can* visit the identical `arsid` twice within the exact same direction (e.g., looping back on an upstream path). However, a single route will **never** traverse both a Seoul `10195` and a Gyeonggi `10195`. Thus, `arsid` safely and uniquely resolves to a single physical station within the operational scope of any given `route_id`.
