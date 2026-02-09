import java.time.DayOfWeek
import java.time.LocalTime

class SupermarketChain(
    private val supermarkets: MutableList<Supermarket>
) {

    fun addSupermarket(supermarket: Supermarket) {
        if (supermarkets.any { it.id == supermarket.id }) {
            throw IllegalArgumentException("Cannot add: Supermarket with ID ${supermarket.id} already exists.")
        }
        supermarkets.add(supermarket)
    }

    fun removeSupermarket(supermarket: Supermarket) {
        // removeIf devuelve 'true' si eliminó algo
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

    fun getOpenSupermarkets(day: DayOfWeek, time: LocalTime): String {
        val openList = supermarkets.filter { it.isOpen(day, time) }

        if (openList.isEmpty()) {
            return "No supermarkets open at this time."
        }
        return openList.joinToString(", ") { "${it.name} (${it.id})" }
    }
}