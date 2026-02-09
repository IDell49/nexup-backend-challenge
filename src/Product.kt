/**
 * Simple data carrier for catalog items.
 * Note: Price is mutable to support dynamic pricing (e.g., inflation updates).
 */
data class Product(
    val id: Int,
    val name: String,
    var price: Double
)