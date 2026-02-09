import java.time.DayOfWeek

fun main() {
    println("=== SUPERMARKET MANAGEMENT SYSTEM - STARTING SIMULATION ===\n")

    // 1. Setup
    val rice = Product(1, "White Rice", 200.0)
    val milk = Product(2, "Whole Milk", 150.0)
    val coke = Product(3, "Coca-Cola 2.5L", 500.0)
    val soap = Product(4, "Dove Soap", 100.0)
    val water = Product(5, "Mineral Water", 80.0)
    val wine = Product(6, "Cabernet Sauvignon Wine", 1200.0)

    // Configuración de Sucursales con HORARIOS
    // Central: Abre todos los días de 08 a 22
    val centralStore = Supermarket(
        id = 1,
        name = "Central Store",
        openingHour = 8,
        closingHour = 22,
        openDays = DayOfWeek.entries.toList() // Lunes a Domingo
    )

    // Norte: Abre solo de Lunes a Viernes, de 09 a 18
    val northStore = Supermarket(
        id = 2,
        name = "North Store",
        openingHour = 9,
        closingHour = 18,
        openDays = listOf(
            DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY, DayOfWeek.FRIDAY
        )
    )

    // 2. Load Stock
    println(">> Loading inventory...")
    listOf(rice, milk, coke, soap, water, wine).forEach {
        centralStore.registerProduct(it)
        centralStore.addStock(it.id, 100)
    }

    // North Store (NO Wine, intentionally, to test errors)
    listOf(rice, milk, coke, water).forEach {
        northStore.registerProduct(it)
        northStore.addStock(it.id, 50)
    }
    println("   Inventory loaded.\n")

    // 3. Process Sales
    // --- ERRORS OCCUR HERE ---
    println("\n>> Simulating operational errors:")

    // Error 1: Product does not exist (Wine in North Branch)
    processSale(northStore, wine, 5)

    // Error 2: Insufficient stock
    processSale(northStore, coke, 1000)

    // 4. DATA GENERATION FOR TOP 5
    // We force specific sales to ensure all 6 items have data
    // so we can prove the 6th item is dropped from the report.
    println("\n>> GENERATING LEADERBOARD DATA (Selling all 6 items)...")

    // Target Ranking:
    // 1. Rice  (Total ~50)
    // 2. Coke  (Total ~40)
    // 3. Water (Total ~30)
    // 4. Milk  (Total 20)
    // 5. Wine  (Total 10)
    // 6. Soap  (Total 5) -> SHOULD BE EXCLUDED
    // Use helper function 'processSale' to keep Main clean
    processSale(northStore, water, 10)
    processSale(northStore, water, 20)  // 10 prev + 20 = 30
    processSale(centralStore, rice, 30) // 20 prev + 30 = 50
    processSale(centralStore, coke, 30) // 10 prev + 30 = 40
    processSale(centralStore, milk, 20) // New sale
    processSale(centralStore, wine, 10) // New sale
    processSale(centralStore, soap, 5)  // New sale (The Loser)
    processSale(centralStore, coke, 10)
    processSale(centralStore, rice, 20)
    println("   Data generation complete.\n")

    // 5. Final Reports

    println("==========================================")
    println("   INDIVIDUAL STORE AUDIT: ${centralStore.name.uppercase()}")
    println("==========================================")

    // 1. Requirement: Register a sale and return price
    // We sell 5 MORE units of Rice to demonstrate the return value
    val lastSale = centralStore.registerSale(rice.id, 5)

    println("1. Sale Return (${rice.name} x5):    $$lastSale")
    println("2. Total Qty Sold (${rice.name}):    ${centralStore.getQuantitySold(rice.id)}")
    println("3. Total Revenue (${rice.name}):     $${centralStore.getProductRevenue(rice.id)}")
    println("4. Store Total Revenue:            $${centralStore.getTotalRevenue()}")
    println("==========================================")

    val chain = SupermarketChain(listOf(centralStore, northStore))

    println("\n==========================================")
    println("       CONSOLIDATED FINAL REPORT")
    println("==========================================")
    println("1. Chain Total Revenue:   $${chain.getTotalRevenue()}")
    println("2. Best Performing Store: ${chain.getSupermarketWithHighestRevenue()}")
    println("3. Top Selling Products:  ${chain.getTop5SellingProducts()}")
    println("==========================================")

    // AL FINAL DEL MAIN, AGREGAMOS LA PRUEBA DE HORARIOS:

    println("\n==========================================")
    println("       OPENING HOURS CHECK")
    println("==========================================")

    // Caso 1: Lunes a las 10 AM (Ambos deberían estar abiertos)
    val check1 = chain.getOpenSupermarkets(DayOfWeek.MONDAY, 10)
    println("1. Monday 10:00 -> $check1")

    // Caso 2: Domingo a las 10 AM (Norte cierra los domingos)
    val check2 = chain.getOpenSupermarkets(DayOfWeek.SUNDAY, 10)
    println("2. Sunday 10:00 -> $check2")

    // Caso 3: Lunes a las 20:00 (Norte cierra a las 18, Centro cierra a las 22)
    val check3 = chain.getOpenSupermarkets(DayOfWeek.MONDAY, 20)
    println("3. Monday 20:00 -> $check3")

    // Caso 4: Lunes a las 23:00 (Todos cerrados)
    val check4 = chain.getOpenSupermarkets(DayOfWeek.MONDAY, 23)
    println("4. Monday 23:00 -> $check4")

    println("==========================================")
}

/**
 * Helper function to handle sales safely.
 */
fun processSale(store: Supermarket, product: Product, quantity: Int) {
    try {
        val total = store.registerSale(product.id, quantity)
        // If successful:
        println("Sale OK at ${store.name}: ${product.name} x$quantity = $$total")
    } catch (e: Exception) {
        // If failed (Supermarket throws Exception):
        // 'e.message' contains YOUR original message (e.g., "Insufficient stock...")
        println("REJECTED at ${store.name}: ${e.message}")
    }
}