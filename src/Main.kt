import java.time.DayOfWeek
import java.time.LocalTime

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
        openingHour = LocalTime.of(8, 0),
        closingHour = LocalTime.of(22, 0),
        openDays = DayOfWeek.entries.toList() // Lunes a Domingo
    )

    // Norte: Abre solo de Lunes a Viernes, de 09 a 18
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
    // --- ERRORS OCCUR HERE --
    println("\n>> Simulating operational errors:")

    // Error 1: Product does not exist (Wine in North Branch)
    processSale(northStore, wine, 5)

    // Error 2: Insufficient stock
    processSale(northStore, coke, 1000)

    // DATA GENERATION FOR TOP 5
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

    // 4. Final Reports
    // If stock is insufficient, it returns 0.0 instead of crashing.
    val lastSale = processSale(centralStore, rice, 5)

    println("==========================================")
    println("   INDIVIDUAL STORE AUDIT: ${centralStore.name.uppercase()}")
    println("==========================================")

    println("1. Sale Return (${rice.name} x5):    $$lastSale")
    println("2. Total Qty Sold (${rice.name}):    ${centralStore.getQuantitySold(rice.id)}")
    println("3. Total Revenue (${rice.name}):     $${centralStore.getProductRevenue(rice.id)}")
    println("4. Store Total Revenue:            $${centralStore.getTotalRevenue()}")
    println("==========================================")

    val chain = SupermarketChain(mutableListOf(centralStore, northStore))

    println("\n==========================================")
    println("       CHAIN MANAGEMENT TEST")
    println("==========================================")

    // 1. Estado Inicial
    println("1. Initial State:")
    println("   -> List: ${chain.getAvailableSupermarkets()}")

    // 2. Crear y Agregar nueva sucursal "Este"
    val eastStore = Supermarket(3, "East Store")
    chain.addSupermarket(eastStore)

    println("\n2. Added 'East Store':")
    println("   -> List: ${chain.getAvailableSupermarkets()}")

    // 3. Eliminarla
    chain.removeSupermarket(eastStore)

    println("\n3. Removed 'East Store':")
    println("   -> List: ${chain.getAvailableSupermarkets()}")

    println("==========================================")


    println("\n==========================================")
    println("       CONSOLIDATED FINAL REPORT")
    println("==========================================")
    println("1. Chain Total Revenue:   $${chain.getTotalRevenue()}")
    println("2. Best Performing Store: ${chain.getSupermarketWithHighestRevenue()}")
    println("3. Top Selling Products:  ${chain.getTop5SellingProducts()}")
    println("==========================================")

    println("\n==========================================")
    println("       OPENING HOURS CHECK")
    println("==========================================")

    // Caso 1: Lunes a las 10:00 (Ambos abiertos)
    val time1 = LocalTime.of(10, 0)
    println("1. Monday $time1 -> ${chain.getOpenSupermarkets(DayOfWeek.MONDAY, time1)}")

    // Caso 2: Domingo a las 10:00 (Norte cierra domingos)
    val time2 = LocalTime.of(10, 0)
    println("2. Sunday $time2 -> ${chain.getOpenSupermarkets(DayOfWeek.SUNDAY, time2)}")

    // Caso 3: Lunes a las 20:30 (Norte cierra a las 18:00)
    val time3 = LocalTime.of(20, 30)
    println("3. Monday $time3 -> ${chain.getOpenSupermarkets(DayOfWeek.MONDAY, time3)}")

    // Caso 4: Lunes a las 23:00 (Todos cerrados)
    val time4 = LocalTime.of(23, 0)
    println("4. Monday $time4 -> ${chain.getOpenSupermarkets(DayOfWeek.MONDAY, time4)}")

    println("==========================================")
}

/**
 * Helper function to handle sales safely.
 * Returns the total price of the sale, or 0.0 if the sale failed.
 */
fun processSale(store: Supermarket, product: Product, quantity: Int): Double {
    return try {
        val total = store.registerSale(product.id, quantity)
        println("Sale OK at ${store.name}: ${product.name} x$quantity = $$total")
        total // Return the actual transaction amount
    } catch (e: Exception) {
        println("REJECTED at ${store.name}: ${e.message}")
        0.0 // Return 0.0 on failure so calculations don't break
    }
}