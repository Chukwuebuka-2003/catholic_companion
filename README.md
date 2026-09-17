# Catholic Companion Android

Native Kotlin/Jetpack Compose implementation

## Implemented foundation

- Three primary destinations: Today, Rosary and Learn.
- Secondary Settings destination.
- Complete local five-decade Rosary for all four mystery sets.
- Customary day-based mystery suggestion with manual override.
- Automatic on-device progress persistence using Preferences DataStore.
- Previous/next controls, progress semantics, confirmation before ending, dark mode and large-text-friendly layouts.
- An offline 2026 General Roman Calendar release, with explicit scope and provenance.
- A complete 365-day primary Mass reading schedule for Sunday cycles A/B and weekday cycle II.
- The complete public-domain World English Bible, Catholic Edition (73 books), bundled offline.
- Local resolution of daily reading citations, including alternatives and cross-chapter ranges.
- Unit coverage for Rosary sequence integrity and weekly suggestions.
- Room-backed, versioned liturgical calendar schema with source provenance.
- Date navigation and explicit missing/out-of-coverage states on Today.
- Validated support for alternate celebrations and ordered reading references.
- FastAPI calendar service and draft importer for the English General Roman Calendar.
- Contextual Learn chat with day/mystery prompts and visible source links.
- A provider-neutral FastAPI AI endpoint with Exa retrieval that keeps service credentials out of the APK.

The bundled English prayer text is deliberately labelled as draft in the UI and must still be checked against the selected edition. The signed-off 2026 General Roman Calendar is installed into Room from an immutable offline asset on first launch. Daily references resolve locally against the public-domain World English Bible, Catholic Edition; the app clearly states that this is not official English Lectionary wording. 

## Build

Use Android Studio Quail or newer with JDK 17 and Android SDK 37 installed. Then sync and run the `app` configuration, or run:

```shell
./gradlew test
./gradlew assembleDebug
```

No API key, account or backend is needed for the current offline milestone.


## Next product gate
