import java.time.DayOfWeek
import java.time.LocalTime

class SupermarketChain(
    private val supermarkets: MutableList<Supermarket>
) {

    /**
     * Adds a new [supermarket] to the chain.
     * Throws an exception if a supermarket with the same ID already exists.
     */
    fun addSupermarket(supermarket: Supermarket) {
        if (supermarkets.any { it.id == supermarket.id }) {
            throw IllegalArgumentException("Cannot add: Supermarket with ID ${supermarket.id} already exists.")
        }
        supermarkets.add(supermarket)
    }

    /**
     * Removes a [supermarket] from the chain.
     * Throws an exception if the supermarket ID is not found.
     */
    fun removeSupermarket(supermarket: Supermarket) {
        val removed = supermarkets.removeIf { it.id == supermarket.id }
        if (!removed) {
            throw IllegalArgumentException("Cannot remove: Supermarket with ID ${supermarket.id} not found.")
        }
    }

    fun getAvailableSupermarkets(): String {
        return supermarkets.joinToString(", ") { "${it.name} (ID: ${it.id})" }
    }

    fun getTotalRevenue(): Double {
        return supermarkets.sumOf { it.getTotalRevenue() }
    }

    fun getSupermarketWithHighestRevenue(): String {
        val winner = supermarkets.maxByOrNull { it.getTotalRevenue() }

        return if (winner != null) {
            "${winner.name} (ID: ${winner.id}). Total Revenue: ${winner.getTotalRevenue()}"
        } else {
            "No supermarkets or sales data available."
        }
    }

    /**
     * Generates a report of the top 5 best-selling products across the entire chain.
     * Strategy:
     * 1. Collect all sales entries from all stores.
     * 2. Group by Product and sum the sold quantities.
     * 3. Sort descending by total quantity and take the top 5.
     */
    fun getTop5SellingProducts(): String {
        return supermarkets
            .flatMap { it.getSoldEntries() }
            .groupBy { it.product }
            .mapValues { (_, entries) -> entries.sumOf { it.soldQuantity } }
            .toList()
            .sortedByDescending { (_, totalQuantity) -> totalQuantity }
            .take(5)
            .joinToString(" - ") { (product, totalQuantity) ->
                "${product.name}: $totalQuantity"
            }
    }

    /**
     * Returns a string listing all supermarkets open on the given [day] at [time].
     */
    fun getOpenSupermarkets(day: DayOfWeek, time: LocalTime): String {
        val openList = supermarkets.filter { it.isOpen(day, time) }

        if (openList.isEmpty()) {
            return "No supermarkets open at this time."
        }
        return openList.joinToString(", ") { "${it.name} (${it.id})" }
    }
}