package no.nav.fo.service

fun parseADRolle(rawRolleStrenger: List<String>): List<String> =
        rawRolleStrenger.map {
            check(it.startsWith("CN="), { "Feil format på AD-rolle: " + it })
            it.split(",")[0].split("CN=")[1]
        }
