import java.time.DayOfWeek
import java.time.LocalTime

data class StockEntry(
    val product: Product,
    var currentQuantity: Int,
    var soldQuantity: Int = 0,
    var revenueGenerated: Double = 0.0,
)

class Supermarket(
    val id: Int,
    val name: String,
    val openingHour: LocalTime = LocalTime.of(8, 0),
    val closingHour: LocalTime = LocalTime.of(22, 0),
    val openDays: List<DayOfWeek> = DayOfWeek.entries.toList()
) {
    private val inventory: MutableMap<Int, StockEntry> = mutableMapOf()

    /**
     * Checks if the store is currently accepting customers.
     * Returns true if the [day] is in [openDays] and the [time] is within
     * the [openingHour] and [closingHour] window.
     */
    fun isOpen(day: DayOfWeek, time: LocalTime): Boolean {
        if (!openDays.contains(day)) return false
        return (time == openingHour || time.isAfter(openingHour)) && time.isBefore(closingHour)
    }

    /**
     * Initializes a [product] in the local catalog with 0 stock.
     * Throws an exception if the product ID is already registered.
     */
    fun registerProduct(product: Product) {
        if (inventory.containsKey(product.id)) {
            throw IllegalArgumentException("Product with ID ${product.id} is already registered in Supermarket '$name'.")
        }
        inventory[product.id] = StockEntry(product, 0)
    }

    /**
     * Removes a [product] from the catalog.
     * Throws an exception if the product ID is not registered.
     */
    fun unregisterProduct(product: Product) {
        if (!inventory.containsKey(product.id)) {
            throw IllegalArgumentException("Cannot unregister: Product with ID ${product.id} is NOT registered in Supermarket '$name'.")
        }
        inventory.remove(product.id)
    }

    /**
     * Increases the stock of a specific [productId] by the given [quantity].
     */
    fun addStock(productId: Int, quantity: Int) {
        require(quantity > 0) { "Quantity to add must be positive. Received: $quantity" }

        val entry = inventory[productId]
            ?: throw IllegalArgumentException("Product ID $productId not found in Supermarket '$name'. Register it first.")

        entry.currentQuantity += quantity
    }

    /**
     * Processes a transaction for a specific [productId].
     * Updates inventory levels, increments sales count, and accumulates revenue.
     * Returns the total transaction value (price * quantity).
     */
    fun registerSale(productId: Int, quantity: Int): Double {
        require(quantity > 0) { "Quantity to sell must be positive. Received: $quantity" }

        val entry = inventory[productId]
            ?: throw IllegalArgumentException("Product with ID $productId does not exist in Supermarket '$name'")

        if (entry.currentQuantity < quantity) {
            throw IllegalStateException("Insufficient stock for '${entry.product.name}'. Available: ${entry.currentQuantity}, Requested: $quantity")
        }

        // Atomically update stock and sales stats
        entry.currentQuantity -= quantity
        entry.soldQuantity += quantity

        val totalSalePrice = entry.product.price * quantity
        entry.revenueGenerated += totalSalePrice

        return totalSalePrice
    }

    fun getQuantitySold(productId: Int): Int = inventory[productId]?.soldQuantity ?: 0
    fun getProductRevenue(productId: Int): Double = inventory[productId]?.revenueGenerated ?: 0.0

    // Aggregates total revenue from all products in this store
    fun getTotalRevenue(): Double = inventory.values.sumOf { it.revenueGenerated }

    // Helper to retrieve only active products for reporting
    fun getSoldEntries(): List<StockEntry> = inventory.values.filter { it.soldQuantity > 0 }
}