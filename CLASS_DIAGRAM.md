
```mermaid
classDiagram
class Product {
    +Int id
    +String name
    +Double price
}

class StockEntry {
    +Product product
    +Int currentQuantity
    +Int soldQuantity
    +Double revenueGenerated
}

class Supermarket {
    +Int id
    +String name
    +LocalTime openingHour
    +LocalTime closingHour
    +List~DayOfWeek~ openDays
    -Map~Int StockEntry~ inventory
    +isOpen(day, time) Boolean
    +registerProduct(product)
    +unregisterProduct(product)
    +addStock(productId, quantity)
    +registerSale(productId, quantity) Double
    +getQuantitySold() Int
    +getProductRevenue() Double
    +getTotalRevenue() Double
    +getSoldEntries() List~StockEntry~
}

class SupermarketChain {
    -List~Supermarket~ supermarkets
    +addSupermarket(supermarket)
    +removeSupermarket(supermarket)
    +getAvailableSupermarket(): String
    +getTotalRevenue() Double
    +getSupermarketWithHighestRevenue() String
    +getTop5SellingProducts() String
    +getOpenSupermarkets(day, time) String
}

Supermarket "1" *-- "*" StockEntry : contains
StockEntry "1" o-- "1" Product : references
SupermarketChain "1" o-- "*" Supermarket : manages