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
    // Key: Product ID. Value: The complete StockEntry.
    private val inventory: MutableMap<Int, StockEntry> = mutableMapOf()

    // TODO: Implement logic to add products, record sales, and generate reports.
}