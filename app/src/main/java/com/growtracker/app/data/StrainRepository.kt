package com.growtracker.app.data

// Reuse the serializable models from GrowModels.kt (SeedManufacturer, StrainInfo)
object StrainRepository {
    // Seeded dataset using existing types
    private val baseManufacturers: List<SeedManufacturer> = listOf(
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
                // Core Royal Queen Seeds Collection (values normalized without %)
                StrainInfo(name = "White Widow", thcContent = "19-25", cbdContent = "<1", type = PlantType.FEMINIZED_HYBRID),
                StrainInfo(name = "Amnesia Haze", thcContent = "20-25", cbdContent = "<1", type = PlantType.FEMINIZED_SATIVA),
                StrainInfo(name = "Blue Cheese", thcContent = "18-20", cbdContent = "<1", type = PlantType.FEMINIZED_INDICA),
                StrainInfo(name = "Critical", thcContent = "18-22", cbdContent = "<1", type = PlantType.FEMINIZED_HYBRID),
                StrainInfo(name = "Power Flower", thcContent = "19-24", cbdContent = "<1", type = PlantType.FEMINIZED_SATIVA),
                StrainInfo(name = "Sour Diesel", thcContent = "19-25", cbdContent = "<1", type = PlantType.FEMINIZED_SATIVA),
                StrainInfo(name = "OG Kush", thcContent = "19-26", cbdContent = "<1", type = PlantType.FEMINIZED_HYBRID),
                StrainInfo(name = "Cookies Gelato", thcContent = "25-28", cbdContent = "<1", type = PlantType.FEMINIZED_HYBRID),
                StrainInfo(name = "Purple Queen", thcContent = "15-22", cbdContent = "<1", type = PlantType.FEMINIZED_INDICA),
                StrainInfo(name = "Special Queen #1", thcContent = "18-20", cbdContent = "<1", type = PlantType.FEMINIZED_HYBRID),

                // Extended Royal Queen Seeds Collection
                StrainInfo(name = "Royal Domina", thcContent = "20-25", cbdContent = "<1", type = PlantType.FEMINIZED_INDICA),
                StrainInfo(name = "Royal Jack", thcContent = "18-23", cbdContent = "<1", type = PlantType.FEMINIZED_SATIVA),
                StrainInfo(name = "Royal Haze", thcContent = "17-21", cbdContent = "<1", type = PlantType.FEMINIZED_SATIVA),
                StrainInfo(name = "Royal Cookies", thcContent = "23-26", cbdContent = "<1", type = PlantType.FEMINIZED_HYBRID),
                StrainInfo(name = "Royal Gorilla", thcContent = "25-30", cbdContent = "<1", type = PlantType.FEMINIZED_HYBRID),
                StrainInfo(name = "Royal AK", thcContent = "17-20", cbdContent = "<1", type = PlantType.FEMINIZED_HYBRID),
                StrainInfo(name = "Royal Bluematic", thcContent = "14-16", cbdContent = "<1", type = PlantType.AUTOFLOWER),
                StrainInfo(name = "Royal Dwarf", thcContent = "13-16", cbdContent = "<1", type = PlantType.AUTOFLOWER),
                StrainInfo(name = "Easy Bud", thcContent = "12-14", cbdContent = "<1", type = PlantType.AUTOFLOWER),
                StrainInfo(name = "Quick One", thcContent = "13-17", cbdContent = "<1", type = PlantType.AUTOFLOWER),
                StrainInfo(name = "Royal Critical", thcContent = "18-22", cbdContent = "<1", type = PlantType.FEMINIZED_HYBRID),
                StrainInfo(name = "Royal Cheese", thcContent = "17-19", cbdContent = "<1", type = PlantType.FEMINIZED_INDICA),
                StrainInfo(name = "Fat Banana", thcContent = "25-28", cbdContent = "<1", type = PlantType.FEMINIZED_HYBRID),
                StrainInfo(name = "Wedding Gelato", thcContent = "25-27", cbdContent = "<1", type = PlantType.FEMINIZED_HYBRID),
                StrainInfo(name = "Mimosa x Orange Punch", thcContent = "24-27", cbdContent = "<1", type = PlantType.FEMINIZED_HYBRID),
                StrainInfo(name = "Sherbet Queen", thcContent = "24-26", cbdContent = "<1", type = PlantType.FEMINIZED_HYBRID),
                StrainInfo(name = "Green Gelato", thcContent = "24-26", cbdContent = "<1", type = PlantType.FEMINIZED_HYBRID),
                StrainInfo(name = "Watermelon", thcContent = "22-24", cbdContent = "<1", type = PlantType.FEMINIZED_HYBRID),
                StrainInfo(name = "Pineapple Kush", thcContent = "18-25", cbdContent = "<1", type = PlantType.FEMINIZED_HYBRID),
                StrainInfo(name = "Epsilon F1", thcContent = "15-20", cbdContent = "<1", type = PlantType.FEMINIZED_HYBRID),
                // Note: RQS sells Stress Killer Automatic CBD; kept as-is from dataset
                StrainInfo(name = "Stress Killer", thcContent = "18-21", cbdContent = "<1", type = PlantType.FEMINIZED_INDICA),
                StrainInfo(name = "Dance World", thcContent = "12-15", cbdContent = "12-15", type = PlantType.FEMINIZED_HYBRID),
                StrainInfo(name = "Joanne's CBD", thcContent = "1", cbdContent = "12-15", type = PlantType.FEMINIZED_INDICA),
                StrainInfo(name = "Euphoria", thcContent = "9", cbdContent = "6", type = PlantType.FEMINIZED_HYBRID),
                StrainInfo(name = "Painkiller XL", thcContent = "9", cbdContent = "9", type = PlantType.FEMINIZED_HYBRID),
                StrainInfo(name = "Honey Cream", thcContent = "16-20", cbdContent = "<1", type = PlantType.FEMINIZED_INDICA),
                StrainInfo(name = "Skunk XL", thcContent = "17-20", cbdContent = "<1", type = PlantType.FEMINIZED_HYBRID),
                StrainInfo(name = "Ice", thcContent = "18-20", cbdContent = "<1", type = PlantType.FEMINIZED_HYBRID),
                StrainInfo(name = "Northern Light", thcContent = "18-22", cbdContent = "<1", type = PlantType.FEMINIZED_INDICA),
                StrainInfo(name = "Fruit Spirit", thcContent = "18-22", cbdContent = "<1", type = PlantType.FEMINIZED_HYBRID),
                StrainInfo(name = "Special Kush #1", thcContent = "17-20", cbdContent = "<1", type = PlantType.FEMINIZED_INDICA),
                StrainInfo(name = "Shining Silver Haze", thcContent = "21-25", cbdContent = "<1", type = PlantType.FEMINIZED_SATIVA),
                StrainInfo(name = "Dark Angel", thcContent = "20-25", cbdContent = "<1", type = PlantType.FEMINIZED_INDICA),
                StrainInfo(name = "Sweet ZZ", thcContent = "17-20", cbdContent = "<1", type = PlantType.FEMINIZED_HYBRID),
                StrainInfo(name = "Diesel", thcContent = "20-25", cbdContent = "<1", type = PlantType.FEMINIZED_SATIVA),
                StrainInfo(name = "Bubble Kush", thcContent = "19-22", cbdContent = "<1", type = PlantType.FEMINIZED_INDICA),
                StrainInfo(name = "Monster", thcContent = "16-18", cbdContent = "<1", type = PlantType.FEMINIZED_SATIVA),
                StrainInfo(name = "Lemon Shining Silver Haze", thcContent = "21-25", cbdContent = "<1", type = PlantType.FEMINIZED_SATIVA),
                StrainInfo(name = "Royal Moby", thcContent = "21-24", cbdContent = "<1", type = PlantType.FEMINIZED_SATIVA),
                StrainInfo(name = "Chocolate Haze", thcContent = "20-25", cbdContent = "<1", type = PlantType.FEMINIZED_SATIVA),
                StrainInfo(name = "Apollo F1", thcContent = "16-21", cbdContent = "<1", type = PlantType.FEMINIZED_HYBRID),
                StrainInfo(name = "Titan F1", thcContent = "25-30", cbdContent = "<1", type = PlantType.FEMINIZED_HYBRID),
                StrainInfo(name = "Hyperion F1", thcContent = "23-26", cbdContent = "<1", type = PlantType.FEMINIZED_HYBRID),
                StrainInfo(name = "Zeus F1", thcContent = "25-28", cbdContent = "<1", type = PlantType.FEMINIZED_HYBRID),
                // Orion F1: RQS lists it as F1-hybrid auto; no official THC% provided on product page. Keep CBD as <1 and leave THC blank.
                StrainInfo(name = "Orion F1", thcContent = "", cbdContent = "<1", type = PlantType.FEMINIZED_HYBRID),
                StrainInfo(name = "Poseidon F1", thcContent = "20-23", cbdContent = "<1", type = PlantType.FEMINIZED_HYBRID),
                StrainInfo(name = "Hera F1", thcContent = "22-25", cbdContent = "<1", type = PlantType.FEMINIZED_HYBRID),
                StrainInfo(name = "Athena F1", thcContent = "24-27", cbdContent = "<1", type = PlantType.FEMINIZED_HYBRID),
                StrainInfo(name = "Hermes F1", thcContent = "21-24", cbdContent = "<1", type = PlantType.FEMINIZED_HYBRID),
                StrainInfo(name = "Artemis F1", thcContent = "23-26", cbdContent = "<1", type = PlantType.FEMINIZED_HYBRID),
                StrainInfo(name = "Dionysus F1", thcContent = "22-25", cbdContent = "<1", type = PlantType.FEMINIZED_HYBRID),

                // Kept existing item not present in the dataset
                StrainInfo(name = "Royal Highness (CBD)", thcContent = "", cbdContent = "", type = PlantType.FEMINIZED_HYBRID)
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
        ,
        // SpeedRunSeeds (feminized autoflowers - Indica category)
        SeedManufacturer(
            name = "SpeedRunSeeds",
            strains = listOf(
                // Base set (Indica category)
                StrainInfo(name = "Fatality", thcContent = "", cbdContent = "", type = PlantType.AUTOFLOWER),
                StrainInfo(name = "Golden Gun", thcContent = "", cbdContent = "", type = PlantType.AUTOFLOWER),
                StrainInfo(name = "Acid Snow", thcContent = "", cbdContent = "", type = PlantType.AUTOFLOWER),
                StrainInfo(name = "Blappleberry Haze F3", thcContent = "", cbdContent = "", type = PlantType.AUTOFLOWER),
                StrainInfo(name = "Boss Battle", thcContent = "", cbdContent = "", type = PlantType.AUTOFLOWER),
                StrainInfo(name = "Dreamcast", thcContent = "", cbdContent = "", type = PlantType.AUTOFLOWER),
                StrainInfo(name = "Frosted Cherry'Os F2", thcContent = "", cbdContent = "", type = PlantType.AUTOFLOWER),
                StrainInfo(name = "Frosted Cherry'Os F3", thcContent = "", cbdContent = "", type = PlantType.AUTOFLOWER),
                StrainInfo(name = "God Particle", thcContent = "", cbdContent = "", type = PlantType.AUTOFLOWER),
                StrainInfo(name = "Granite Runtz", thcContent = "", cbdContent = "", type = PlantType.AUTOFLOWER),
                StrainInfo(name = "Holy Hand Grenade", thcContent = "", cbdContent = "", type = PlantType.AUTOFLOWER),
                StrainInfo(name = "Magnum Dong", thcContent = "", cbdContent = "", type = PlantType.AUTOFLOWER),
                StrainInfo(name = "Pound Dawg F4", thcContent = "", cbdContent = "", type = PlantType.AUTOFLOWER),
                StrainInfo(name = "Sour Black Cherry Haze", thcContent = "", cbdContent = "", type = PlantType.AUTOFLOWER),
                StrainInfo(name = "Super Chonk", thcContent = "", cbdContent = "", type = PlantType.AUTOFLOWER),
                StrainInfo(name = "Supreme Runtz", thcContent = "", cbdContent = "", type = PlantType.AUTOFLOWER),
                StrainInfo(name = "Supreme Runtz F3", thcContent = "", cbdContent = "", type = PlantType.AUTOFLOWER),
                StrainInfo(name = "Terp Sneeze", thcContent = "", cbdContent = "", type = PlantType.AUTOFLOWER),
                StrainInfo(name = "Witness Protection", thcContent = "", cbdContent = "", type = PlantType.AUTOFLOWER),

                // Additional strains from Sativa, Sweet & Fruity, Funky & Skunky categories
                StrainInfo(name = "Double Jump", thcContent = "", cbdContent = "", type = PlantType.AUTOFLOWER),
                StrainInfo(name = "Combo Breaker", thcContent = "", cbdContent = "", type = PlantType.AUTOFLOWER),
                StrainInfo(name = "Final Boss F2", thcContent = "", cbdContent = "", type = PlantType.AUTOFLOWER),
                StrainInfo(name = "Granite Gas", thcContent = "", cbdContent = "", type = PlantType.AUTOFLOWER),
                StrainInfo(name = "Granite Haze F4", thcContent = "", cbdContent = "", type = PlantType.AUTOFLOWER),
                StrainInfo(name = "Granite Haze F5", thcContent = "", cbdContent = "", type = PlantType.AUTOFLOWER),
                StrainInfo(name = "Granite Haze F6", thcContent = "", cbdContent = "", type = PlantType.AUTOFLOWER),
                StrainInfo(name = "Haze Beast", thcContent = "", cbdContent = "", type = PlantType.AUTOFLOWER),
                StrainInfo(name = "Haze Invader", thcContent = "", cbdContent = "", type = PlantType.AUTOFLOWER),
                StrainInfo(name = "Holy Fire", thcContent = "", cbdContent = "", type = PlantType.AUTOFLOWER),
                StrainInfo(name = "Jehova's Witness", thcContent = "", cbdContent = "", type = PlantType.AUTOFLOWER),
                StrainInfo(name = "Rainbow Six", thcContent = "", cbdContent = "", type = PlantType.AUTOFLOWER),
                StrainInfo(name = "Rockslide", thcContent = "", cbdContent = "", type = PlantType.AUTOFLOWER),
                StrainInfo(name = "Sapphyre", thcContent = "", cbdContent = "", type = PlantType.AUTOFLOWER),
                StrainInfo(name = "Sapphyre F2", thcContent = "", cbdContent = "", type = PlantType.AUTOFLOWER),
                StrainInfo(name = "Slapple", thcContent = "", cbdContent = "", type = PlantType.AUTOFLOWER),
                StrainInfo(name = "Easter Egg", thcContent = "", cbdContent = "", type = PlantType.AUTOFLOWER),
                StrainInfo(name = "Iced Latte", thcContent = "", cbdContent = "", type = PlantType.AUTOFLOWER),
                StrainInfo(name = "Orange Portal", thcContent = "", cbdContent = "", type = PlantType.AUTOFLOWER),
                StrainInfo(name = "SLAM Berry", thcContent = "", cbdContent = "", type = PlantType.AUTOFLOWER),
                StrainInfo(name = "Froot Fuel", thcContent = "", cbdContent = "", type = PlantType.AUTOFLOWER),
                StrainInfo(name = "Froot Fuel Bx1", thcContent = "", cbdContent = "", type = PlantType.AUTOFLOWER),
                StrainInfo(name = "Pound Dawg F5", thcContent = "", cbdContent = "", type = PlantType.AUTOFLOWER)
            )
        )
    )

    // Merge base list with SeedFinder additions and optional A–Z JSON (res/raw)
    val manufacturers: List<SeedManufacturer> by lazy {
        val jsonManufacturers = loadSeedFinderFromRaw()
        mergeManufacturers(
            baseManufacturers,
            seedFinderAdditions() + jsonManufacturers
        )
    }

    suspend fun fetchFromWebSources(): List<SeedManufacturer> {
        // Not implemented; return the seeded dataset
        return manufacturers
    }
    
    private fun mergeManufacturers(
        base: List<SeedManufacturer>,
        extra: List<SeedManufacturer>
    ): List<SeedManufacturer> {
        val map = linkedMapOf<String, MutableList<StrainInfo>>()
        // Normalize and alias manufacturer names to avoid duplicates like "Barneys Farm" vs "Barney's Farm"
        val aliasMap = mapOf(
            // Common brand punctuation variants
            "barneys farm" to "barney's farm",
            "white label" to "white label seeds",
            "greenhouse seeds" to "green house seeds",
            // Brand spelling variants
            "th seeds" to "t.h.seeds",
            "world of seeds bank" to "world of seeds",
            "zambeza" to "zambeza seeds"
        )
        fun keyOf(name: String): String {
            val raw = name.trim().lowercase()
            val alias = aliasMap[raw] ?: raw
            return alias
        }
        // load base
        for (m in base) {
            map.getOrPut(keyOf(m.name)) { mutableListOf() }.addAll(m.strains)
        }
        // merge extra
        for (m in extra) {
            val k = keyOf(m.name)
            val existing = map.getOrPut(k) { mutableListOf() }
            val names = existing.map { it.name.trim().lowercase() }.toMutableSet()
            for (s in m.strains) {
                if (names.add(s.name.trim().lowercase())) existing.add(s)
            }
        }
        // rebuild deterministic list preserving base order and then adding new manufacturers in extra order
        val orderedKeys = LinkedHashSet<String>().apply {
            base.forEach { add(keyOf(it.name)) }
            extra.forEach { add(keyOf(it.name)) }
        }
        return orderedKeys.map { k ->
            val origName = base.find { keyOf(it.name) == k }?.name
                ?: extra.find { keyOf(it.name) == k }?.name
                ?: k
            SeedManufacturer(name = origName, strains = map[k] ?: emptyList())
        }
    }

    // Load full A–Z dataset from res/raw/seedfinder_a_to_z.json, if present
    private fun loadSeedFinderFromRaw(): List<SeedManufacturer> {
        return try {
            val res = com.growtracker.app.GrowTrackerApp.context.resources
            val id = res.getIdentifier("seedfinder_a_to_z", "raw", com.growtracker.app.GrowTrackerApp.context.packageName)
            if (id == 0) return emptyList()
            res.openRawResource(id).use { input ->
                val json = input.readBytes().toString(Charsets.UTF_8)
                parseBreederMapJson(json)
            }
        } catch (_: Throwable) {
            emptyList()
        }
    }

    // Parse a simple JSON map: { "Breeder": ["Strain1", ...], ... }
    private fun parseBreederMapJson(json: String): List<SeedManufacturer> {
        return try {
            val arr = mutableListOf<SeedManufacturer>()
            val obj = org.json.JSONObject(json)
            val names = obj.keys().asSequence().toList().sortedBy { it.lowercase() }
            for (breeder in names) {
                val strainsJson = obj.optJSONArray(breeder) ?: continue
                val strains = mutableListOf<StrainInfo>()
                for (i in 0 until strainsJson.length()) {
                    val n = strainsJson.optString(i).trim()
                    if (n.isNotEmpty()) strains += StrainInfo(name = n, thcContent = "", cbdContent = "", type = PlantType.FEMINIZED_HYBRID)
                }
                // Sort strains for nicer dropdown UX
                val sortedStrains = strains.sortedBy { it.name.lowercase() }
                arr += SeedManufacturer(name = breeder, strains = sortedStrains)
            }
            arr
        } catch (_: Throwable) {
            emptyList()
        }
    }
}
