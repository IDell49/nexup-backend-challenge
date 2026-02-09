import java.time.DayOfWeek
import java.time.LocalTime

fun main() {
    println("=== SUPERMARKET MANAGEMENT SYSTEM - STARTING SIMULATION ===\n")

    // --- PHASE 1: SYSTEM CONFIGURATION ---

    // 1. Initialize Product Catalog
    val rice = Product(1, "White Rice", 200.0)
    val milk = Product(2, "Whole Milk", 150.0)
    val coke = Product(3, "Coca-Cola 2.5L", 500.0)
    val soap = Product(4, "Dove Soap", 100.0)
    val water = Product(5, "Mineral Water", 80.0)
    val wine = Product(6, "Cabernet Sauvignon Wine", 1200.0)

    // 2. Initialize Stores with Schedules
    // Central: Standard hours (08-22), open everyday.
    val centralStore = Supermarket(
        id = 1,
        name = "Central Store",
        openingHour = LocalTime.of(8, 0),
        closingHour = LocalTime.of(22, 0),
        openDays = DayOfWeek.entries.toList()
    )

    // North: Business hours (09-18), Mon-Fri only.
    val northStore = Supermarket(
        id = 2,
        name = "North Store",
        openingHour = LocalTime.of(9, 0),
        closingHour = LocalTime.of(18, 0),
        openDays = listOf(
            DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY, DayOfWeek.FRIDAY
        )
    )

    // 3. Stock Inventory
    println(">> Loading inventory...")
    listOf(rice, milk, coke, soap, water, wine).forEach {
        centralStore.registerProduct(it)
        centralStore.addStock(it.id, 100)
    }

    // North Store carries a limited selection (No Wine)
    listOf(rice, milk, coke, water).forEach {
        northStore.registerProduct(it)
        northStore.addStock(it.id, 50)
    }
    println("   Inventory loaded.\n")


    // --- PHASE 2: OPERATIONS & VALIDATION ---

    println(">> Simulating operational errors (Validation Checks):")

    // Test 1: Sell unregistered product
    processSale(northStore, wine, 5)

    // Test 2: Insufficient stock
    processSale(northStore, coke, 1000)

    // Test 3: Unregister product
    println(">> Removing Rice from Central Store...")
    centralStore.unregisterProduct(rice)

    // Attempt sale of removed product
    processSale(centralStore, rice, 1) // Should fail

    // Restore for remaining tests
    centralStore.registerProduct(rice)
    centralStore.addStock(rice.id, 50)


    // --- PHASE 3: DATA GENERATION ---

    println("\n>> GENERATING SALES DATA (Selling 6 different items)...")
    // Generating a specific ranking to verify the Top 5 logic.
    // Target Ranking:
    // 1. Rice  (~50)
    // 2. Coke  (~40)
    // 3. Water (~30)
    // 4. Milk  (20)
    // 5. Wine  (10)
    // 6. Soap  (5) -> Should be excluded from the report.

    processSale(northStore, water, 30)
    processSale(centralStore, rice, 50)
    processSale(centralStore, coke, 40)
    processSale(centralStore, milk, 20)
    processSale(centralStore, wine, 10)
    processSale(centralStore, soap, 5)

    println("   Data generation complete.\n")


    // --- PHASE 4: REPORTS & AUDITS ---

    // 4a. Individual Store Audit (Central)
    // We register one final sale to demonstrate return values
    val lastSale = processSale(centralStore, rice, 5)

    println("==========================================")
    println("   INDIVIDUAL STORE AUDIT: ${centralStore.name.uppercase()}")
    println("==========================================")
    println("1. Last Sale Return (${rice.name} x5): $$lastSale")
    println("2. Total Qty Sold (${rice.name}):      ${centralStore.getQuantitySold(rice.id)}")
    println("3. Total Revenue (${rice.name}):       $${centralStore.getProductRevenue(rice.id)}")
    println("4. Store Total Revenue:              $${centralStore.getTotalRevenue()}")
    println("==========================================")

    // 4b. Chain Consolidated Report
    val chain = SupermarketChain(mutableListOf(centralStore, northStore))

    println("\n==========================================")
    println("       CONSOLIDATED FINAL REPORT")
    println("==========================================")
    println("1. Chain Total Revenue:   $${chain.getTotalRevenue()}")
    println("2. Best Performing Store: ${chain.getSupermarketWithHighestRevenue()}")
    println("3. Top Selling Products:  ${chain.getTop5SellingProducts()}")
    println("==========================================")


    // --- PHASE 5: BONUS FEATURE (SCHEDULES) ---

    println("\n==========================================")
    println("       OPENING HOURS CHECK (Bonus)")
    println("==========================================")

    val mondayMorning = LocalTime.of(10, 0)
    println("1. Monday $mondayMorning (Both Open) -> ${chain.getOpenSupermarkets(DayOfWeek.MONDAY, mondayMorning)}")

    val sundayMorning = LocalTime.of(10, 0)
    println("2. Sunday $sundayMorning (North Closed) -> ${chain.getOpenSupermarkets(DayOfWeek.SUNDAY, sundayMorning)}")

    val mondayNight = LocalTime.of(20, 30)
    println("3. Monday $mondayNight (North Closed) -> ${chain.getOpenSupermarkets(DayOfWeek.MONDAY, mondayNight)}")

    val midnight = LocalTime.of(23, 0)
    println("4. Monday $midnight (All Closed) -> ${chain.getOpenSupermarkets(DayOfWeek.MONDAY, midnight)}")

    println("==========================================")

    // --- PHASE 6: CHAIN MANAGEMENT ---

    println("\n==========================================")
    println("       CHAIN MANAGEMENT TEST")
    println("==========================================")

    println("1. Initial State: ${chain.getAvailableSupermarkets()}")

    val eastStore = Supermarket(3, "East Store")
    chain.addSupermarket(eastStore)
    println("2. Added 'East Store': ${chain.getAvailableSupermarkets()}")

    chain.removeSupermarket(eastStore)
    println("3. Removed 'East Store': ${chain.getAvailableSupermarkets()}")

    println("==========================================")
}

/**
 * Helper to execute a sale transaction for a given [store] and [product].
 * Returns the sale amount or 0.0 if the transaction failed.
 */
fun processSale(store: Supermarket, product: Product, quantity: Int): Double {
    return try {
        val total = store.registerSale(product.id, quantity)
        println("Sale OK at ${store.name}: ${product.name} x$quantity = $$total")
        total
    } catch (e: Exception) {
        println("REJECTED at ${store.name}: ${e.message}")
        0.0
    }
}