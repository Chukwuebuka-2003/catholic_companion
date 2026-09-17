package org.catholiccompanion.app.model

import java.time.DayOfWeek

data class Mystery(
    val title: String,
    val scriptureReference: String,
    val summary: String,
    val fruit: String,
    val meditation: String,
)

enum class MysterySet(
    val displayName: String,
    val mysteries: List<Mystery>,
) {
    JOYFUL(
        displayName = "Joyful Mysteries",
        mysteries = listOf(
            Mystery(
                title = "The Annunciation",
                scriptureReference = "Luke 1:26–38",
                summary = "The angel Gabriel comes to Mary in Nazareth and announces that she will " +
                    "conceive Jesus, the Son of the Most High, by the Holy Spirit. Mary asks how this " +
                    "will happen, receives Gabriel’s answer in faith, and freely entrusts herself to " +
                    "God’s will.",
                fruit = "Humility and obedience to God",
                meditation = "Place yourself beside Mary at the Annunciation. Ask for the humility to " +
                    "listen and the courage to say yes when God’s invitation unsettles your plans.",
            ),
            Mystery(
                title = "The Visitation",
                scriptureReference = "Luke 1:39–56",
                summary = "Mary travels in haste to help her relative Elizabeth. At Mary’s greeting, " +
                    "John leaps in Elizabeth’s womb; Elizabeth blesses Mary, and Mary praises God in " +
                    "the Magnificat for remembering the lowly and fulfilling his promises.",
                fruit = "Charity and love of neighbour",
                meditation = "Contemplate Mary carrying Christ to another person. Ask for a generous " +
                    "heart that notices who needs your presence and serves without waiting to be asked.",
            ),
            Mystery(
                title = "The Nativity",
                scriptureReference = "Luke 2:1–20",
                summary = "Jesus is born in Bethlehem and laid in a manger because there is no room " +
                    "for the Holy Family in the lodging place. Angels announce good news to shepherds, " +
                    "who find Mary, Joseph, and the infant and return glorifying God.",
                fruit = "Poverty of spirit and love of Christ",
                meditation = "Rest before the humility of God made small for us. Ask to receive Jesus " +
                    "simply, to loosen your grip on possessions, and to make room for him in daily life.",
            ),
            Mystery(
                title = "The Presentation",
                scriptureReference = "Luke 2:22–40",
                summary = "Mary and Joseph bring Jesus to the Temple in obedience to the Law. Simeon " +
                    "recognises the promised salvation and foretells both Christ’s mission and the " +
                    "sorrow Mary will bear; the prophet Anna gives thanks and speaks of the child.",
                fruit = "Purity of heart and faithful obedience",
                meditation = "Watch Mary and Joseph place what is most precious to them in God’s hands. " +
                    "Ask for a purified heart that offers your work, relationships, and future to God.",
            ),
            Mystery(
                title = "The Finding in the Temple",
                scriptureReference = "Luke 2:41–52",
                summary = "After searching for three days, Mary and Joseph find the twelve-year-old " +
                    "Jesus among the teachers in the Temple. Jesus speaks of his Father’s house, then " +
                    "returns with them to Nazareth and lives in obedience as Mary treasures these events.",
                fruit = "Seeking Jesus and fidelity to duty",
                meditation = "Bring to Jesus whatever feels lost or confusing. Ask to seek him " +
                    "persistently, to recognise the Father’s work, and to be faithful in ordinary duties.",
            ),
        ),
    ),
    LUMINOUS(
        displayName = "Luminous Mysteries",
        mysteries = listOf(
            Mystery(
                title = "The Baptism of Jesus",
                scriptureReference = "Matthew 3:13–17",
                summary = "Jesus enters the Jordan and receives baptism from John, identifying himself " +
                    "with sinners even though he is without sin. The heavens open, the Spirit descends " +
                    "like a dove, and the Father reveals Jesus as his beloved Son.",
                fruit = "Openness to the Holy Spirit",
                meditation = "Remember the grace of your own Baptism. Ask the Holy Spirit to renew your " +
                    "identity as God’s beloved child and help you live your baptismal promises today.",
            ),
            Mystery(
                title = "The Wedding at Cana",
                scriptureReference = "John 2:1–12",
                summary = "When the wine fails at a wedding feast, Mary brings the need to Jesus and " +
                    "directs the servants to do whatever he tells them. Jesus changes water into wine, " +
                    "revealing his glory in the first of his signs, and his disciples believe in him.",
                fruit = "Trust in Jesus through Mary",
                meditation = "Let Mary bring your unspoken needs to her Son. Ask for the trust of the " +
                    "servants, ready to obey Jesus even before you can see how he will act.",
            ),
            Mystery(
                title = "The Proclamation of the Kingdom",
                scriptureReference = "Mark 1:14–15; Matthew 5–7",
                summary = "Jesus announces that God’s Kingdom is at hand and calls everyone to repent " +
                    "and believe the Gospel. Through his teaching, forgiveness, healings, and mercy, he " +
                    "reveals the Father’s reign and invites disciples into a transformed way of life.",
                fruit = "Conversion and trust in the Gospel",
                meditation = "Hear Jesus address his call to you personally. Ask where your heart must " +
                    "change, and choose one concrete way to live his mercy, truth, and justice today.",
            ),
            Mystery(
                title = "The Transfiguration",
                scriptureReference = "Luke 9:28–36",
                summary = "Jesus takes Peter, John, and James up the mountain, where his appearance " +
                    "becomes radiant and Moses and Elijah speak with him about the exodus he will fulfil " +
                    "in Jerusalem. From the cloud, the Father commands the disciples to listen to his Son.",
                fruit = "Desire for holiness",
                meditation = "Contemplate the glory of Christ shining through his humanity. Ask for grace " +
                    "to listen to him faithfully and to carry the light of prayer into suffering and service.",
            ),
            Mystery(
                title = "The Institution of the Eucharist",
                scriptureReference = "Luke 22:14–20; 1 Corinthians 11:23–26",
                summary = "At the Last Supper, Jesus gives the bread and cup to his disciples as his Body " +
                    "and Blood, the new covenant poured out for them, and commands them to do this in " +
                    "memory of him. He gives himself as food on the eve of his sacrifice on the Cross.",
                fruit = "Love of the Eucharist",
                meditation = "Remain with Jesus as he places his life in the disciples’ hands. Ask for " +
                    "deeper faith in his Eucharistic presence and a life shaped by thanksgiving and self-gift.",
            ),
        ),
    ),
    SORROWFUL(
        displayName = "Sorrowful Mysteries",
        mysteries = listOf(
            Mystery(
                title = "The Agony in the Garden",
                scriptureReference = "Matthew 26:36–46",
                summary = "In Gethsemane, Jesus is overwhelmed with sorrow and asks his disciples to " +
                    "keep watch. He prays that the cup may pass, yet freely submits to the Father’s will. " +
                    "The disciples sleep, and Jesus rises to meet the one who betrays him.",
                fruit = "Sorrow for sin and obedience to God",
                meditation = "Keep watch with Jesus in his anguish. Place your fear and reluctance beside " +
                    "his, asking for strength to pray honestly and still entrust yourself to the Father.",
            ),
            Mystery(
                title = "The Scourging at the Pillar",
                scriptureReference = "John 19:1; Mark 15:6–15",
                summary = "Though Pilate finds no guilt deserving death, he has Jesus scourged and hands " +
                    "him over to be crucified under pressure from the crowd. The innocent Christ accepts " +
                    "violence and humiliation without abandoning his saving love.",
                fruit = "Purity and self-discipline",
                meditation = "Look with compassion on the wounded Christ. Pray for those whose bodies are " +
                    "abused or degraded, and ask for purity that honours your own body and every person.",
            ),
            Mystery(
                title = "The Crowning with Thorns",
                scriptureReference = "Matthew 27:27–31",
                summary = "The soldiers dress Jesus in a scarlet cloak, force a crown of thorns onto his " +
                    "head, place a reed in his hand, and mock him as king. The true King endures contempt " +
                    "and cruelty, answering human pride with patient love.",
                fruit = "Moral courage and humility",
                meditation = "Stand near Jesus as he is mocked and misunderstood. Ask for freedom from " +
                    "pride and for courage to remain faithful when truth or goodness brings ridicule.",
            ),
            Mystery(
                title = "The Carrying of the Cross",
                scriptureReference = "Luke 23:26–32",
                summary = "Jesus carries the instrument of his execution toward Calvary. Simon of Cyrene " +
                    "is made to carry the cross behind him, and Jesus speaks to the women who mourn. He " +
                    "continues the road of self-giving love despite exhaustion, rejection, and pain.",
                fruit = "Patience in trials",
                meditation = "Walk beside Christ on the road to Calvary. Ask him to help you carry today’s " +
                    "cross without bitterness and to become a willing Simon for someone else.",
            ),
            Mystery(
                title = "The Crucifixion",
                scriptureReference = "Luke 23:33–49; John 19:25–30",
                summary = "Jesus is crucified between criminals, forgives his executioners, entrusts his " +
                    "mother and beloved disciple to one another, and promises paradise to the repentant " +
                    "thief. He completes his offering and dies, entrusting his spirit to the Father.",
                fruit = "Perseverance and forgiving love",
                meditation = "Remain at the foot of the Cross with Mary. Receive Christ’s mercy, name the " +
                    "person you need help to forgive, and entrust the suffering and dying to his love.",
            ),
        ),
    ),
    GLORIOUS(
        displayName = "Glorious Mysteries",
        mysteries = listOf(
            Mystery(
                title = "The Resurrection",
                scriptureReference = "Matthew 28:1–10; John 20:1–29",
                summary = "On the first day of the week, the tomb is found empty and the risen Jesus " +
                    "appears to his disciples. He transforms their fear and grief into faith and joy. " +
                    "Death does not have the final word: Christ is alive and sends his followers to witness.",
                fruit = "Faith and hope",
                meditation = "Stand before the empty tomb and hear Christ call you out of fear. Ask for " +
                    "resurrection faith, especially in the places where hope seems buried or impossible.",
            ),
            Mystery(
                title = "The Ascension",
                scriptureReference = "Acts 1:6–11; Luke 24:46–53",
                summary = "The risen Jesus commissions the apostles to be his witnesses and promises the " +
                    "power of the Holy Spirit. He is taken from their sight and returns to the Father, not " +
                    "to abandon his Church but to reign and draw humanity toward its heavenly destiny.",
                fruit = "Hope for heaven and faithful mission",
                meditation = "Look toward Christ without withdrawing from the world he loves. Ask for hope " +
                    "in eternal life and the boldness to witness through your words, work, and relationships.",
            ),
            Mystery(
                title = "The Descent of the Holy Spirit",
                scriptureReference = "Acts 2:1–13",
                summary = "As Mary and the disciples are gathered at Pentecost, a sound like a mighty wind " +
                    "fills the house and tongues as of fire rest upon them. Filled with the Holy Spirit, " +
                    "they proclaim God’s mighty works so that people of many nations can understand.",
                fruit = "Wisdom and zeal for mission",
                meditation = "Join Mary and the disciples in expectant prayer. Invite the Holy Spirit into " +
                    "your gifts and weaknesses, asking to speak and act so others can recognise God’s love.",
            ),
            Mystery(
                title = "The Assumption of Mary",
                scriptureReference = "Revelation 12:1; Luke 1:46–55",
                summary = "Catholic faith holds that, at the end of her earthly life, Mary was taken body " +
                    "and soul into heavenly glory. Scripture does not narrate the event directly; the Church " +
                    "contemplates it in light of Mary’s union with her risen Son and the promised victory over death.",
                fruit = "Grace for a holy death and devotion to Mary",
                meditation = "Contemplate Mary sharing fully in her Son’s victory. Entrust your life and death " +
                    "to Christ, and ask for grace to journey toward heaven with steady hope.",
            ),
            Mystery(
                title = "The Coronation of Mary",
                scriptureReference = "Revelation 12:1; Luke 1:46–55",
                summary = "The Church contemplates Mary in heavenly glory as Queen, sharing in the triumph " +
                    "of Christ the King and interceding for his people. Scripture does not describe a " +
                    "coronation ceremony; the mystery draws on the woman crowned with stars and Mary’s praise of God.",
                fruit = "Perseverance in grace and trust in Mary’s intercession",
                meditation = "Rejoice that God exalts the lowly and brings faithful love to fulfilment. Ask " +
                    "Mary to pray for your perseverance and to lead every honour back to her Son.",
            ),
        ),
    ),
    ;

    companion object {
        fun recommendedFor(day: DayOfWeek): MysterySet = when (day) {
            DayOfWeek.MONDAY, DayOfWeek.SATURDAY -> JOYFUL
            DayOfWeek.TUESDAY, DayOfWeek.FRIDAY -> SORROWFUL
            DayOfWeek.THURSDAY -> LUMINOUS
            DayOfWeek.WEDNESDAY, DayOfWeek.SUNDAY -> GLORIOUS
        }
    }
}

data class PrayerStep(
    val id: String,
    val title: String,
    val instruction: String,
    val text: String,
    val decade: Int? = null,
)

data class RosaryProgress(
    val mysterySet: MysterySet,
    val stepIndex: Int,
)

object RosaryCatalog {
    private const val SIGN_OF_CROSS =
        "In the name of the Father, and of the Son, and of the Holy Spirit. Amen."

    private const val APOSTLES_CREED =
        "I believe in God, the Father almighty, Creator of heaven and earth, and in Jesus Christ, " +
            "his only Son, our Lord, who was conceived by the Holy Spirit, born of the Virgin Mary, " +
            "suffered under Pontius Pilate, was crucified, died and was buried; he descended into hell; " +
            "on the third day he rose again from the dead; he ascended into heaven, and is seated at the " +
            "right hand of God the Father almighty; from there he will come to judge the living and the dead. " +
            "I believe in the Holy Spirit, the holy catholic Church, the communion of saints, the forgiveness " +
            "of sins, the resurrection of the body, and life everlasting. Amen."

    private const val OUR_FATHER =
        "Our Father, who art in heaven, hallowed be thy name; thy kingdom come; thy will be done on earth " +
            "as it is in heaven. Give us this day our daily bread; and forgive us our trespasses as we forgive " +
            "those who trespass against us; and lead us not into temptation, but deliver us from evil. Amen."

    private const val HAIL_MARY =
        "Hail Mary, full of grace, the Lord is with thee. Blessed art thou among women, and blessed is the " +
            "fruit of thy womb, Jesus. Holy Mary, Mother of God, pray for us sinners, now and at the hour " +
            "of our death. Amen."

    private const val GLORY_BE =
        "Glory be to the Father, and to the Son, and to the Holy Spirit, as it was in the beginning, " +
            "is now, and ever shall be, world without end. Amen."

    private const val FATIMA_PRAYER =
        "O my Jesus, forgive us our sins, save us from the fires of hell, lead all souls to heaven, " +
            "especially those most in need of thy mercy."

    private const val HAIL_HOLY_QUEEN =
        "Hail, holy Queen, Mother of mercy, hail, our life, our sweetness and our hope. To thee do we cry, " +
            "poor banished children of Eve; to thee do we send up our sighs, mourning and weeping in this " +
            "valley of tears. Turn then, most gracious advocate, thine eyes of mercy toward us; and after this " +
            "our exile, show unto us the blessed fruit of thy womb, Jesus. O clement, O loving, O sweet Virgin Mary."

    private const val CONCLUDING_PRAYER =
        "O God, whose only-begotten Son, by his life, death and resurrection, has purchased for us the rewards " +
            "of eternal life; grant, we beseech thee, that meditating upon these mysteries of the most holy " +
            "Rosary of the Blessed Virgin Mary, we may imitate what they contain and obtain what they promise, " +
            "through the same Christ our Lord. Amen."

    fun stepsFor(set: MysterySet): List<PrayerStep> = buildList {
        add(PrayerStep("opening-sign", "Sign of the Cross", "Begin the Rosary", SIGN_OF_CROSS))
        add(PrayerStep("creed", "Apostles’ Creed", "On the crucifix", APOSTLES_CREED))
        add(PrayerStep("opening-our-father", "Our Father", "On the first large bead", OUR_FATHER))
        repeat(3) { index ->
            add(
                PrayerStep(
                    id = "opening-hail-mary-${index + 1}",
                    title = "Hail Mary ${index + 1} of 3",
                    instruction = "For an increase in faith, hope and charity",
                    text = HAIL_MARY,
                ),
            )
        }
        add(PrayerStep("opening-glory", "Glory Be", "Before the first decade", GLORY_BE))

        set.mysteries.forEachIndexed { mysteryIndex, mystery ->
            val decade = mysteryIndex + 1
            add(
                PrayerStep(
                    id = "mystery-$decade",
                    title = "$decade. ${mystery.title}",
                    instruction = "Announce the mystery · ${mystery.scriptureReference}",
                    text = "Pause and contemplate this mystery before beginning the decade.",
                    decade = decade,
                ),
            )
            add(PrayerStep("decade-$decade-our-father", "Our Father", "On the large bead", OUR_FATHER, decade))
            repeat(10) { beadIndex ->
                add(
                    PrayerStep(
                        id = "decade-$decade-hail-mary-${beadIndex + 1}",
                        title = "Hail Mary ${beadIndex + 1} of 10",
                        instruction = mystery.title,
                        text = HAIL_MARY,
                        decade = decade,
                    ),
                )
            }
            add(PrayerStep("decade-$decade-glory", "Glory Be", "At the end of the decade", GLORY_BE, decade))
            add(
                PrayerStep(
                    id = "decade-$decade-fatima",
                    title = "Fatima Prayer",
                    instruction = "Customary optional prayer",
                    text = FATIMA_PRAYER,
                    decade = decade,
                ),
            )
        }

        add(PrayerStep("hail-holy-queen", "Hail, Holy Queen", "After the five decades", HAIL_HOLY_QUEEN))
        add(PrayerStep("concluding-prayer", "Concluding Prayer", "Let us pray", CONCLUDING_PRAYER))
        add(PrayerStep("closing-sign", "Sign of the Cross", "Conclude the Rosary", SIGN_OF_CROSS))
    }
}
