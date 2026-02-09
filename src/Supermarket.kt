/**
 * Helper data class to manage the inventory state of a specific product.
 * It groups the product definition with its current stock and sales stats.
 */
data class StockEntry(
    val product: Product,
    var currentQuantity: Int,
    var soldQuantity: Int = 0,
    var revenueGenerated: Double = 0.0
)

class Supermarket(
    val id: Int,
    val name: String
) {
    // Internal "Database" for this supermarket.
    private val inventory: MutableMap<Int, StockEntry> = mutableMapOf()

    /**
     * Registers a NEW product in the supermarket catalog.
     * Initializes stock at 0.
     * @throws IllegalArgumentException if the product ID is already registered.
     */
    fun registerProduct(product: Product) {
        if (inventory.containsKey(product.id)) {
            throw IllegalArgumentException("Product with ID ${product.id} is already registered in Supermarket '$name'.")
        }
        // Initialize with 0 stock
        inventory[product.id] = StockEntry(product, 0)
    }

    /**
     * Adds stock to an existing product.
     * @throws IllegalArgumentException if the product has not been registered yet.
     */
    fun addStock(productId: Int, quantity: Int) {
        val entry = inventory[productId]
            ?: throw IllegalArgumentException("Product ID $productId not found in Supermarket '$name'. Please call registerProduct() first.")
        
        entry.currentQuantity += quantity
    }

    /**
     * Records a sale of a product.
     * Updates stock, sold quantity, and revenue in a single atomic operation per product.
     * @return The total price of the sale.
     * @throws IllegalArgumentException if product not found.
     * @throws IllegalStateException if stock is insufficient.
     */
    fun registerSale(productId: Int, quantity: Int): Double {
        val entry = inventory[productId]
            ?: throw IllegalArgumentException("Product with ID $productId does not exist in Supermarket '$name'")

        if (entry.currentQuantity < quantity) {
            throw IllegalStateException("Insufficient stock for '${entry.product.name}'. Available: ${entry.currentQuantity}, Requested: $quantity")
        }

        // Update state
        entry.currentQuantity -= quantity
        entry.soldQuantity += quantity

        val totalSalePrice = entry.product.price * quantity
        entry.revenueGenerated += totalSalePrice

        return totalSalePrice
    }

    // --- Getters & Helpers ---

    fun getQuantitySold(productId: Int): Int = inventory[productId]?.soldQuantity ?: 0

    fun getProductRevenue(productId: Int): Double = inventory[productId]?.revenueGenerated ?: 0.0

    fun getTotalRevenue(): Double = inventory.values.sumOf { it.revenueGenerated }

    /**
     * Helper for SupermarketChain: returns only the entries that have sales.
     */
    fun getSoldEntries(): List<StockEntry> = inventory.values.filter { it.soldQuantity > 0 }
}