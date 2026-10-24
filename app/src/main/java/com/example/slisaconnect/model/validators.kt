package com.example.slisaconnect.model


import java.time.LocalDate
import java.time.format.DateTimeParseException

/** Pure validation helpers shared by the forms and the repository. Each returns field name to message. */
object Validators {
    private val email = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
    private val phone = Regex("^[+\\d][\\d\\s-]{8,14}$")

    fun profile(first: String, last: String, mail: String, tel: String): Map<String, String> = buildMap {
        if (first.isBlank()) put("first", "Enter your first name.")
        if (last.isBlank()) put("last", "Enter your last name.")
        if (!email.matches(mail.trim())) put("email", "Enter a valid email, like name@example.com.")
        if (tel.isNotBlank() && !phone.matches(tel.trim())) put("phone", "Use 9 to 15 digits, for example 082 555 0123.")
    }

    /** Returns initials like "T.M." or null when the text is not 2 or 3 letters. Full names are refused on purpose. */
    fun initials(text: String): String? {
        val letters = text.filter { it != '.' && !it.isWhitespace() }
        if (letters.length !in 2..3 || !letters.all { it.isLetter() }) return null
        return letters.uppercase().map { "$it." }.joinToString("")
    }

    fun team(name: String, sport: String, ageGroup: String, coach: String): Map<String, String> = buildMap {
        if (name.trim().length < 3) put("name", "Enter a team name with at least 3 letters.")
        if (sport !in listOf("Soccer", "Netball")) put("sport", "Choose soccer or netball.")
        if (ageGroup.isBlank()) put("ageGroup", "Choose an age group.")
        if (coach.isBlank()) put("coach", "Enter the coach's name.")
    }

    fun teamContact(description: String, mail: String, tel: String): Map<String, String> = buildMap {
        if (description.length > 200) put("description", "Keep the description under 200 characters.")
        if (mail.isNotBlank() && !email.matches(mail.trim())) put("email", "Enter a valid email or leave it blank.")
        if (tel.isNotBlank() && !phone.matches(tel.trim())) put("phone", "Use 9 to 15 digits or leave it blank.")
    }

    fun parseDate(text: String): LocalDate? =
        try { LocalDate.parse(text.trim()) } catch (_: DateTimeParseException) { null }

    fun event(title: String, venue: String, date: LocalDate?, deadline: LocalDate?): Map<String, String> = buildMap {
        if (title.isBlank()) put("title", "Enter an event name.")
        if (venue.isBlank()) put("venue", "Enter a venue.")
        if (date == null) put("date", "Use the format 2026-11-21.")
        if (deadline == null) put("deadline", "Use the format 2026-11-07.")
        if (date != null && deadline != null && deadline.isAfter(date)) {
            put("deadline", "Deadline must be on or before the event.")
        }
    }
}
