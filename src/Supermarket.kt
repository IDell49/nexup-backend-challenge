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
    // UPDATE 1: Use LocalTime.of() for default values
    val openingHour: LocalTime = LocalTime.of(8, 0),
    val closingHour: LocalTime = LocalTime.of(22, 0),
    val openDays: List<DayOfWeek> = DayOfWeek.entries.toList()
) {
    private val inventory: MutableMap<Int, StockEntry> = mutableMapOf()

    /**
     * Determines if the supermarket is open.
     */
    fun isOpen(day: DayOfWeek, time: LocalTime): Boolean {
        // 1. Check day
        if (!openDays.contains(day)) return false
        // 2. Check time range
        return (time == openingHour || time.isAfter(openingHour)) && time.isBefore(closingHour)
    }

    fun registerProduct(product: Product) {
        if (inventory.containsKey(product.id)) {
            throw IllegalArgumentException("Product with ID ${product.id} is already registered in Supermarket '$name'.")
        }
        inventory[product.id] = StockEntry(product, 0)
    }

    fun addStock(productId: Int, quantity: Int) {
        require(quantity > 0) { "Quantity to add must be bigger than 0. Received: $quantity" }
        val entry = inventory[productId]
            ?: throw IllegalArgumentException("Product ID $productId not found in Supermarket '$name'. Please call registerProduct() first.")
        entry.currentQuantity += quantity
    }

    fun registerSale(productId: Int, quantity: Int): Double {
        require(quantity > 0) { "Quantity to sell must be bigger than 0. Received: $quantity" }
        val entry = inventory[productId]
            ?: throw IllegalArgumentException("Product with ID $productId does not exist in Supermarket '$name'")
        if (entry.currentQuantity < quantity) {
            throw IllegalStateException("Insufficient stock for '${entry.product.name}'. Available: ${entry.currentQuantity}, Requested: $quantity")
        }
        entry.currentQuantity -= quantity
        entry.soldQuantity += quantity
        val totalSalePrice = entry.product.price * quantity
        entry.revenueGenerated += totalSalePrice
        return totalSalePrice
    }

    fun getQuantitySold(productId: Int): Int = inventory[productId]?.soldQuantity ?: 0
    fun getProductRevenue(productId: Int): Double = inventory[productId]?.revenueGenerated ?: 0.0
    fun getTotalRevenue(): Double = inventory.values.sumOf { it.revenueGenerated }
    fun getSoldEntries(): List<StockEntry> = inventory.values.filter { it.soldQuantity > 0 }
}