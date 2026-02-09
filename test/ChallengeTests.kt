import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.DayOfWeek
import java.time.LocalTime

class ChallengeTests {

    private lateinit var superA: Supermarket
    private lateinit var superB: Supermarket
    private lateinit var chain: SupermarketChain

    // Define standard test products available for all tests
    private val meat = Product(1, "Meat", 10.0)
    private val fish = Product(2, "Fish", 20.0)
    private val chicken = Product(3, "Chicken", 5.0)

    @BeforeEach
    fun setUp() {
        superA = Supermarket(1, "Supermarket A")
        superB = Supermarket(2, "Supermarket B")

        // BROAD SETUP: Register the full catalog to ALL supermarkets.
        // This allows any test to use any product in any store without extra setup.
        listOf(meat, fish, chicken).forEach {
            superA.registerProduct(it)
            superB.registerProduct(it)
        }

        chain = SupermarketChain(mutableListOf(superA, superB))
    }

    // --- HAPPY PATHS ---

    @Test
    fun `addStock updates quantity correctly and completes sale`() {
        superA.addStock(meat.id, 50)
        assertEquals(0, superA.getQuantitySold(meat.id))

        assertDoesNotThrow {
            superA.registerSale(meat.id, 50)
        }
    }

    @Test
    fun `registerSale updates stock, sales count and revenue`() {
        superA.addStock(fish.id, 10)

        val saleTotal = superA.registerSale(fish.id, 5)

        assertEquals(100.0, saleTotal)
        assertEquals(5, superA.getQuantitySold(fish.id))
        assertEquals(100.0, superA.getProductRevenue(fish.id))
        assertEquals(100.0, superA.getTotalRevenue())
    }

    // --- EDGE CASES & VALIDATION ---

    @Test
    fun `validation - prevent adding negative or zero stock`() {
        // Negative
        val ex1 = assertThrows<IllegalArgumentException> {
            superA.addStock(meat.id, -10)
        }
        assertTrue(ex1.message!!.contains("must be bigger than 0"))

        // Zero
        val ex2 = assertThrows<IllegalArgumentException> {
            superA.addStock(meat.id, 0)
        }
        assertTrue(ex2.message!!.contains("must be bigger than 0"))
    }

    @Test
    fun `validation - prevent selling negative or zero quantity`() {
        superA.addStock(meat.id, 10)

        // Negative
        assertThrows<IllegalArgumentException> {
            superA.registerSale(meat.id, -1)
        }

        // Zero
        assertThrows<IllegalArgumentException> {
            superA.registerSale(meat.id, 0)
        }
    }

    @Test
    fun `validation - cannot add stock to unregistered product`() {
        val unknownId = 999
        val exception = assertThrows<IllegalArgumentException> {
            superA.addStock(unknownId, 10)
        }
        assertTrue(exception.message!!.contains("registerProduct() first"))
    }

    @Test
    fun `validation - cannot sell insufficient stock`() {
        superA.addStock(meat.id, 10)

        assertThrows<IllegalStateException> {
            superA.registerSale(meat.id, 11)
        }
    }

    // --- CHAIN INTEGRATION ---

    @Test
    fun `chain - calculate total revenue correctly`() {
        superA.addStock(meat.id, 100)
        superB.addStock(chicken.id, 100)

        superA.registerSale(meat.id, 10)    // $100
        superB.registerSale(chicken.id, 20) // $100

        assertEquals(200.0, chain.getTotalRevenue())
    }

    @Test
    fun `chain - identify supermarket with highest revenue`() {
        superA.addStock(meat.id, 100)
        superB.addStock(chicken.id, 200)

        superA.registerSale(meat.id, 10)     // $100
        superB.registerSale(chicken.id, 200) // $1000

        val result = chain.getSupermarketWithHighestRevenue()

        assertTrue(result.contains("Supermarket B"))
        assertTrue(result.contains("1000.0"))
    }

    @Test
    fun `chain - top 5 products formatted correctly and ordered by sales volume`() {
        // 1. Setup 3 extra products to reach a total of 6 items
        val chips = Product(4, "Chips", 10.0)
        val soda = Product(5, "Soda", 10.0)
        val candy = Product(6, "Candy", 10.0) // This will be the loser (6th place)

        // Register new items
        listOf(chips, soda, candy).forEach {
            superA.registerProduct(it)
            superA.addStock(it.id, 100)
        }

        // Ensure stock for existing items (Meat, Fish, Chicken)
        superA.addStock(meat.id, 100)
        superA.addStock(fish.id, 100)
        superA.addStock(chicken.id, 100)

        // 2. Execute Sales to create a specific ranking
        // Ranking Goal:
        // #1 Chicken: 60
        // #2 Meat:    50
        // #3 Soda:    40
        // #4 Chips:   30
        // #5 Fish:    20
        // #6 Candy:   10 (Should be excluded)

        superA.registerSale(chicken.id, 60)
        superA.registerSale(meat.id, 50)
        superA.registerSale(soda.id, 40)
        superA.registerSale(chips.id, 30)
        superA.registerSale(fish.id, 20)
        superA.registerSale(candy.id, 10)

        // 3. Get Report
        val top5 = chain.getTop5SellingProducts()

        // 4. Assert Exact Match (Enforces Order and Exclusion)
        val expected = "Chicken: 60 - Meat: 50 - Soda: 40 - Chips: 30 - Fish: 20"

        assertEquals(expected, top5, "Report must be ordered descending by sales and contain exactly 5 items")

        // Double check exclusion just in case
        assertFalse(top5.contains("Candy"), "The 6th product (Candy) should not appear in the Top 5")
    }

    @Test
    fun `edge case - top 5 report handles less than 5 items and excludes unsold products`() {
        // Setup: All products are already registered via setUp().
        // We only add stock to the ones we intend to sell.
        superA.addStock(meat.id, 10)
        superA.addStock(fish.id, 10)
        // Chicken is registered but has 0 stock and 0 sales.

        // Action: Sell only 2 types (Meat and Fish).
        superA.registerSale(meat.id, 5)
        superA.registerSale(fish.id, 2)

        val report = chain.getTop5SellingProducts()

        // Assertions:
        // 1. Should contain Meat and Fish
        assertTrue(report.contains("Meat: 5"))
        assertTrue(report.contains("Fish: 2"))

        // 2. Should NOT contain Chicken (Sales = 0)
        assertFalse(report.contains("Chicken"), "Unsold products should not appear in the report")

        // 3. Format check: Should look like "Meat: 5 - Fish: 2"
        assertEquals("Meat: 5 - Fish: 2", report)
    }


    // --- Architecture ---
    
    @Test
    fun `architecture - revenue history remains stable when product price changes (Inflation Test)`() {
        // 1. Setup: Product costs $10 initially
        val volatileProduct = Product(999, "Gold", 10.0)
        superA.registerProduct(volatileProduct)
        superA.addStock(volatileProduct.id, 20)

        // 2. Sell 1 unit at $10
        superA.registerSale(volatileProduct.id, 1) // Revenue += 10

        // 3. Action: Change price to $50 (Inflation!)
        volatileProduct.price = 50.0

        // 4. Sell 1 unit at NEW price $50
        superA.registerSale(volatileProduct.id, 1) // Revenue += 50

        // 5. Assertions
        // Total should be 10 + 50 = 60.
        // If logic was wrong (calculating purely on current price), it would be 2 * 50 = 100.
        assertEquals(60.0, superA.getTotalRevenue(), "Historical revenue should be preserved despite price changes")
    }

    @Test
    fun `architecture - chain aggregates sales of the same product across different supermarkets`() {
        // Setup: Same product 'Meat' in both supermarkets
        superA.addStock(meat.id, 100)
        superB.addStock(meat.id, 100)

        // Action: Sales in different locations
        superA.registerSale(meat.id, 5) // 5 sales in A
        superB.registerSale(meat.id, 7) // 7 sales in B

        // Execution
        val topProducts = chain.getTop5SellingProducts()

        // Assertion
        // Should sum 5 + 7 = 12
        assertTrue(topProducts.contains("Meat: 12"), "Should aggregate sales from all branches (5+7=12)")
    }

    @Test
    fun `management - successfully add a new supermarket to the chain`() {
        val superC = Supermarket(3, "Supermarket C")

        chain.addSupermarket(superC)

        val available = chain.getAvailableSupermarkets()
        assertTrue(available.contains("Supermarket C"), "List should contain the new supermarket")
        assertTrue(available.contains("ID: 3"), "List should contain the new ID")
    }

    @Test
    fun `management - prevent adding supermarket with duplicate ID`() {
        // superA has ID 1. Try to add another with ID 1.
        val fakeSuper = Supermarket(1, "Impostor")

        val exception = assertThrows<IllegalArgumentException> {
            chain.addSupermarket(fakeSuper)
        }

        assertTrue(exception.message!!.contains("already exists"))
    }

    @Test
    fun `management - successfully remove a supermarket from the chain`() {
        // Remove superA (ID 1)
        chain.removeSupermarket(superA)

        val available = chain.getAvailableSupermarkets()

        assertFalse(available.contains("Supermarket A"), "Supermarket A should be removed")
        assertTrue(available.contains("Supermarket B"), "Supermarket B should still exist")
    }

    @Test
    fun `management - prevent removing a non-existent supermarket`() {
        val ghostSuper = Supermarket(999, "Ghost Store")

        val exception = assertThrows<IllegalArgumentException> {
            chain.removeSupermarket(ghostSuper)
        }

        assertTrue(exception.message!!.contains("not found"))
    }

    @Test
    fun `schedule - chain correctly lists only open supermarkets`() {
        // Setup:
        // superA (Default): 08-22, All Days
        // Custom B: 08-12, All Days (Closes early)
        val earlyStore = Supermarket(2, "Early Bird", LocalTime.of(8, 0), LocalTime.of(12, 0))

        // Re-initialize chain for this specific test
        chain = SupermarketChain(mutableListOf(superA, earlyStore))

        // Scenario 1: 10:00 AM (Both Open)
        val bothOpen = chain.getOpenSupermarkets(DayOfWeek.MONDAY, LocalTime.of(10, 0))
        assertTrue(bothOpen.contains("Supermarket A"))
        assertTrue(bothOpen.contains("Early Bird"))

        // Scenario 2: 01:00 PM (SuperA Open, EarlyStore Closed)
        val oneOpen = chain.getOpenSupermarkets(DayOfWeek.MONDAY, LocalTime.of(13, 0))
        assertTrue(oneOpen.contains("Supermarket A"))
        assertFalse(oneOpen.contains("Early Bird"))

        // Scenario 3: 11:00 PM (Both Closed)
        val allClosed = chain.getOpenSupermarkets(DayOfWeek.MONDAY, LocalTime.of(23, 0))
        assertEquals("No supermarkets open at this time.", allClosed)
    }
    // --- BONUS OBJECTIVE: SCHEDULES ---

    @Test
    fun `schedule - supermarket validates opening hours correctly (Inclusive Start, Exclusive End)`() {
        // Setup: Store opens 09:00 - 18:00
        val officeStore = Supermarket(
            3, "Office Store",
            LocalTime.of(9, 0),
            LocalTime.of(18, 0),
            DayOfWeek.entries.toList()
        )

        // 1. Open
        assertTrue(officeStore.isOpen(DayOfWeek.MONDAY, LocalTime.of(9, 0)), "Should be open exactly at opening hour")
        assertTrue(officeStore.isOpen(DayOfWeek.MONDAY, LocalTime.of(12, 30)), "Should be open in the middle of the day")

        // 2. Closed (Before)
        assertFalse(officeStore.isOpen(DayOfWeek.MONDAY, LocalTime.of(8, 59)), "Should be closed 1 minute before opening")

        // 3. Closed (After/At closing)
        // Logic check: time.isBefore(closingHour) -> 18:00 is NOT before 18:00
        assertFalse(officeStore.isOpen(DayOfWeek.MONDAY, LocalTime.of(18, 0)), "Should be closed exactly at closing hour")
        assertFalse(officeStore.isOpen(DayOfWeek.MONDAY, LocalTime.of(18, 0, 1)), "Should be closed after closing hour")
    }

    @Test
    fun `schedule - supermarket validates opening days correctly`() {
        // Setup: Store opens only Monday and Friday
        val partialStore = Supermarket(
            4, "Part-Time Store",
            LocalTime.of(8, 0),
            LocalTime.of(20, 0),
            listOf(DayOfWeek.MONDAY, DayOfWeek.FRIDAY)
        )

        // Open Days
        assertTrue(partialStore.isOpen(DayOfWeek.MONDAY, LocalTime.of(10, 0)))
        assertTrue(partialStore.isOpen(DayOfWeek.FRIDAY, LocalTime.of(10, 0)))

        // Closed Days
        assertFalse(partialStore.isOpen(DayOfWeek.TUESDAY, LocalTime.of(10, 0)))
        assertFalse(partialStore.isOpen(DayOfWeek.SUNDAY, LocalTime.of(10, 0)))
    }
}