import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ChallengeTests {

    private lateinit var superA: Supermarket
    private lateinit var superB: Supermarket
    private lateinit var chain: SupermarketChain

    private val meat = Product(1, "Meat", 10.0)
    private val fish = Product(2, "Fish", 20.0)
    private val chicken = Product(3, "Chicken", 5.0)

    @BeforeEach
    fun setUp() {
        superA = Supermarket(1, "Supermarket A")
        superB = Supermarket(2, "Supermarket B")

        superA.registerProduct(meat)
        superA.registerProduct(fish)
        
        superB.registerProduct(meat)
        superB.registerProduct(chicken)

        chain = SupermarketChain(listOf(superA, superB))
    }

    // --- HAPPY PATHS ---

    @Test
    fun `addStock updates quantity correctly`() {
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
    fun `chain - top 5 products formatted correctly`() {
        superA.addStock(meat.id, 100)
        superA.addStock(fish.id, 100)
        superB.addStock(meat.id, 100)
        superB.addStock(chicken.id, 100)

        superA.registerSale(meat.id, 10)
        superA.registerSale(fish.id, 5)
        
        superB.registerSale(meat.id, 20) // Meat total = 30
        superB.registerSale(chicken.id, 50)

        val top5 = chain.getTop5SellingProducts()

        assertTrue(top5.contains("Chicken: 50"))
        assertTrue(top5.contains("Meat: 30"))
        assertTrue(top5.contains("Fish: 5"))
    }

    @Test
    fun `edge case - top 5 report handles less than 5 items and excludes unsold products`() {
        // Setup: 3 products in total
        superA.registerProduct(chicken)

        superA.addStock(meat.id, 10)
        superA.addStock(fish.id, 10)
        superA.addStock(chicken.id, 10)

        // Action: Sell only 2 types (Meat and Fish). Chicken is NOT sold.
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
        // (Verify it didn't add extra separators or nulls)
        assertEquals("Meat: 5 - Fish: 2", report)
    }

    @Test
    fun `edge case - top 5 report truncates correctly when more than 5 products are sold`() {
        // 1. Setup 6 distinct products (using clear names for debugging)
        val p1 = Product(101, "Prod_1st", 10.0)
        val p2 = Product(102, "Prod_2nd", 10.0)
        val p3 = Product(103, "Prod_3rd", 10.0)
        val p4 = Product(104, "Prod_4th", 10.0)
        val p5 = Product(105, "Prod_5th", 10.0)
        val p6 = Product(106, "Prod_6th_Loser", 10.0) // Should be excluded

        // Register all in SuperA
        listOf(p1, p2, p3, p4, p5, p6).forEach {
            superA.registerProduct(it)
            superA.addStock(it.id, 100) // Give enough stock
        }

        // 2. Sell in descending order to control the ranking
        superA.registerSale(p1.id, 60)
        superA.registerSale(p2.id, 50)
        superA.registerSale(p3.id, 40)
        superA.registerSale(p4.id, 30)
        superA.registerSale(p5.id, 20)
        superA.registerSale(p6.id, 10) // Lowest sales (10 units)

        // 3. Get Report
        val report = chain.getTop5SellingProducts()

        // 4. Assertions
        
        // Check that Top 5 are present
        assertTrue(report.contains("Prod_1st: 60"))
        assertTrue(report.contains("Prod_5th: 20"))

        // CRITICAL CHECK: Check that the 6th product is NOT present
        assertFalse(report.contains("Prod_6th_Loser"), "The 6th product (lowest sales) should NOT appear in Top 5")
        
        // Optional: Verify exact format string
        val expected = "Prod_1st: 60 - Prod_2nd: 50 - Prod_3rd: 40 - Prod_4th: 30 - Prod_5th: 20"
        assertEquals(expected, report)
    }

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
        // superA and superB already have 'meat' registered in setUp()
        
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
}