package com.growtracker.app.data

fun getSeedManufacturers(): List<SeedManufacturer> {
    return listOf(
        SeedManufacturer(
            name = "Sensi Seeds",
            strains = listOf(
                // Classic Sensi Seeds Collection
                StrainInfo("Skunk #1", "15-19%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Northern Lights", "16-21%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("Big Bud", "15-20%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("Jack Herer", "18-23%", "<1%", PlantType.FEMINIZED_SATIVA),
                StrainInfo("Super Skunk", "19-25%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Black Domina", "20-25%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("Hindu Kush", "15-20%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("Silver Haze", "18-23%", "<1%", PlantType.FEMINIZED_SATIVA),
                StrainInfo("Afghani #1", "17-20%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("Early Skunk", "15-18%", "<1%", PlantType.FEMINIZED_HYBRID),
                
                // Extended Sensi Seeds Collection - Classic Strains
                StrainInfo("Silver Pearl", "16-20%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Mexican Sativa", "12-16%", "<1%", PlantType.FEMINIZED_SATIVA),
                StrainInfo("Durban", "14-18%", "<1%", PlantType.FEMINIZED_SATIVA),
                StrainInfo("Early Girl", "11-15%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Fruity Juice", "15-20%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Hashplant", "13-18%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("Maple Leaf Indica", "8-12%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("Ruderalis Indica", "10-14%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("Ruderalis Skunk", "8-12%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Shiva Skunk", "13-20%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("Shiva Shanti II", "15-20%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("Ed Rosenthal Super Bud", "12-16%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Four Way", "15-19%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("Marley's Collie", "12-16%", "<1%", PlantType.FEMINIZED_SATIVA),
                StrainInfo("Sensi #1", "12-16%", "<1%", PlantType.FEMINIZED_HYBRID),
                
                // Purple Collection
                StrainInfo("Purple Kush", "17-22%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("Purple Haze", "15-20%", "<1%", PlantType.FEMINIZED_SATIVA),
                StrainInfo("Purple Skunk", "16-21%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Purple Afghani", "14-19%", "<1%", PlantType.FEMINIZED_INDICA),
                
                // Haze Collection
                StrainInfo("Super Silver Haze", "19-23%", "<1%", PlantType.FEMINIZED_SATIVA),
                StrainInfo("Neville's Haze", "16-21%", "<1%", PlantType.FEMINIZED_SATIVA),
                StrainInfo("Amnesia Haze", "20-25%", "<1%", PlantType.FEMINIZED_SATIVA),
                StrainInfo("Lemon Haze", "17-22%", "<1%", PlantType.FEMINIZED_SATIVA),
                StrainInfo("Silver Haze #9", "18-23%", "<1%", PlantType.FEMINIZED_SATIVA),
                
                // Kush Collection
                StrainInfo("Master Kush", "18-22%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("Bubba Kush", "15-22%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("Critical Kush", "20-25%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("Skywalker Kush", "20-25%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("Afghan Kush", "15-20%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("Pakistani Kush", "16-20%", "<1%", PlantType.FEMINIZED_INDICA),
                
                // White Collection
                StrainInfo("White Widow", "18-25%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("White Rhino", "20-25%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("White Ice", "18-22%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("White Russian", "20-25%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("White Label", "16-20%", "<1%", PlantType.FEMINIZED_HYBRID),
                
                // Cheese Collection
                StrainInfo("Big Buddha Cheese", "14-20%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("Blue Cheese", "17-20%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("Exodus Cheese", "15-20%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Sweet Cheese", "16-20%", "<1%", PlantType.FEMINIZED_HYBRID),
                
                // Diesel Collection
                StrainInfo("Sour Diesel", "18-25%", "<1%", PlantType.FEMINIZED_SATIVA),
                StrainInfo("NYC Diesel", "18-22%", "<1%", PlantType.FEMINIZED_SATIVA),
                StrainInfo("Strawberry Diesel", "16-20%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Diesel Berry", "17-21%", "<1%", PlantType.FEMINIZED_HYBRID),
                
                // Chem Collection
                StrainInfo("Chemdawg", "19-26%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Chem Sister", "20-25%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Chem '91", "18-24%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Chem Dog #4", "19-25%", "<1%", PlantType.FEMINIZED_HYBRID),
                
                // Dream Collection
                StrainInfo("Blue Dream", "17-24%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Green Dream", "18-22%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Dream Queen", "16-22%", "<1%", PlantType.FEMINIZED_SATIVA),
                StrainInfo("Purple Dream", "17-21%", "<1%", PlantType.FEMINIZED_HYBRID),
                
                // Cookie Collection
                StrainInfo("Girl Scout Cookies", "19-28%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Thin Mint Cookies", "20-24%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Forum Cookies", "20-25%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Platinum Cookies", "18-24%", "<1%", PlantType.FEMINIZED_HYBRID),
                
                // Gelato Collection
                StrainInfo("Gelato #33", "20-25%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Gelato #41", "21-26%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Gelato #45", "20-24%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Wedding Gelato", "22-28%", "<1%", PlantType.FEMINIZED_HYBRID),
                
                // OG Collection Extended
                StrainInfo("OG Kush", "19-26%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Tahoe OG", "20-25%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("SFV OG", "19-24%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Fire OG", "20-25%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Ghost OG", "18-23%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Banana OG", "19-25%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("King Louis OG", "20-26%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("Larry OG", "20-25%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Alien OG", "20-28%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Presidential OG", "18-24%", "<1%", PlantType.FEMINIZED_INDICA),
                
                // Critical Collection
                StrainInfo("Critical Mass", "19-22%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("Critical +", "18-20%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Critical Jack", "18-21%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Critical Bilbo", "17-20%", "<1%", PlantType.FEMINIZED_HYBRID),
                
                // Trainwreck Collection
                StrainInfo("Trainwreck", "18-25%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Purple Trainwreck", "18-22%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Lemon Trainwreck", "17-22%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Trainwreck Kush", "19-24%", "<1%", PlantType.FEMINIZED_HYBRID),
                
                // Auto Collection
                StrainInfo("Northern Lights Auto", "14-18%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Skunk #1 Auto", "13-17%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Big Bud Auto", "14-19%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Jack Herer Auto", "16-20%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Super Skunk Auto", "17-22%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("White Widow Auto", "16-21%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("AK-47 Auto", "17-20%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Blueberry Auto", "16-20%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Amnesia Haze Auto", "18-22%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Sour Diesel Auto", "17-21%", "<1%", PlantType.AUTOFLOWER),
                
                // Medical Collection
                StrainInfo("Charlotte's Web", "0.3%", "20%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("ACDC", "1-6%", "14-20%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Harlequin", "5-10%", "5-10%", PlantType.FEMINIZED_SATIVA),
                StrainInfo("Cannatonic", "6-17%", "6-17%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Sour Tsunami", "10-11%", "11-13%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Remedy", "1%", "15-18%", PlantType.FEMINIZED_INDICA),
                StrainInfo("Ringo's Gift", "1%", "15-24%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Sweet and Sour Widow", "4-7%", "7-15%", PlantType.FEMINIZED_INDICA),
                StrainInfo("Pennywise", "8-15%", "8-15%", PlantType.FEMINIZED_INDICA),
                StrainInfo("Canna-Tsu", "1%", "16-20%", PlantType.FEMINIZED_HYBRID),
                
                // Legendary Collection
                StrainInfo("AK-47", "17-20%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Blueberry", "16-20%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("Bubblegum", "13-19%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("Chronic", "13-19%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("G13", "18-24%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("Grandaddy Purple", "17-23%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("Hawaiian", "14-19%", "<1%", PlantType.FEMINIZED_SATIVA),
                StrainInfo("Maui Wowie", "13-19%", "<1%", PlantType.FEMINIZED_SATIVA),
                StrainInfo("Panama Red", "12-16%", "<1%", PlantType.FEMINIZED_SATIVA),
                StrainInfo("Thai", "15-22%", "<1%", PlantType.FEMINIZED_SATIVA)
            )
        ),
        SeedManufacturer(
            name = "Royal Queen Seeds",
            strains = listOf(
                // Core Royal Queen Seeds Collection
                StrainInfo("White Widow", "19-25", "<1", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Amnesia Haze", "20-25", "<1", PlantType.FEMINIZED_SATIVA),
                StrainInfo("Blue Cheese", "18-20", "<1", PlantType.FEMINIZED_INDICA),
                StrainInfo("Critical", "18-22", "<1", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Power Flower", "19-24", "<1", PlantType.FEMINIZED_SATIVA),
                StrainInfo("Sour Diesel", "19-25", "<1", PlantType.FEMINIZED_SATIVA),
                StrainInfo("OG Kush", "19-26", "<1", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Cookies Gelato", "25-28", "<1", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Purple Queen", "15-22", "<1", PlantType.FEMINIZED_INDICA),
                StrainInfo("Special Queen #1", "18-20", "<1", PlantType.FEMINIZED_HYBRID),
                
                // Extended Royal Queen Seeds Collection
                StrainInfo("Royal Domina", "20-25", "<1", PlantType.FEMINIZED_INDICA),
                StrainInfo("Royal Jack", "18-23", "<1", PlantType.FEMINIZED_SATIVA),
                StrainInfo("Royal Haze", "17-21", "<1", PlantType.FEMINIZED_SATIVA),
                StrainInfo("Royal Cookies", "23-26", "<1", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Royal Gorilla", "25-30", "<1", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Royal AK", "17-20", "<1", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Royal Bluematic", "14-16", "<1", PlantType.AUTOFLOWER),
                StrainInfo("Royal Dwarf", "13-16", "<1", PlantType.AUTOFLOWER),
                StrainInfo("Easy Bud", "12-14", "<1", PlantType.AUTOFLOWER),
                StrainInfo("Quick One", "13-17", "<1", PlantType.AUTOFLOWER),
                StrainInfo("Royal Critical", "18-22", "<1", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Royal Cheese", "17-19", "<1", PlantType.FEMINIZED_INDICA),
                StrainInfo("Fat Banana", "25-28", "<1", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Wedding Gelato", "25-27", "<1", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Mimosa x Orange Punch", "24-27", "<1", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Sherbet Queen", "24-26", "<1", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Green Gelato", "24-26", "<1", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Watermelon", "22-24", "<1", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Pineapple Kush", "18-25", "<1", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Epsilon F1", "15-20", "<1", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Stress Killer", "18-21", "<1", PlantType.FEMINIZED_INDICA),
                StrainInfo("Dance World", "12-15", "12-15", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Joanne's CBD", "1", "12-15", PlantType.FEMINIZED_INDICA),
                StrainInfo("Euphoria", "9", "6", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Painkiller XL", "9", "9", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Honey Cream", "16-20", "<1", PlantType.FEMINIZED_INDICA),
                StrainInfo("Skunk XL", "17-20", "<1", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Ice", "18-20", "<1", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Northern Light", "18-22", "<1", PlantType.FEMINIZED_INDICA),
                StrainInfo("Fruit Spirit", "18-22", "<1", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Special Kush #1", "17-20", "<1", PlantType.FEMINIZED_INDICA),
                StrainInfo("Shining Silver Haze", "21-25", "<1", PlantType.FEMINIZED_SATIVA),
                StrainInfo("Dark Angel", "20-25", "<1", PlantType.FEMINIZED_INDICA),
                StrainInfo("Sweet ZZ", "17-20", "<1", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Diesel", "20-25", "<1", PlantType.FEMINIZED_SATIVA),
                StrainInfo("Bubble Kush", "19-22", "<1", PlantType.FEMINIZED_INDICA),
                StrainInfo("Monster", "16-18", "<1", PlantType.FEMINIZED_SATIVA),
                StrainInfo("Lemon Shining Silver Haze", "21-25", "<1", PlantType.FEMINIZED_SATIVA),
                StrainInfo("Royal Moby", "21-24", "<1", PlantType.FEMINIZED_SATIVA),
                StrainInfo("Chocolate Haze", "20-25", "<1", PlantType.FEMINIZED_SATIVA),
                StrainInfo("Apollo F1", "16-21", "<1", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Titan F1", "25-30", "<1", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Hyperion F1", "23-26", "<1", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Zeus F1", "25-28", "<1", PlantType.FEMINIZED_HYBRID),
                // Orion F1: product page does not provide numeric THC; keep CBD as <1 and leave THC empty
                StrainInfo("Orion F1", "", "<1", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Poseidon F1", "20-23", "<1", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Hera F1", "22-25", "<1", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Athena F1", "24-27", "<1", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Hermes F1", "21-24", "<1", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Artemis F1", "23-26", "<1", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Dionysus F1", "22-25", "<1", PlantType.FEMINIZED_HYBRID)
            )
        ),
        SeedManufacturer(
            name = "Fast Buds",
            strains = listOf(
                // Core Fast Buds Collection
                StrainInfo("Gorilla Glue", "24-27%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Girl Scout Cookies", "23-26%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Zkittlez", "22-25%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Gelato", "24-26%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Wedding Cheesecake", "25-27%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("LSD-25", "21-25%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Blackberry", "20-26%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Green Crack", "22-24%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Mexican Airlines", "20-23%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Californian Snow", "22-26%", "<1%", PlantType.AUTOFLOWER),
                
                // Extended Fast Buds Autoflower Collection
                StrainInfo("Fast Buds #2", "20-25%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Pineapple Express", "23-26%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Blue Dream'matic", "21-24%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Tangie'matic", "22-25%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Six Shooter", "18-21%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Crystal Meth", "22-25%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Stardawg", "22-25%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Fastberry", "18-23%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Original Amnesia", "20-25%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("West Coast OG", "25-27%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Smoothie", "21-26%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Rhino Ryder", "20-24%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Northern Express", "18-22%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Diesel", "20-25%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Jack Herer", "18-23%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("White Widow", "19-24%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("AK-47", "20-22%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Blueberry", "18-21%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Critical", "18-22%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Bubble Kush", "19-24%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Lemon Pie", "24-26%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Cream Cookies", "20-24%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Purple Lemonade", "21-25%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Orange Sherbet", "22-24%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Strawberry Pie", "23-26%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Forbidden Runtz", "23-26%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Mimosa Cake", "24-27%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Banana Purple Punch", "20-25%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Tropicana Cookies", "22-25%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Bruce Banner", "25-29%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Gorilla Cookies", "24-28%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Wedding Glue", "24-27%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Sunset Sherbet", "19-24%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Grapefruit", "17-24%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Sour Diesel", "20-25%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("OG Kush", "19-24%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Cherry Cola", "21-25%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Apple Strudel", "22-24%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Cookies & Cream", "22-25%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Chemdawg", "20-25%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Runtz Muffin", "27-29%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Afghan Kush", "20-23%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Lemon AK", "22-24%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Do-Si-Dos", "26-30%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Purple Punch", "20-25%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Glue Gelato", "24-26%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Triple Grape", "25-28%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Ztrawberriez", "24-27%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Gorilla Punch", "23-25%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Orange Bubblegum", "22-25%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("MAC 1", "23-26%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Strawberry Gorilla", "21-26%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Zkittlez Glue", "25-27%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Papaya Cookies", "24-28%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Lemon Cherry Cookies", "25-28%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Afghan Kush Ryder", "20-24%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Big Kush", "24-27%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Crystal Candy", "22-25%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Dos Cookies", "28-30%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Elf Bar", "25-28%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Frosted Cookies", "27-30%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Giga Nugs", "24-28%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Holy Moly", "26-28%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Italian Ice", "25-27%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Jealousy", "28-32%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Killer Gorilla", "25-29%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("London Pound Cake", "24-26%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Milky Way", "22-25%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Night Owl", "25-28%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Orange Punch", "18-22%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Poison", "24-26%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Quiet Monk", "20-24%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Red Hot Cookies", "25-28%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Smokin' Gun", "26-29%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Trainwreck", "20-25%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Ultra Lemon", "23-26%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Violet Moonbeam", "21-25%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Watermelon Zkittlez", "22-25%", "<1%", PlantType.AUTOFLOWER)
            )
        ),
        SeedManufacturer(
            name = "Barney's Farm",
            strains = listOf(
                // Core Barney's Farm Collection
                StrainInfo("Tangerine Dream", "25%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Pineapple Express", "24%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Blue Cheese", "20%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("Sweet Tooth", "16%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("Liberty Haze", "25%", "<1%", PlantType.FEMINIZED_SATIVA),
                StrainInfo("Gorilla Zkittlez", "24%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Mimosa x Orange Punch", "25%", "<1%", PlantType.FEMINIZED_HYBRID),
                
                // Extended Barney's Farm Collection
                StrainInfo("Wedding Cake", "25-27%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Runtz Muffin", "29-30%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Dos Si Dos 33", "28-30%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Glue Gelato", "26-28%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Zkittlez OG", "26-28%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Pink Kush", "24-26%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("Strawberry Lemonade", "22-26%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Purple Punch", "25-27%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("Gelato 41", "26-28%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Gorilla Glue #4", "25-28%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Cookies Kush", "24-26%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Orange Sherbet", "22-24%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Tropicana Cookies", "22-25%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Forbidden Runtz", "23-26%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("London Pound Cake 75", "21-24%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Purple Sunset", "20-24%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Biscotti", "25-28%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Sunset Sherbet", "19-24%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("MAC 1", "23-26%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Wedding Gelato", "25-27%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Banana Kush Cake", "22-25%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Blueberry OG", "22-24%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Cherry Pie", "20-23%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Girl Scout Cookies", "19-28%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Gorilla Breath", "24-28%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Lemon Tree", "22-25%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Watermelon Zkittlez", "22-25%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Zkittlez Glue", "25-27%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Blue Gelato 41", "26-28%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Cookies & Cream", "22-25%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Do-Si-Dos", "26-30%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Gelato #33", "20-25%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Ice Cream Cake", "25-27%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Laughing Buddha", "18-23%", "<1%", PlantType.FEMINIZED_SATIVA),
                StrainInfo("Pineapple Chunk", "25%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Red Dragon", "15-20%", "<1%", PlantType.FEMINIZED_SATIVA),
                StrainInfo("Utopia Haze", "15-20%", "<1%", PlantType.FEMINIZED_SATIVA),
                StrainInfo("Vanilla Kush", "20%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("Violator Kush", "22%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("Blue Mammoth", "20-25%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("Critical Kush", "25%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("Dr. Grinspoon", "15-20%", "<1%", PlantType.FEMINIZED_SATIVA),
                StrainInfo("Green Gelato", "24-26%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("LSD", "24%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Nightshade", "20-24%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("Phantom OG", "18-24%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Shiskaberry", "20%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("8 Ball Kush", "19-23%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("Acapulco Gold", "15-23%", "<1%", PlantType.FEMINIZED_SATIVA),
                StrainInfo("Amnesia Lemon", "22%", "<1%", PlantType.FEMINIZED_SATIVA),
                StrainInfo("Bad Azz Kush", "20%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("Blue Mammoth", "20-25%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("Cheese", "20%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("Chronic Thunder", "22%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Cotton Candy Kush", "22%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("G13 Haze", "22-24%", "<1%", PlantType.FEMINIZED_SATIVA),
                StrainInfo("Triple Cheese", "17%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("Morning Glory", "20-23%", "<1%", PlantType.FEMINIZED_SATIVA),
                StrainInfo("Honey B", "23%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Kosher Kush", "22-26%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("Malana Bomb", "20%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("OG Kush", "19-26%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Peppermint Kush", "20%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Skywalker OG", "20-30%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Tangerine Kush", "22%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("White Russian", "20-25%", "<1%", PlantType.FEMINIZED_INDICA)
            )
        ),
        SeedManufacturer(
            name = "Dutch Passion",
            strains = listOf(
                // Core Dutch Passion Collection
                StrainInfo("Orange Bud", "16-20%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Blueberry", "17-19%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("White Widow", "18-22%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Power Plant", "15-20%", "<1%", PlantType.FEMINIZED_SATIVA),
                StrainInfo("Durban Poison", "15-20%", "<1%", PlantType.FEMINIZED_SATIVA),
                StrainInfo("Mazar", "19-20%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("Ultimate", "16-18%", "<1%", PlantType.FEMINIZED_HYBRID),
                
                // Extended Dutch Passion Collection
                StrainInfo("Auto Blackberry Kush", "15-20%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Auto Brooklyn Sunrise", "19-22%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Auto Charlotte's Angel", "1%", "15-17%", PlantType.AUTOFLOWER),
                StrainInfo("Auto Cinderella Jack", "15-20%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Auto Colorado Cookies", "20-22%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Auto Daiquiri Lime", "20-25%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Auto Duck", "12-17%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Auto Euforia", "15-18%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Auto Glueberry OG", "20-25%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Auto Kerosene Krash", "20-25%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Auto Lemon Kix", "17-20%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Auto Mazar", "17-20%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Auto Orange Bud", "15-19%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Auto Purple", "15-20%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Auto Ultimate", "15-18%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Auto White Widow", "18-22%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Brooklyn Sunrise", "19-22%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("CBD Auto Blackberry Kush", "6-8%", "6-8%", PlantType.AUTOFLOWER),
                StrainInfo("CBD Auto Compassion Lime", "8-12%", "8-12%", PlantType.AUTOFLOWER),
                StrainInfo("CBD Auto White Widow", "9%", "9%", PlantType.AUTOFLOWER),
                StrainInfo("CBD Blackberry Kush", "6-8%", "6-8%", PlantType.FEMINIZED_INDICA),
                StrainInfo("CBD Charlotte's Angel", "1%", "15-17%", PlantType.FEMINIZED_SATIVA),
                StrainInfo("CBD Compassion", "8-12%", "8-12%", PlantType.FEMINIZED_INDICA),
                StrainInfo("CBD Skunk Haze", "7-12%", "7-12%", PlantType.FEMINIZED_SATIVA),
                StrainInfo("Cinderella Jack", "15-20%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Colorado Cookies", "20-22%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Daiquiri Lime", "20-25%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Desfrán", "16-20%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Euforia", "15-18%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Frisian Duck", "12-17%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Glueberry OG", "20-25%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Kerosene Krash", "20-25%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Master Kush", "18-22%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("Night Queen", "18-22%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("Orange Hill Special", "15-20%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Outlaw", "20-25%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Pamir Gold", "15-20%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("Purple #1", "15-20%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("Shaman", "15-20%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Skywalker Haze", "15-22%", "<1%", PlantType.FEMINIZED_SATIVA),
                StrainInfo("Strawberry Cough", "15-18%", "<1%", PlantType.FEMINIZED_SATIVA),
                StrainInfo("The Cali Collection Triangle Kush", "20-25%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("The Cali Collection Gelato 41", "20-25%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("The Cali Collection Larry Bird Kush", "20-25%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("Think Big", "18-22%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Twilight", "15-20%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("Passion #1", "15-20%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Star Ryder", "12-15%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Think Different", "15-20%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Auto Think Different", "15-20%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Auto Night Queen", "18-22%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Auto Skywalker Haze", "15-22%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Auto Strawberry Cough", "15-18%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Auto Twilight", "15-20%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Auto Desfrán", "16-20%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Auto Outlaw", "20-25%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Auto Pamir Gold", "15-20%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Auto Shaman", "15-20%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Auto Brooklyn Sunrise", "19-22%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Auto Cinderella Jack", "15-20%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Auto Colorado Cookies", "20-22%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Auto Daiquiri Lime", "20-25%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Auto Duck", "12-17%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Auto Euforia", "15-18%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Auto Glueberry OG", "20-25%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Auto Kerosene Krash", "20-25%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Auto Lemon Kix", "17-20%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Auto Orange Hill Special", "15-20%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("HiFi 4G", "20-25%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Auto HiFi 4G", "20-25%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Sugar Bomb Punch", "20-25%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Auto Sugar Bomb Punch", "20-25%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Bubba Island Kush", "18-23%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("Auto Bubba Island Kush", "18-23%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Critical Orange Punch", "18-22%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Auto Critical Orange Punch", "18-22%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Banana Blaze", "20-25%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Auto Banana Blaze", "20-25%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Gelato 41", "20-25%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Auto Gelato 41", "20-25%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Orange Sherbet", "18-22%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Auto Orange Sherbet", "18-22%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Zkittlez", "18-23%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Auto Zkittlez", "18-23%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Purple Lemonade", "15-22%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Auto Purple Lemonade", "15-22%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Runtz", "19-29%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Auto Runtz", "19-29%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Wedding Cheesecake", "22-25%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Auto Wedding Cheesecake", "22-25%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Cookies Gelato", "20-28%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Auto Cookies Gelato", "20-28%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Apple Strudel", "20-24%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Auto Apple Strudel", "20-24%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Blue Velvet", "20-24%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("Auto Blue Velvet", "20-24%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Do-Si-Dos", "22-30%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Auto Do-Si-Dos", "22-30%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Forbidden Fruit", "23-26%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("Auto Forbidden Fruit", "23-26%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Cherry Pie", "16-24%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Auto Cherry Pie", "16-24%", "<1%", PlantType.AUTOFLOWER)
            )
        ),
        SeedManufacturer(
            name = "Greenhouse Seeds",
            strains = listOf(
                StrainInfo("Green House Cheese", "18-22%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("Super Lemon Haze", "22-25%", "<1%", PlantType.FEMINIZED_SATIVA),
                StrainInfo("Trainwreck", "18-25%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("The Church", "18-22%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Kalashnikova", "18-20%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Great White Shark", "15-20%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("White Rhino", "20-25%", "<1%", PlantType.FEMINIZED_INDICA)
            )
        ),
        SeedManufacturer(
            name = "DNA Genetics",
            strains = listOf(
                StrainInfo("Tangie", "20-22%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("LA Confidential", "16-20%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("Chocolope", "18-21%", "<1%", PlantType.FEMINIZED_SATIVA),
                StrainInfo("Lemon Skunk", "15-20%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Kosher Kush", "20-25%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("Holy Grail Kush", "18-24%", "<1%", PlantType.FEMINIZED_HYBRID)
            )
        ),
        SeedManufacturer(
            name = "Dinafem Seeds",
            strains = listOf(
                StrainInfo("White Widow", "15-20%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Moby Dick", "21%", "<1%", PlantType.FEMINIZED_SATIVA),
                StrainInfo("Critical +", "18-20%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Blue Hash", "15-20%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("OG Kush", "20-24%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Cheese", "16-20%", "<1%", PlantType.FEMINIZED_INDICA)
            )
        ),
        SeedManufacturer(
            name = "Humboldt Seed Organization",
            strains = listOf(
                StrainInfo("Blue Dream", "17-24%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Trainwreck", "18-25%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Girl Scout Cookies", "19-28%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Green Crack", "15-25%", "<1%", PlantType.FEMINIZED_SATIVA),
                StrainInfo("Emerald Headband", "22-26%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Purple Trainwreck", "18-22%", "<1%", PlantType.FEMINIZED_HYBRID)
            )
        ),
        SeedManufacturer(
            name = "Paradise Seeds",
            strains = listOf(
                StrainInfo("Wappa", "15-25%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("Sensi Star", "16-20%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("White Berry", "15-20%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Nebula", "16-21%", "<1%", PlantType.FEMINIZED_SATIVA),
                StrainInfo("Durga Mata", "15-20%", "<1%", PlantType.FEMINIZED_INDICA)
            )
        ),
        SeedManufacturer(
            name = "Sweet Seeds",
            strains = listOf(
                StrainInfo("Big Devil XL", "18-22%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Green Poison", "15-20%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Jack 47", "16-20%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Cream Caramel", "15-20%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("Sweet Cheese", "16-20%", "<1%", PlantType.FEMINIZED_HYBRID)
            )
        ),
        SeedManufacturer(
            name = "Cali Connection",
            strains = listOf(
                StrainInfo("Tahoe OG", "20-25%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("SFV OG Kush", "18-24%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Alien OG", "20-28%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Deadhead OG", "20-25%", "<1%", PlantType.FEMINIZED_HYBRID)
            )
        ),
        SeedManufacturer(
            name = "Mr. Nice Seeds",
            strains = listOf(
                StrainInfo("Critical Mass", "19-22%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("Super Silver Haze", "18-23%", "<1%", PlantType.FEMINIZED_SATIVA),
                StrainInfo("Black Widow", "18-25%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Medicine Man", "15-20%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("Mango Haze", "16-23%", "<1%", PlantType.FEMINIZED_SATIVA)
            )
        ),
        SeedManufacturer(
            name = "Delicious Seeds",
            strains = listOf(
                StrainInfo("Critical Jack", "18-21%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Cotton Candy", "15-22%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Sugar Black Rose", "18-25%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("Caramelo", "15-21%", "<1%", PlantType.FEMINIZED_HYBRID)
            )
        ),
        SeedManufacturer(
            name = "Bomb Seeds",
            strains = listOf(
                StrainInfo("THC Bomb", "20-25%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Big Bomb", "16-20%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("Cherry Bomb", "18-20%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Atomic", "15-20%", "<1%", PlantType.FEMINIZED_HYBRID)
            )
        ),
        SeedManufacturer(
            name = "Auto Seeds",
            strains = listOf(
                StrainInfo("Berry Ryder", "15-18%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Dwarf Low Flyer", "10-15%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Diesel Ryder", "16-20%", "<1%", PlantType.AUTOFLOWER),
                StrainInfo("Sweet Dwarf", "12-16%", "<1%", PlantType.AUTOFLOWER)
            )
        ),
        SeedManufacturer(
            name = "Female Seeds",
            strains = listOf(
                StrainInfo("White Widow x Big Bud", "15-20%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Grapefruit", "15-20%", "<1%", PlantType.FEMINIZED_SATIVA),
                StrainInfo("C99", "16-20%", "<1%", PlantType.FEMINIZED_SATIVA),
                StrainInfo("Skunk Special", "15-18%", "<1%", PlantType.FEMINIZED_HYBRID)
            )
        ),
        SeedManufacturer(
            name = "Kannabia Seeds",
            strains = listOf(
                StrainInfo("Special K", "18-20%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Big Band", "15-18%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("Mataro Blue", "18-20%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("La Blanca", "15-20%", "<1%", PlantType.FEMINIZED_HYBRID)
            )
        ),
        SeedManufacturer(
            name = "Big Buddha Seeds",
            strains = listOf(
                StrainInfo("Cheese", "16-20%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("Blue Cheese", "17-20%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("Chiesel", "16-18%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Buddha's Sister", "15-18%", "<1%", PlantType.FEMINIZED_INDICA)
            )
        ),
        SeedManufacturer(
            name = "Ace Seeds",
            strains = listOf(
                StrainInfo("Golden Tiger", "15-25%", "<1%", PlantType.FEMINIZED_SATIVA),
                StrainInfo("Panama", "15-20%", "<1%", PlantType.FEMINIZED_SATIVA),
                StrainInfo("Malawi", "16-21%", "<1%", PlantType.FEMINIZED_SATIVA),
                StrainInfo("Orient Express", "15-22%", "<1%", PlantType.FEMINIZED_HYBRID)
            )
        ),
        SeedManufacturer(
            name = "Amsterdam Genetics",
            strains = listOf(
                StrainInfo("White Choco", "18-22%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("AK-020", "15-20%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Blue Amnesia", "16-20%", "<1%", PlantType.FEMINIZED_SATIVA),
                StrainInfo("Strawberry Cough", "15-18%", "<1%", PlantType.FEMINIZED_SATIVA)
            )
        ),
        SeedManufacturer(
            name = "Anesia Seeds",
            strains = listOf(
                StrainInfo("Future #1", "25-29%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Imperium X", "25-30%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Blackberry Moonrocks", "24-28%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("Slurricane", "26-28%", "<1%", PlantType.FEMINIZED_INDICA)
            )
        ),
        SeedManufacturer(
            name = "BudVoyage",
            strains = listOf(
                // BudVoyage Complete Strain Collection - including official hansbrainfood.de strains

                StrainInfo("Green Gelato Auto", "24%", "<1%", PlantType.AUTOFLOWER), // Sativa-dominant - Official: 24% THC
                StrainInfo("Northern Lights Auto", "14%", "<1%", PlantType.AUTOFLOWER), // Indica-dominant - Official: 14% THC
                StrainInfo("White Widow Auto", "14%", "<1%", PlantType.AUTOFLOWER), // Hybrid - Official: 14% THC
                StrainInfo("Strawberry Smile Auto", "16-20%", "<1%", PlantType.AUTOFLOWER), // Sativa-dominant
                StrainInfo("Purple Punch Auto", "18-22%", "<1%", PlantType.AUTOFLOWER), // Indica-dominant
                StrainInfo("Purple Haze Fem", "14-18%", "<1%", PlantType.FEMINIZED_SATIVA),
                StrainInfo("Hellfire OG Fem", "30%", "<1%", PlantType.FEMINIZED_HYBRID), // Official: 30% THC
                StrainInfo("Blackberry Moonrocks Fem", "20-24%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("Back 2 Future Fem", "19-23%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Pineapple Fruz Fem", "18-22%", "<1%", PlantType.FEMINIZED_SATIVA),
                StrainInfo("Fat Bastard Fem", "38%", "<1%", PlantType.FEMINIZED_INDICA), // Official: 38% THC - STRONGEST STRAIN!
                StrainInfo("Sleepy Joe Fem", "21-25%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("Amnesia F1 Fem", "20-24%", "<1%", PlantType.FEMINIZED_SATIVA),
                StrainInfo("Kush Original F1 Fem", "18-22%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("Do si Dos F1 Fem", "26-30%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Amnesia Haze", "20-22%", "<1%", PlantType.FEMINIZED_SATIVA),
                StrainInfo("AK-47", "19-20%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Blueberry", "16-18%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("Blue Dream", "17-24%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Bubble Gum", "17-19%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("Cheese", "18-20%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("Chronic", "16-20%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Critical Mass", "19-22%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("Girl Scout Cookies", "25-28%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Gorilla Glue #4", "25-28%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Green Crack", "15-25%", "<1%", PlantType.FEMINIZED_SATIVA),
                StrainInfo("Hindu Kush", "15-20%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("Jack Herer", "18-23%", "<1%", PlantType.FEMINIZED_SATIVA),
                StrainInfo("Lemon Haze", "15-20%", "<1%", PlantType.FEMINIZED_SATIVA),
                StrainInfo("Mango Kush", "11-20%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("Northern Lights", "16-21%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("OG Kush", "19-26%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Pineapple Express", "19-25%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Purple Haze", "14-18%", "<1%", PlantType.FEMINIZED_SATIVA),
                StrainInfo("Skunk #1", "15-19%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Sour Diesel", "19-25%", "<1%", PlantType.FEMINIZED_SATIVA),
                StrainInfo("Super Silver Haze", "18-23%", "<1%", PlantType.FEMINIZED_SATIVA),
                StrainInfo("Trainwreck", "18-25%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("White Widow", "18-25%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Big Bud", "15-20%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("Black Domina", "20-25%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("Purple Kush", "17-22%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("Strawberry Cough", "15-18%", "<1%", PlantType.FEMINIZED_SATIVA),
                StrainInfo("White Russian", "20-25%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("Afghan Kush", "15-20%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("Blue Cheese", "17-20%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("Durban Poison", "15-25%", "<1%", PlantType.FEMINIZED_SATIVA),
                StrainInfo("G13", "18-24%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("Granddaddy Purple", "17-23%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("Hash Plant", "13-18%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("Maui Wowie", "13-19%", "<1%", PlantType.FEMINIZED_SATIVA),
                StrainInfo("NYC Diesel", "18-22%", "<1%", PlantType.FEMINIZED_SATIVA),
                StrainInfo("Chocolate Thai", "15-22%", "<1%", PlantType.FEMINIZED_SATIVA),
                StrainInfo("Acapulco Gold", "15-23%", "<1%", PlantType.FEMINIZED_SATIVA),
                StrainInfo("Colombian Gold", "12-18%", "<1%", PlantType.FEMINIZED_SATIVA),
                StrainInfo("Panama Red", "12-16%", "<1%", PlantType.FEMINIZED_SATIVA),
                StrainInfo("Thai", "15-22%", "<1%", PlantType.FEMINIZED_SATIVA),
                StrainInfo("Lamb's Bread", "16-21%", "<1%", PlantType.FEMINIZED_SATIVA),
                StrainInfo("Chemdawg", "19-26%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Wedding Cake", "25-27%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Gelato", "20-25%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Zkittlez", "18-23%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Sunset Sherbet", "19-24%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Do-Si-Dos", "26-30%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Runtz", "19-29%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Mimosa", "19-27%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Purple Punch", "20-25%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("Ice Cream Cake", "25-27%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Biscotti", "25-28%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Animal Cookies", "18-27%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Sherbet", "18-24%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("MAC", "23-26%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Forbidden Fruit", "23-26%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("Cherry Pie", "16-24%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Jungle Boys Wedding Cake", "25-28%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Jungle Boys Mimosa", "20-24%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Jungle Boys White Fire OG", "22-28%", "<1%", PlantType.FEMINIZED_HYBRID),
                StrainInfo("Jungle Boys Motor Breath", "24-28%", "<1%", PlantType.FEMINIZED_INDICA),
                StrainInfo("London Pound Cake", "21-24%", "<1%", PlantType.FEMINIZED_HYBRID)
            )
        )
        ,
        // Speedrun Seeds - imported from categories: Indica, Sativa, Sweet & Fruity, Funky & Skunky
        SeedManufacturer(
            name = "SpeedRunSeeds",
            strains = listOf(
                // Base set (Indica category)
                StrainInfo("Fatality", "", "", PlantType.AUTOFLOWER),
                StrainInfo("Golden Gun", "", "", PlantType.AUTOFLOWER),
                StrainInfo("Acid Snow", "", "", PlantType.AUTOFLOWER),
                StrainInfo("Blappleberry Haze F3", "", "", PlantType.AUTOFLOWER),
                StrainInfo("Boss Battle", "", "", PlantType.AUTOFLOWER),
                StrainInfo("Dreamcast", "", "", PlantType.AUTOFLOWER),
                StrainInfo("Frosted Cherry'Os F2", "", "", PlantType.AUTOFLOWER),
                StrainInfo("Frosted Cherry'Os F3", "", "", PlantType.AUTOFLOWER),
                StrainInfo("God Particle", "", "", PlantType.AUTOFLOWER),
                StrainInfo("Granite Runtz", "", "", PlantType.AUTOFLOWER),
                StrainInfo("Holy Hand Grenade", "", "", PlantType.AUTOFLOWER),
                StrainInfo("Magnum Dong", "", "", PlantType.AUTOFLOWER),
                StrainInfo("Pound Dawg F4", "", "", PlantType.AUTOFLOWER),
                StrainInfo("Sour Black Cherry Haze", "", "", PlantType.AUTOFLOWER),
                StrainInfo("Super Chonk", "", "", PlantType.AUTOFLOWER),
                StrainInfo("Supreme Runtz", "", "", PlantType.AUTOFLOWER),
                StrainInfo("Supreme Runtz F3", "", "", PlantType.AUTOFLOWER),
                StrainInfo("Terp Sneeze", "", "", PlantType.AUTOFLOWER),
                StrainInfo("Witness Protection", "", "", PlantType.AUTOFLOWER),

                // Additional strains from Sativa, Sweet & Fruity, Funky & Skunky categories
                StrainInfo("Double Jump", "", "", PlantType.AUTOFLOWER),
                StrainInfo("Combo Breaker", "", "", PlantType.AUTOFLOWER),
                StrainInfo("Final Boss F2", "", "", PlantType.AUTOFLOWER),
                StrainInfo("Granite Gas", "", "", PlantType.AUTOFLOWER),
                StrainInfo("Granite Haze F4", "", "", PlantType.AUTOFLOWER),
                StrainInfo("Granite Haze F5", "", "", PlantType.AUTOFLOWER),
                StrainInfo("Granite Haze F6", "", "", PlantType.AUTOFLOWER),
                StrainInfo("Haze Beast", "", "", PlantType.AUTOFLOWER),
                StrainInfo("Haze Invader", "", "", PlantType.AUTOFLOWER),
                StrainInfo("Holy Fire", "", "", PlantType.AUTOFLOWER),
                StrainInfo("Jehova's Witness", "", "", PlantType.AUTOFLOWER),
                StrainInfo("Rainbow Six", "", "", PlantType.AUTOFLOWER),
                StrainInfo("Rockslide", "", "", PlantType.AUTOFLOWER),
                StrainInfo("Sapphyre", "", "", PlantType.AUTOFLOWER),
                StrainInfo("Sapphyre F2", "", "", PlantType.AUTOFLOWER),
                StrainInfo("Slapple", "", "", PlantType.AUTOFLOWER),
                StrainInfo("Easter Egg", "", "", PlantType.AUTOFLOWER),
                StrainInfo("Iced Latte", "", "", PlantType.AUTOFLOWER),
                StrainInfo("Orange Portal", "", "", PlantType.AUTOFLOWER),
                StrainInfo("SLAM Berry", "", "", PlantType.AUTOFLOWER),
                StrainInfo("Froot Fuel", "", "", PlantType.AUTOFLOWER),
                StrainInfo("Froot Fuel Bx1", "", "", PlantType.AUTOFLOWER),
                StrainInfo("Pound Dawg F5", "", "", PlantType.AUTOFLOWER)
            )
        )
    )
}

fun getFertilizerManufacturers(): List<FertilizerManufacturer> {
    return listOf(
        FertilizerManufacturer(
            name = "Advanced Nutrients",
            products = listOf(
                FertilizerProduct("Sensi grow A", "Grunddünger", "4-0-0", "Wachstumsphase Teil A"),
                FertilizerProduct("Sensi grow B", "Grunddünger", "1-3-4", "Wachstumsphase Teil B"),
                FertilizerProduct("Sensi Bloom A", "Blütedünger", "3-0-0", "Blütephase Teil A"),
                FertilizerProduct("Sensi Bloom B", "Blütedünger", "1-3-4", "Blütephase Teil B"),
                FertilizerProduct("Big Bud", "Zusätze", "0-1-3", "Blütenbooster"),
                FertilizerProduct("Overdrive", "Zusätze", "1-5-4", "Finaler Blütenbooster"),
                FertilizerProduct("Voodoo Juice", "Zusätze", "", "Wurzelstimulator"),
                FertilizerProduct("B-52", "Zusätze", "", "B-Vitamin Komplex"),
                FertilizerProduct("Rhino Skin", "Zusätze", "", "Potassium Silikat")
            )
        ),
        FertilizerManufacturer(
            name = "General Hydroponics",
            products = listOf(
                FertilizerProduct("FloraGroß", "Grunddünger", "2-1-6", "Wachstumsphase"),
                FertilizerProduct("FloraBloom", "Blütedünger", "0-5-4", "Blütephase"),
                FertilizerProduct("FloraMicro", "Grunddünger", "5-0-1", "Mikronährstoffe"),
                FertilizerProduct("CALiMAGic", "Zusätze", "1-0-0", "Calcium Magnesium"),
                FertilizerProduct("Liquid KoolBloom", "Zusätze", "0-10-10", "Blütenbooster"),
                FertilizerProduct("Diamond Nectar", "Zusätze", "", "Fulvo- und Huminsäuren"),
                FertilizerProduct("FloraKleen", "Zusätze", "", "Spüllösung"),
                FertilizerProduct("RapidStart", "Zusätze", "", "Wurzelstimulator")
            )
        ),
        FertilizerManufacturer(
            name = "Biobizz",
            products = listOf(
                FertilizerProduct("Bio-grow", "Grunddünger", "4-3-6", "Organischer Wachstumsdünger"),
                FertilizerProduct("Bio-Bloom", "Blütedünger", "2-6-3.5", "Organischer Blütedünger"),
                FertilizerProduct("Fish-Mix", "Grunddünger", "5-1-4", "Organischer Fischemulsion"),
                FertilizerProduct("Top-Max", "Zusätze", "", "Organischer Blütenbooster"),
                FertilizerProduct("Bio-Heaven", "Zusätze", "", "Energiestimulator"),
                FertilizerProduct("Root-Juice", "Zusätze", "", "Organischer Wurzelstimulator"),
                FertilizerProduct("Acti-Vera", "Zusätze", "", "Aloe-Vera-basierter Pflanzenstärker"),
                FertilizerProduct("Alg-A-Mic", "Zusätze", "", "Algenstimulator"),
                FertilizerProduct("Cal-Mag", "Zusätze", "", "Calcium Magnesium organisch")
            )
        ),
        FertilizerManufacturer(
            name = "Canna",
            products = listOf(
                FertilizerProduct("Terra Vega", "Grunddünger", "3-1-3", "Erdsubstrat Wachstum"),
                FertilizerProduct("Terra Flores", "Blütedünger", "2-2-4", "Erdsubstrat Blüte"),
                FertilizerProduct("Hydro Vega A", "Grunddünger", "5-0-1", "Hydroponik Wachstum A"),
                FertilizerProduct("Hydro Vega B", "Grunddünger", "1-4-2", "Hydroponik Wachstum B"),
                FertilizerProduct("Hydro Flores A", "Blütedünger", "5-0-1", "Hydroponik Blüte A"),
                FertilizerProduct("Hydro Flores B", "Blütedünger", "1-4-2", "Hydroponik Blüte B"),
                FertilizerProduct("PK 13/14", "Zusätze", "0-13-14", "Phosphor Kalium Booster"),
                FertilizerProduct("Cannazym", "Zusätze", "", "Enzympräparat"),
                FertilizerProduct("Rhizotonic", "Zusätze", "", "Wurzelstimulator")
            )
        ),
        FertilizerManufacturer(
            name = "PlaGroßn",
            products = listOf(
                FertilizerProduct("Terra grow", "Grunddünger", "5-2-5", "Erdsubstrat Wachstum"),
                FertilizerProduct("Terra Bloom", "Blütedünger", "2-2-5", "Erdsubstrat Blüte"),
                FertilizerProduct("Hydro A&B", "Grunddünger", "5-0-3", "Hydroponik Komplett"),
                FertilizerProduct("Green Sensation", "Zusätze", "0-9-10", "4-in-1 Booster"),
                FertilizerProduct("Power Roots", "Zusätze", "", "Wurzelstimulator"),
                FertilizerProduct("Sugar Royal", "Zusätze", "", "Kohlenhydrate"),
                FertilizerProduct("Vita Race", "Zusätze", "", "Eisenchelat")
            )
        ),
        FertilizerManufacturer(
            name = "Hesi",
            products = listOf(
                FertilizerProduct("TNT Complex", "Grunddünger", "3-2-3", "Wachstumsdünger"),
                FertilizerProduct("Bloom Complex", "Blütedünger", "1-4-5", "Blütedünger"),
                FertilizerProduct("Root Complex", "Zusätze", "", "Wurzelstimulator"),
                FertilizerProduct("Boost", "Zusätze", "", "Blütenbooster"),
                FertilizerProduct("PK 13/14", "Zusätze", "0-13-14", "Phosphor Kalium"),
                FertilizerProduct("PowerZyme", "Zusätze", "", "Enzympräparat"),
                FertilizerProduct("SuperVit", "Zusätze", "", "Vitaminpräparat")
            )
        )
        ,
        // Additional manufacturers commonly used by growers
        FertilizerManufacturer(
            name = "FoxFarm",
            products = listOf(
                FertilizerProduct("Grow Big", "Grunddünger", "3-2-4", "Wachstumsdünger"),
                FertilizerProduct("Big Bloom", "Blütedünger", "0.01-0.03-0.04", "Blüte Booster (flüssig)")
            )
        ),
        FertilizerManufacturer(
            name = "Botanicare",
            products = listOf(
                FertilizerProduct("Pure Blend Pro Grow", "Grunddünger", "3-1-3", "Wachstum"),
                FertilizerProduct("Pure Blend Pro Bloom", "Blütedünger", "1-3-4", "Blüte"),
                FertilizerProduct("Cal-Mag Plus", "Zusätze", "", "Calcium/Magnesium Ergänzung")
            )
        ),
        FertilizerManufacturer(
            name = "House & Garden",
            products = listOf(
                FertilizerProduct("A&B Fertilizer", "Grunddünger", "5-0-1", "Komplettdünger A&B"),
                FertilizerProduct("Coco", "Grunddünger", "4-1-3", "Cocospezifische Serie")
            )
        ),
        FertilizerManufacturer(
            name = "Atami",
            products = listOf(
                FertilizerProduct("B'cuzz Grow", "Grunddünger", "3-1-4", "Wachstumsnährstoff"),
                FertilizerProduct("B'cuzz Bloom", "Blütedünger", "1-4-5", "Blüte")
            )
        ),
        FertilizerManufacturer(
            name = "Plagron",
            products = listOf(
                FertilizerProduct("Terra Grow", "Grunddünger", "3-1-3", "Erde Wachstum"),
                FertilizerProduct("Terra Bloom", "Blütedünger", "2-2-4", "Erde Blüte")
            )
        ),
        FertilizerManufacturer(
            name = "Grotek",
            products = listOf(
                FertilizerProduct("Growth", "Grunddünger", "4-0-1", "Wachstum"),
                FertilizerProduct("Bloom", "Blütedünger", "1-4-5", "Blüte")
            )
        ),
        FertilizerManufacturer(
            name = "Dutch Pro",
            products = listOf(
                FertilizerProduct("Start", "Grunddünger", "2-1-3", "Starter Nährstoff"),
                FertilizerProduct("Bloom", "Blütedünger", "1-3-4", "Blüte")
            )
        )
    )
}
