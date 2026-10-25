package com.example.slisaconnect.data

import java.time.LocalDate
import com.example.slisaconnect.model.*

/** Demo data for the prototype. Replace with a real backend later. */
object SeedData {
    private val pool = listOf(
        "T.M.", "L.N.", "S.D.", "A.Z.", "K.M.", "N.K.", "P.S.", "Z.M.",
        "B.D.", "M.R.", "R.T.", "C.N.", "J.M.", "F.K.", "H.S.", "G.L.",
    )

    private fun players(count: Int, consent: Int, age: Int, seed: Int) =
        List(count) { i -> Player(pool[(i + seed) % pool.size], age - (i % 2), i < consent) }

    private fun profile(first: String, last: String, email: String, province: String, age: String) =
        UserProfile(first, last, email, "", province, age)

    fun initial(): AppState = AppState(
        currentUserId = "u1",
        users = listOf(
            AppUser("u1", "Thandi Mokoena", Role.ADMIN, "Gauteng", true, "Today"),
            AppUser("u2", "Sipho Dlamini", Role.COORDINATOR, "Gauteng", true, "Today"),
            AppUser("u3", "Naledi Khumalo", Role.SUPPORT, "KwaZulu-Natal", true, "Yesterday"),
            AppUser("u4", "Ayanda Zulu", Role.FACILITATOR, "Gauteng", true, "2 days ago"),
            AppUser("u5", "Lerato Mahlangu", Role.MANAGER, "Gauteng", true, "Today"),
            AppUser("u6", "Kabelo Nkosi", Role.MEMBER, "Gauteng", true, "1 week ago"),
            AppUser("u7", "Nomsa Radebe", Role.MANAGER, "Gauteng", true, "3 days ago"),
            AppUser("u8", "Pieter van Wyk", Role.MANAGER, "Western Cape", true, "5 days ago"),
            AppUser("u9", "Zanele Sithole", Role.MEMBER, "KwaZulu-Natal", false, "2 months ago"),
        ),
        profiles = mapOf(
            "u1" to profile("Thandi", "Mokoena", "thandi@slisaconnect.example", "Gauteng", "25-34"),
            "u2" to profile("Sipho", "Dlamini", "sipho@slisaconnect.example", "Gauteng", "35-44"),
            "u3" to profile("Naledi", "Khumalo", "naledi@slisaconnect.example", "KwaZulu-Natal", "25-34"),
            "u4" to profile("Ayanda", "Zulu", "ayanda@slisaconnect.example", "Gauteng", "25-34"),
            "u5" to profile("Lerato", "Mahlangu", "lerato@example.com", "Gauteng", "25-34"),
            "u6" to profile("Kabelo", "Nkosi", "kabelo@example.com", "Gauteng", "18-24"),
        ),
        teams = listOf(
            Team("t1", "SLC-T-26-0041", "Soweto Stars FC", "Soccer", "U15", "Mr M. Radebe", "u5",
                TeamStatus.SUBMITTED, "", LocalDate.of(2026, 10, 21), players(14, 14, 15, 0),
                ChallengeStatus.NOT_STARTED, listOf(CompetitionEntry("Youth Cup Gauteng", EntryStatus.PENDING)),
                description = "Under-15 boys from Soweto. We train on Tuesdays and Thursdays.", coachIds = listOf("u6")),
            Team("t2", "SLC-T-26-0038", "Alex Netball Queens", "Netball", "U17", "Ms N. Mthembu", "u7",
                TeamStatus.UNDER_REVIEW, "", LocalDate.of(2026, 10, 18), players(11, 9, 17, 4),
                ChallengeStatus.SUBMITTED, listOf(CompetitionEntry("Play for Change Netball Day", EntryStatus.PENDING))),
            Team("t3", "SLC-T-26-0029", "Mamelodi Rising", "Soccer", "U13", "Mr T. Sibiya", "u7",
                TeamStatus.APPROVED, "", LocalDate.of(2026, 10, 3), players(16, 16, 13, 7),
                ChallengeStatus.APPROVED,
                listOf(
                    CompetitionEntry("Youth Cup Gauteng", EntryStatus.ACCEPTED),
                    CompetitionEntry("Sports Day Tshwane", EntryStatus.PENDING),
                )),
            Team("t4", "SLC-T-26-0034", "Khayelitsha Flyers", "Netball", "U17", "Ms A. Jacobs", "u8",
                TeamStatus.ACTION_REQUIRED,
                "Guardian consent is missing for 3 players. Please send the consent forms again.",
                LocalDate.of(2026, 10, 12), players(13, 10, 17, 2), ChallengeStatus.NOT_STARTED, emptyList()),
            Team("t5", "SLC-T-26-0043", "Diepkloof Dribblers", "Soccer", "U17", "Mr L. Naidoo", "u6",
                TeamStatus.SUBMITTED, "", LocalDate.of(2026, 10, 23), players(12, 12, 17, 9),
                ChallengeStatus.NOT_STARTED, listOf(CompetitionEntry("Youth Cup Gauteng", EntryStatus.PENDING))),
        ),
        events = listOf(
            SportsEvent("e1", "Youth Cup Gauteng", EventType.TOURNAMENT, "Soccer", LocalDate.of(2026, 11, 21),
                "Orlando Stadium Grounds, Soweto", LocalDate.of(2026, 11, 7), true),
            SportsEvent("e2", "Play for Change Netball Day", EventType.PLAY_FOR_CHANGE, "Netball", LocalDate.of(2026, 11, 28),
                "Alexandra Sports Complex", LocalDate.of(2026, 11, 14), true),
            SportsEvent("e3", "Coaches safeguarding workshop", EventType.TRAINING, "Any", LocalDate.of(2026, 11, 5),
                "SLiSA Hub, Johannesburg", LocalDate.of(2026, 11, 1), true),
            SportsEvent("e4", "Sports Day Tshwane", EventType.SPORTS_DAY, "Any", LocalDate.of(2026, 12, 5),
                "Pretoria Showgrounds", LocalDate.of(2026, 11, 20), false),
        ),
        resources = listOf(
            SupportResource("r1", "Care centre, Soweto", "Safety & support", "011 ••• ••42", ResourceStatus.SUBMITTED, "Partner NGO"),
            SupportResource("r2", "Youth-friendly clinic, Alexandra", "Health & wellness", "011 ••• ••17", ResourceStatus.VERIFIED, "Volunteer"),
            SupportResource("r3", "Legal aid advice line", "Know your rights", "0800 ••• •••", ResourceStatus.PUBLISHED, "Admin"),
        ),
        audit = listOf(
            AuditEntry("08:12", "Sipho Dlamini", "Approved Mamelodi Rising"),
            AuditEntry("Yesterday", "Thandi Mokoena", "Published Youth Cup Gauteng"),
        ),
    )
}
