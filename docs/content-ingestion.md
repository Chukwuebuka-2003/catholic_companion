# Liturgical content ingestion

The app ships a reviewed, versioned 2026 calendar bundle and keeps its scope and
content provenance visible to the user.

The selected first scope is the **General Roman Calendar** in English. It is the
universal baseline only: it does not claim Nigerian, Vatican City, diocesan, or
parish-local accuracy. The FastAPI importer in `backend/` creates immutable draft
releases from LitCal for comparison and review; drafts are not served to the app.

Before importing a release:

1. Add every calendar, lectionary and prayer source to `source-register.csv`.
2. Record redistribution, offline storage, AI-processing and quotation permissions separately in `allowed_uses`.
3. Obtain reviewer sign-off for the exact scope and coverage dates.
4. Map the release to `CalendarBundle`: one scope, deterministic days, one primary celebration per day, explicit alternatives and ordered reading references.
5. Run `CalendarBundleValidator` before calling `LiturgyRepository.install`.
6. Compare every date in the declared coverage against the reviewed reference before publication.

Reading text is nullable by design. When redistribution is not permitted, store the citation, publisher title and HTTPS source link without copying the text.

## 2026 daily Lectionary schedule

The bundled release contains a primary Mass reading schedule for every day from
1 January through 31 December 2026: first reading, responsorial psalm, second
reading when appointed, and Gospel. It follows Sunday cycle A through Christ the
King, changes to cycle B on the First Sunday of Advent, and uses weekday cycle II.

The reproducible normalizer is `scripts/import_2026_lectionary.py`. Its source is
the MIT-published Catholic Readings API dataset pinned to commit `973e9864`.
That source follows the United States calendar, so the importer applies eight
reviewable overrides for the universal General Roman scope: 4–6 January, 17 May,
4 and 7 June, 26 November, and 12 December. These restore the universal dates
for Epiphany, Ascension and Corpus Christi and remove United States-only proper
selections. The importer rejects missing dates or missing essential readings.

Run it after checking out that pinned source revision:

```shell
python3 scripts/import_2026_lectionary.py /path/to/catholic-readings-api/readings/2026
```

This release covers the standard Mass-of-the-day selection. It does not contain
every vigil, ritual Mass, votive Mass, local celebration, optional common, or
Gospel-acclamation verse.

## Bundled English Scripture

The app bundles the public-domain **World English Bible, Catholic Edition**
(2020 stable text edition), including all 73 books in the traditional Catholic
order. It is a Bible text for offline reading, not the official vernacular
Lectionary for Mass. The UI states that distinction wherever daily readings are
shown.

The reproducible importer is `scripts/import_web_c.py`. It consumes the official
eBible.org VPL and USFM archives, writes one compact verse file per book, and
records both archive SHA-256 hashes in the generated manifest. It does not
silently substitute text from a different translation. Lettered lectionary
references such as `3b` are displayed as the complete source verse and labelled
accordingly.
