package org.catholiccompanion.app.model

enum class AngelusLanguage(
    val displayName: String,
) {
    ENGLISH("English"),
    LATIN("Latin"),
}

data class AngelusPart(
    val role: String?,
    val english: String,
    val latin: String,
) {
    fun text(language: AngelusLanguage): String = when (language) {
        AngelusLanguage.ENGLISH -> english
        AngelusLanguage.LATIN -> latin
    }
}

object AngelusPrayer {
    private const val ENGLISH_HAIL_MARY =
        "Hail, Mary, full of grace, the Lord is with thee. Blessed art thou among women, and blessed is " +
            "the fruit of thy womb, Jesus. Holy Mary, Mother of God, pray for us sinners, now and at the " +
            "hour of our death. Amen."

    private const val LATIN_HAIL_MARY =
        "Ave, María, grátia plena, Dóminus tecum. Benedícta tu in muliéribus, et benedíctus fructus " +
            "ventris tui, Iesus. Sancta María, Mater Dei, ora pro nobis peccatóribus, nunc et in hora " +
            "mortis nostrae. Amen."

    val parts: List<AngelusPart> = listOf(
        AngelusPart(
            role = "V.",
            english = "The Angel of the Lord declared unto Mary.",
            latin = "Ángelus Dómini nuntiávit Maríae.",
        ),
        AngelusPart(
            role = "R.",
            english = "And she conceived of the Holy Spirit.",
            latin = "Et concépit de Spíritu Sancto.",
        ),
        AngelusPart(role = null, english = ENGLISH_HAIL_MARY, latin = LATIN_HAIL_MARY),
        AngelusPart(
            role = "V.",
            english = "Behold the handmaid of the Lord.",
            latin = "Ecce ancílla Dómini.",
        ),
        AngelusPart(
            role = "R.",
            english = "Be it done unto me according to thy word.",
            latin = "Fiat mihi secúndum verbum tuum.",
        ),
        AngelusPart(role = null, english = ENGLISH_HAIL_MARY, latin = LATIN_HAIL_MARY),
        AngelusPart(
            role = "V.",
            english = "And the Word was made flesh.",
            latin = "Et Verbum caro factum est.",
        ),
        AngelusPart(
            role = "R.",
            english = "And dwelt among us.",
            latin = "Et habitávit in nobis.",
        ),
        AngelusPart(role = null, english = ENGLISH_HAIL_MARY, latin = LATIN_HAIL_MARY),
        AngelusPart(
            role = "V.",
            english = "Pray for us, O holy Mother of God.",
            latin = "Ora pro nobis, sancta Dei Génetrix.",
        ),
        AngelusPart(
            role = "R.",
            english = "That we may be made worthy of the promises of Christ.",
            latin = "Ut digni efficiámur promissiónibus Christi.",
        ),
        AngelusPart(
            role = "Let us pray",
            english = "Pour forth, we beseech thee, O Lord, thy grace into our hearts; that we, to whom " +
                "the Incarnation of Christ, thy Son, was made known by the message of an angel, may by his " +
                "Passion and Cross be brought to the glory of his Resurrection. Through the same Christ, " +
                "our Lord. Amen.",
            latin = "Orémus. Grátiam tuam, quáesumus, Dómine, méntibus nostris infúnde; ut qui, Ángelo " +
                "nuntiánte, Christi Fílii tui incarnatiónem cognóvimus, per passiónem eius et crucem ad " +
                "resurrectiónis glóriam perducámur. Per eúndem Christum Dóminum nostrum. Amen.",
        ),
    )

    const val ENGLISH_SOURCE_URL = "https://www.usccb.org/prayers/angelus-0"
    const val LATIN_SOURCE_URL =
        "https://www.vatican.va/content/dam/vatican/ra/finestra-palazzo-apostolico/documents/angelus.pdf"
}
