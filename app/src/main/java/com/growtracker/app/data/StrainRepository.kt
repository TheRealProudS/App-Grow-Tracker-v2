package com.growtracker.app.data

// Reuse the serializable models from GrowModels.kt (SeedManufacturer, StrainInfo)
object StrainRepository {
    // Seeded dataset using existing types
    val manufacturers: List<SeedManufacturer> = listOf(
        SeedManufacturer(
            name = "Green House Seeds",
            strains = listOf(
                StrainInfo(name = "Super Lemon Haze", thcContent = "20", cbdContent = "1.2", type = PlantType.FEMINIZED_SATIVA),
                StrainInfo(name = "White Widow", thcContent = "18", cbdContent = "0.5", type = PlantType.FEMINIZED_HYBRID)
            )
        ),
        SeedManufacturer(
            name = "Royal Queen Seeds",
            strains = listOf(
                StrainInfo(name = "Amnesia Haze", thcContent = "22", cbdContent = "0.8", type = PlantType.FEMINIZED_SATIVA),
                StrainInfo(name = "Northern Light", thcContent = "16", cbdContent = "0.3", type = PlantType.FEMINIZED_INDICA),
                StrainInfo(name = "Royal Cookies", thcContent = "", cbdContent = "", type = PlantType.FEMINIZED_HYBRID),
                StrainInfo(name = "Royal Gorilla", thcContent = "", cbdContent = "", type = PlantType.FEMINIZED_HYBRID),
                StrainInfo(name = "Royal Highness (CBD)", thcContent = "", cbdContent = "", type = PlantType.FEMINIZED_HYBRID),
                StrainInfo(name = "Royal AK", thcContent = "", cbdContent = "", type = PlantType.FEMINIZED_HYBRID),
                StrainInfo(name = "Quick One (Auto)", thcContent = "", cbdContent = "", type = PlantType.AUTOFLOWER),
                StrainInfo(name = "Critical", thcContent = "", cbdContent = "", type = PlantType.FEMINIZED_HYBRID),
                StrainInfo(name = "White Widow", thcContent = "18", cbdContent = "0.5", type = PlantType.FEMINIZED_HYBRID),
                StrainInfo(name = "Royal Moby", thcContent = "", cbdContent = "", type = PlantType.FEMINIZED_SATIVA)
            )
        ),
        SeedManufacturer(
            name = "FastBuds",
            strains = listOf(
                StrainInfo(name = "Runtz Auto", thcContent = "19", cbdContent = "0.4", type = PlantType.AUTOFLOWER),
                StrainInfo(name = "Gorilla Glue Auto", thcContent = "20", cbdContent = "0.5", type = PlantType.AUTOFLOWER)
            )
        ),
        SeedManufacturer(
            name = "FemSeeds",
            strains = listOf(
                StrainInfo(name = "Blue Dream", thcContent = "21", cbdContent = "0.6", type = PlantType.FEMINIZED_SATIVA),
                StrainInfo(name = "OG Kush", thcContent = "18", cbdContent = "0.4", type = PlantType.FEMINIZED_INDICA)
            )
        ),
        // Additional common seed manufacturers to populate the dropdown
        SeedManufacturer(
            name = "Sensi Seeds",
            strains = listOf(
                StrainInfo(name = "Super Skunk", thcContent = "17", cbdContent = "0.5", type = PlantType.FEMINIZED_HYBRID),
                StrainInfo(name = "Jack Herer", thcContent = "", cbdContent = "", type = PlantType.FEMINIZED_SATIVA),
                StrainInfo(name = "Skunk #1", thcContent = "16", cbdContent = "0.6", type = PlantType.FEMINIZED_HYBRID),
                StrainInfo(name = "Hindu Kush", thcContent = "", cbdContent = "", type = PlantType.FEMINIZED_INDICA),
                StrainInfo(name = "Early Skunk", thcContent = "", cbdContent = "", type = PlantType.FEMINIZED_HYBRID),
                StrainInfo(name = "Sensi Amnesia XXL Automatic", thcContent = "", cbdContent = "", type = PlantType.AUTOFLOWER),
                StrainInfo(name = "Sticky Orange XXL Automatic", thcContent = "", cbdContent = "", type = PlantType.AUTOFLOWER),
                StrainInfo(name = "Shiva Shanti", thcContent = "", cbdContent = "", type = PlantType.FEMINIZED_INDICA),
                StrainInfo(name = "Pineapple Chunk (Sensi collaboration)", thcContent = "", cbdContent = "", type = PlantType.FEMINIZED_HYBRID)
            )
        ),
        SeedManufacturer(
            name = "Barney's Farm",
            strains = listOf(
                StrainInfo(name = "Pineapple Chunk", thcContent = "26", cbdContent = "", type = PlantType.FEMINIZED_HYBRID),
                StrainInfo(name = "Watermelon Zkittlez", thcContent = "27", cbdContent = "", type = PlantType.FEMINIZED_HYBRID),
                StrainInfo(name = "Blue Dream", thcContent = "26", cbdContent = "", type = PlantType.FEMINIZED_HYBRID),
                StrainInfo(name = "Jealousy", thcContent = "29", cbdContent = "", type = PlantType.FEMINIZED_HYBRID),
                StrainInfo(name = "Mendo Breath", thcContent = "22", cbdContent = "", type = PlantType.FEMINIZED_HYBRID),
                StrainInfo(name = "White Runtz", thcContent = "29", cbdContent = "", type = PlantType.FEMINIZED_HYBRID),
                StrainInfo(name = "Gorilla Zkittlez", thcContent = "26", cbdContent = "", type = PlantType.FEMINIZED_HYBRID),
                StrainInfo(name = "Pineapple Express Auto", thcContent = "24", cbdContent = "", type = PlantType.AUTOFLOWER),
                StrainInfo(name = "Purple Punch Auto", thcContent = "22", cbdContent = "", type = PlantType.AUTOFLOWER),
                StrainInfo(name = "LSD Auto", thcContent = "24", cbdContent = "", type = PlantType.AUTOFLOWER),
                StrainInfo(name = "Bubblegum Gelato", thcContent = "24", cbdContent = "", type = PlantType.FEMINIZED_HYBRID),
                StrainInfo(name = "Papaya Frosting", thcContent = "28", cbdContent = "", type = PlantType.FEMINIZED_HYBRID),
                StrainInfo(name = "Insane OG", thcContent = "27", cbdContent = "", type = PlantType.FEMINIZED_HYBRID),
                StrainInfo(name = "Tropicana Cherry", thcContent = "28", cbdContent = "", type = PlantType.FEMINIZED_HYBRID),
                StrainInfo(name = "Trainwreck", thcContent = "25", cbdContent = "", type = PlantType.FEMINIZED_HYBRID),
                StrainInfo(name = "Jealousy Fem", thcContent = "29", cbdContent = "", type = PlantType.FEMINIZED_HYBRID)
            )
        ),
        SeedManufacturer(
            name = "Dinafem",
            strains = listOf(
                StrainInfo(name = "Critical +", thcContent = "20", cbdContent = "0.4", type = PlantType.FEMINIZED_HYBRID)
            )
        ),
        SeedManufacturer(
            name = "Dutch Passion",
            strains = listOf(
                StrainInfo(name = "Blueberry", thcContent = "18", cbdContent = "0.5", type = PlantType.FEMINIZED_INDICA)
            )
        ),
        SeedManufacturer(
            name = "Humboldt Seed Organization",
            strains = listOf(
                StrainInfo(name = "Lemon OG", thcContent = "19", cbdContent = "0.4", type = PlantType.FEMINIZED_HYBRID)
            )
        ),
        SeedManufacturer(
            name = "Seedsman",
            strains = listOf(
                StrainInfo(name = "Skunk #1", thcContent = "16", cbdContent = "0.6", type = PlantType.FEMINIZED_HYBRID)
            )
        ),
        SeedManufacturer(
            name = "Nirvana Seeds",
            strains = listOf(
                StrainInfo(name = "White Widow", thcContent = "18", cbdContent = "0.5", type = PlantType.FEMINIZED_HYBRID)
            )
        )
        ,
        // Additional manufacturers discovered on CannaConnection / HansBrainfood
        SeedManufacturer(
            name = "Bud Voyage",
            strains = listOf(
                StrainInfo(name = "Fat Bastard AUTO", thcContent = "", cbdContent = "", type = PlantType.AUTOFLOWER),
                StrainInfo(name = "Fat Bastard Fem", thcContent = "", cbdContent = "", type = PlantType.FEMINIZED_HYBRID),
                StrainInfo(name = "Blackberry Moonrocks AUTO", thcContent = "", cbdContent = "", type = PlantType.AUTOFLOWER),
                StrainInfo(name = "Blackberry Moonrocks Fem", thcContent = "", cbdContent = "", type = PlantType.FEMINIZED_HYBRID),
                StrainInfo(name = "Hellfire OG AUTO", thcContent = "", cbdContent = "", type = PlantType.AUTOFLOWER),
                StrainInfo(name = "Hellfire OG Fem", thcContent = "", cbdContent = "", type = PlantType.FEMINIZED_HYBRID),
                StrainInfo(name = "Moonshine Cookies AUTO", thcContent = "", cbdContent = "", type = PlantType.AUTOFLOWER),
                StrainInfo(name = "Back 2 Future AUTO", thcContent = "", cbdContent = "", type = PlantType.AUTOFLOWER),
                StrainInfo(name = "Black Runtz Fem", thcContent = "", cbdContent = "", type = PlantType.FEMINIZED_HYBRID),
                StrainInfo(name = "Bubble Gum Sherb Fem", thcContent = "", cbdContent = "", type = PlantType.FEMINIZED_HYBRID),
                StrainInfo(name = "Pineapple Fruz Fem", thcContent = "", cbdContent = "", type = PlantType.FEMINIZED_HYBRID),
                StrainInfo(name = "Runtz AUTO", thcContent = "", cbdContent = "", type = PlantType.AUTOFLOWER),
                StrainInfo(name = "Florida Sunrise AUTO", thcContent = "", cbdContent = "", type = PlantType.AUTOFLOWER),
                StrainInfo(name = "Northern Lights AUTO", thcContent = "", cbdContent = "", type = PlantType.AUTOFLOWER),
                StrainInfo(name = "Green Gelato AUTO", thcContent = "", cbdContent = "", type = PlantType.AUTOFLOWER),
                StrainInfo(name = "Purple Punch AUTO", thcContent = "", cbdContent = "", type = PlantType.AUTOFLOWER),
                StrainInfo(name = "White Widow AUTO", thcContent = "", cbdContent = "", type = PlantType.AUTOFLOWER),
                StrainInfo(name = "Strawberry Smile AUTO", thcContent = "", cbdContent = "", type = PlantType.AUTOFLOWER)
            )
        ),
        SeedManufacturer(
            name = "Zamnesia",
            strains = listOf()
        ),
        SeedManufacturer(
            name = "No Mercy Seeds",
            strains = listOf()
        ),
        SeedManufacturer(
            name = "Paradise Seeds",
            strains = listOf()
        ),
        SeedManufacturer(
            name = "Philosopher Seeds",
            strains = listOf()
        ),
        SeedManufacturer(
            name = "Positronics Seeds",
            strains = listOf()
        ),
        SeedManufacturer(
            name = "Pyramid Seeds",
            strains = listOf()
        ),
        SeedManufacturer(
            name = "Rare Dankness",
            strains = listOf()
        ),
        SeedManufacturer(
            name = "Reggae Seeds",
            strains = listOf()
        ),
        SeedManufacturer(
            name = "Reserva Privada",
            strains = listOf()
        ),
        SeedManufacturer(
            name = "Resin Seeds",
            strains = listOf()
        ),
        SeedManufacturer(
            name = "Ripper Seeds",
            strains = listOf()
        ),
        SeedManufacturer(
            name = "Roor",
            strains = listOf()
        ),
        SeedManufacturer(
            name = "Sensation Seeds",
            strains = listOf()
        ),
        SeedManufacturer(
            name = "Serious Seeds",
            strains = listOf()
        ),
        SeedManufacturer(
            name = "Sickmeds Seeds",
            strains = listOf()
        ),
        SeedManufacturer(
            name = "Soma Seeds",
            strains = listOf()
        ),
        SeedManufacturer(
            name = "Spliff Seeds",
            strains = listOf()
        ),
        SeedManufacturer(
            name = "Strain Hunters",
            strains = listOf()
        ),
        SeedManufacturer(
            name = "Subcool's The Dank",
            strains = listOf()
        ),
        SeedManufacturer(
            name = "Super Sativa Seed Club",
            strains = listOf()
        ),
        SeedManufacturer(
            name = "Super Strains",
            strains = listOf()
        ),
        SeedManufacturer(
            name = "Sweet Seeds",
            strains = listOf()
        ),
        SeedManufacturer(
            name = "Tropical Seeds Company",
            strains = listOf()
        ),
        SeedManufacturer(
            name = "T.H.Seeds",
            strains = listOf()
        ),
        SeedManufacturer(
            name = "Top Tao Seeds",
            strains = listOf()
        ),
        SeedManufacturer(
            name = "VIP Seeds",
            strains = listOf()
        ),
        SeedManufacturer(
            name = "Vision Seeds",
            strains = listOf()
        ),
        SeedManufacturer(
            name = "White Label Seeds",
            strains = listOf()
        ),
        SeedManufacturer(
            name = "World Of Seeds",
            strains = listOf()
        ),
        SeedManufacturer(
            name = "Zambeza Seeds",
            strains = listOf()
        ),
        SeedManufacturer(
            name = "Zativo",
            strains = listOf()
        )
        ,
        SeedManufacturer(
            name = "Lucky Hemp",
            strains = listOf(
                StrainInfo(name = "Permanent Marker", thcContent = "34", cbdContent = "", type = PlantType.FEMINIZED_HYBRID),
                StrainInfo(name = "MAC (Automatik)", thcContent = "21-23", cbdContent = "", type = PlantType.AUTOFLOWER),
                StrainInfo(name = "Banana Kush", thcContent = "26", cbdContent = "", type = PlantType.FEMINIZED_HYBRID),
                StrainInfo(name = "Do-Si-Dos", thcContent = "25", cbdContent = "", type = PlantType.AUTOFLOWER),
                StrainInfo(name = "Ice Cream Cake US", thcContent = "20-25", cbdContent = "", type = PlantType.FEMINIZED_HYBRID),
                StrainInfo(name = "Tropicana US", thcContent = "21-25", cbdContent = "", type = PlantType.FEMINIZED_HYBRID),
                StrainInfo(name = "Strawberry Tart", thcContent = "23", cbdContent = "", type = PlantType.FEMINIZED_HYBRID),
                StrainInfo(name = "Watermelon Zkittlez", thcContent = "22-25", cbdContent = "", type = PlantType.FEMINIZED_HYBRID),
                StrainInfo(name = "Amnesia Haze", thcContent = "22-24", cbdContent = "", type = PlantType.FEMINIZED_SATIVA),
                StrainInfo(name = "Granddaddy Purple", thcContent = "19", cbdContent = "", type = PlantType.FEMINIZED_HYBRID),
                StrainInfo(name = "Gelato", thcContent = "22-24", cbdContent = "", type = PlantType.AUTOFLOWER),
                StrainInfo(name = "Permanent Marker Fem", thcContent = "34", cbdContent = "", type = PlantType.FEMINIZED_HYBRID),
                StrainInfo(name = "MAC AUTO", thcContent = "21-23", cbdContent = "", type = PlantType.AUTOFLOWER),
                StrainInfo(name = "Banana Kush Fem", thcContent = "26", cbdContent = "", type = PlantType.FEMINIZED_HYBRID),
                StrainInfo(name = "Cookies", thcContent = "20", cbdContent = "", type = PlantType.AUTOFLOWER),
                StrainInfo(name = "Georgia Pie", thcContent = "25-27", cbdContent = "", type = PlantType.FEMINIZED_HYBRID),
                StrainInfo(name = "OG Kush", thcContent = "22", cbdContent = "", type = PlantType.AUTOFLOWER),
                StrainInfo(name = "Bubba Kush US", thcContent = "20-23", cbdContent = "", type = PlantType.FEMINIZED_HYBRID),
                StrainInfo(name = "Skittlez", thcContent = "20", cbdContent = "", type = PlantType.AUTOFLOWER),
                StrainInfo(name = "Cookies & Cream", thcContent = "26", cbdContent = "", type = PlantType.FEMINIZED_HYBRID),
                StrainInfo(name = "Runtz x Wedding Cake", thcContent = "27", cbdContent = "", type = PlantType.FEMINIZED_HYBRID),
                StrainInfo(name = "Amnesia AUTO", thcContent = "20", cbdContent = "", type = PlantType.AUTOFLOWER),
                StrainInfo(name = "Diesel AUTO", thcContent = "20-22", cbdContent = "", type = PlantType.AUTOFLOWER),
                StrainInfo(name = "Sour Tangie AUTO", thcContent = "18-22", cbdContent = "", type = PlantType.AUTOFLOWER),
                StrainInfo(name = "Tangerine Dream AUTO", thcContent = "18-22", cbdContent = "", type = PlantType.AUTOFLOWER),
                StrainInfo(name = "Hardcore OG AUTO", thcContent = "24-28", cbdContent = "", type = PlantType.AUTOFLOWER),
                StrainInfo(name = "Honey M CBD", thcContent = "", cbdContent = "9-18", type = PlantType.FEMINIZED_HYBRID),
                StrainInfo(name = "Charlottes Web CBD", thcContent = "0.5-0.8", cbdContent = "16-18", type = PlantType.FEMINIZED_HYBRID),
                StrainInfo(name = "Pure CBG", thcContent = "", cbdContent = "", type = PlantType.FEMINIZED_HYBRID)
            )
            // Additional items discovered on Lucky Hemp collection page
            .plus(listOf(
                StrainInfo(name = "Super Silver Haze", thcContent = "22-24", cbdContent = "", type = PlantType.FEMINIZED_SATIVA),
                StrainInfo(name = "White Widow", thcContent = "20-22", cbdContent = "", type = PlantType.FEMINIZED_HYBRID),
                StrainInfo(name = "Critical", thcContent = "20", cbdContent = "", type = PlantType.FEMINIZED_HYBRID),
                StrainInfo(name = "Mimosa", thcContent = "27", cbdContent = "", type = PlantType.FEMINIZED_HYBRID),
                StrainInfo(name = "Blueberry Auto", thcContent = "22-23", cbdContent = "", type = PlantType.AUTOFLOWER),
                StrainInfo(name = "Wedding Cake", thcContent = "22", cbdContent = "", type = PlantType.FEMINIZED_HYBRID),
                StrainInfo(name = "White Fire OG AUTO", thcContent = "24", cbdContent = "", type = PlantType.AUTOFLOWER),
                StrainInfo(name = "Tropical Cookies AUTO", thcContent = "20-24", cbdContent = "", type = PlantType.AUTOFLOWER),
                StrainInfo(name = "Forbidden Fruit", thcContent = "22-26", cbdContent = "", type = PlantType.FEMINIZED_HYBRID),
                StrainInfo(name = "Amnesia CBD", thcContent = "0.6-0.9", cbdContent = "18-27", type = PlantType.FEMINIZED_SATIVA),
                StrainInfo(name = "AK47 AUTO", thcContent = "16-18", cbdContent = "", type = PlantType.AUTOFLOWER),
                StrainInfo(name = "Purple Haze AUTO", thcContent = "22", cbdContent = "", type = PlantType.AUTOFLOWER),
                StrainInfo(name = "Northern Lights AUTO", thcContent = "21", cbdContent = "", type = PlantType.AUTOFLOWER),
                StrainInfo(name = "Sour Diesel AUTO", thcContent = "20-23", cbdContent = "", type = PlantType.AUTOFLOWER),
                StrainInfo(name = "Tangie Land", thcContent = "20", cbdContent = "", type = PlantType.FEMINIZED_HYBRID),
                StrainInfo(name = "Apples & Bananas", thcContent = "26", cbdContent = "", type = PlantType.FEMINIZED_HYBRID),
                StrainInfo(name = "Cream Mandarine AUTO", thcContent = "21-23", cbdContent = "", type = PlantType.AUTOFLOWER)
            ))
        )
    )

    suspend fun fetchFromWebSources(): List<SeedManufacturer> {
        // Not implemented; return the seeded dataset
        return manufacturers
    }
}
