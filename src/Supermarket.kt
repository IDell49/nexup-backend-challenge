import java.time.DayOfWeek

/**
 * Helper data class to manage the inventory state of a specific product.
 */
data class StockEntry(
    val product: Product,
    var currentQuantity: Int,
    var soldQuantity: Int = 0,
    var revenueGenerated: Double = 0.0
)

class Supermarket(
    val id: Int,
    val name: String,
    // Objetivo Opcional
    val openingHour: Int = 8,   // Por defecto abre a las 8 (formato 0-23)
    val closingHour: Int = 22,  // Por defecto cierra a las 22 (formato 0-23)
    val openDays: List<DayOfWeek> = DayOfWeek.entries.toList() // Por defecto abre todos los días
) {
    private val inventory: MutableMap<Int, StockEntry> = mutableMapOf()

    /**
     * Lógica para determinar si el supermercado está abierto.
     * @param day El día de la semana a consultar.
     * @param hour La hora del día (0-23).
     */
    fun isOpen(day: DayOfWeek, hour: Int): Boolean {
        // 1. Verificamos si abre ese día
        if (!openDays.contains(day)) return false

        // 2. Verificamos el rango horario (ej: si cierra a las 22, a las 22:00 ya está cerrado)
        return hour >= openingHour && hour < closingHour
    }

    // --- (El resto de tu código sigue igual) ---

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