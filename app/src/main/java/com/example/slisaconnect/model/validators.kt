package com.example.slisaconnect.model



import java.time.LocalDate
import java.time.format.DateTimeParseException

/** Pure validation helpers shared by the forms and the repository. Each returns field name to message. */
object Validators {
    private val email = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
    private val phone = Regex("^[+\\d][\\d\\s-]{8,14}$")

    val provinces = listOf(
        "Eastern Cape", "Free State", "Gauteng", "KwaZulu-Natal", "Limpopo",
        "Mpumalanga", "North West", "Northern Cape", "Western Cape",
    )
    val ageGroups = listOf("Under 18", "18-24", "25-34", "35-44", "45+")
    val teamAgeGroups = listOf("U13", "U15", "U17", "U19", "Open")
    val teamCategories = listOf("Boys", "Girls", "Mixed")

    fun isEmail(text: String): Boolean = email.matches(text.trim())

    fun password(p: String): String? = when {
        p.length < 8 -> "Use at least 8 characters."
        p.all { it.isLetter() } || p.all { it.isDigit() } -> "Mix letters and numbers."
        else -> null
    }

    fun signUp(f: SignUpForm): Map<String, String> = buildMap {
        if (f.first.isBlank()) put("first", "Enter your first name.")
        if (f.last.isBlank()) put("last", "Enter your last name.")
        if (f.ageGroup !in ageGroups) put("ageGroup", "Choose your age group.")
        if (f.province !in provinces) put("province", "Choose your province.")
        if (!isEmail(f.email)) put("email", "Enter a valid email, like name@example.com.")
        if (f.phone.isNotBlank() && !phone.matches(f.phone.trim())) put("phone", "Use 9 to 15 digits, for example 082 555 0123.")
        password(f.password)?.let { put("password", it) }
        if (f.confirm != f.password) put("confirm", "The passwords don't match.")
    }

    fun profile(first: String, last: String, mail: String, tel: String): Map<String, String> = buildMap {
        if (first.isBlank()) put("first", "Enter your first name.")
        if (last.isBlank()) put("last", "Enter your last name.")
        if (!isEmail(mail)) put("email", "Enter a valid email, like name@example.com.")
        if (tel.isNotBlank() && !phone.matches(tel.trim())) put("phone", "Use 9 to 15 digits, for example 082 555 0123.")
    }

    /** Returns initials like "T.M." or null when the text is not 2 or 3 letters. Full names are refused on purpose. */
    fun initials(text: String): String? {
        val letters = text.filter { it != '.' && !it.isWhitespace() }
        if (letters.length !in 2..3 || !letters.all { it.isLetter() }) return null
        return letters.uppercase().map { "$it." }.joinToString("")
    }

    fun team(name: String, sport: String, ageGroup: String, category: String, province: String, coach: String): Map<String, String> = buildMap {
        if (name.trim().length < 3) put("name", "Enter a team name with at least 3 letters.")
        if (sport !in listOf("Soccer", "Netball")) put("sport", "Choose soccer or netball.")
        if (ageGroup !in teamAgeGroups) put("ageGroup", "Choose an age group.")
        if (category !in teamCategories) put("category", "Choose boys, girls or mixed.")
        if (province !in provinces) put("province", "Choose a province.")
        if (coach.isBlank()) put("coach", "Enter the coach's name.")
    }

    fun teamContact(description: String, mail: String, tel: String): Map<String, String> = buildMap {
        if (description.length > 200) put("description", "Keep the description under 200 characters.")
        if (mail.isNotBlank() && !isEmail(mail)) put("email", "Enter a valid email or leave it blank.")
        if (tel.isNotBlank() && !phone.matches(tel.trim())) put("phone", "Use 9 to 15 digits or leave it blank.")
    }

    fun parseDate(text: String): LocalDate? =
        try { LocalDate.parse(text.trim()) } catch (_: DateTimeParseException) { null }

    fun event(title: String, venue: String, description: String, date: LocalDate?, deadline: LocalDate?): Map<String, String> = buildMap {
        if (title.isBlank()) put("title", "Enter an event name.")
        if (title.length > 80) put("title", "Keep the name under 80 characters.")
        if (venue.isBlank()) put("venue", "Enter a venue.")
        if (description.length > 600) put("description", "Keep the description under 600 characters.")
        if (date == null) put("date", "Choose the event date.")
        if (deadline == null) put("deadline", "Choose the last day to respond.")
        if (date != null && deadline != null && deadline.isAfter(date)) {
            put("deadline", "The last day to respond must be on or before the event.")
        }
    }

    fun article(title: String, body: String): Map<String, String> = buildMap {
        if (title.isBlank()) put("title", "Enter a title.")
        if (title.length > 100) put("title", "Keep the title under 100 characters.")
        if (body.trim().length < 20) put("body", "Write at least a couple of sentences.")
        if (body.length > 6000) put("body", "Keep the article under 6000 characters.")
    }

    fun resource(name: String, contact: String): Map<String, String> = buildMap {
        if (name.isBlank()) put("name", "Enter the name of the service.")
        if (contact.isBlank()) put("contact", "Enter a phone number or other way to reach them.")
    }
}
