fun main() {
    println("=== SUPERMARKET MANAGEMENT SYSTEM - STARTING SIMULATION ===\n")

    // 1. Setup
    val rice = Product(1, "White Rice ", 200.0)
    val milk = Product(2, "Whole Milk", 150.0)
    val coke = Product(3, "Coca-Cola 2.5L", 500.0)
    val soap = Product(4, "Dove Soap", 100.0)
    val water = Product(5, "Mineral Water", 80.0)
    val wine = Product(6, "Cabernet Sauvignon Wine", 1200.0)

    val centralStore = Supermarket(1, "Central Branch")
    val northStore = Supermarket(2, "North Branch")

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
    println(">> Processing daily sales...")

    // Use helper function 'processSale' to keep Main clean
    processSale(centralStore, coke, 10)
    processSale(centralStore, rice, 20)

    // --- ERRORS OCCUR HERE ---
    // Thanks to the try-catch inside 'processSale', the program DOES NOT stop.
    // It simply prints the error message defined in Supermarket and continues.

    println("\n>> Simulating operational errors:")

    // Error 1: Product does not exist (Wine in North Branch)
    processSale(northStore, wine, 5)

    // Error 2: Insufficient stock
    processSale(northStore, coke, 1000)

    // Valid sale AFTER errors (Shows program is still alive)
    println("\n>> Recovering operation...")
    processSale(northStore, water, 10)

    println("\n   Register closed.\n")

    // 4. Final Reports
    val chain = SupermarketChain(listOf(centralStore, northStore))

    println("==========================================")
    println("       CONSOLIDATED FINAL REPORT")
    println("==========================================")
    println("1. Chain Total Revenue:   $${chain.getTotalRevenue()}")
    println("2. Best Performing Store: ${chain.getSupermarketWithHighestRevenue()}")
    println("3. Top Selling Products:  ${chain.getTop5SellingProducts()}")
    println("==========================================")
}

/**
 * Helper function to handle sales safely.
 * It catches exceptions thrown by the Supermarket logic (Backend)
 * and presents them nicely to the user (Frontend/Console).
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