# Catholic Android App — Product and Development Plan

## 1. Product goal

Build a daily Catholic companion that helps people understand the day's liturgy, pray the Rosary, and ask grounded follow-up questions. The three primary destinations are **Today · Rosary · Learn**. Put settings behind a secondary menu.

The core daily journey: open Today → read the celebration and readings → explore a short explanation → optionally pray the Rosary → return another day. Avoid competitive prayer scores or guilt-based reminders.

## 2. Planning assumptions

- Solo developer with Python/FastAPI and AI engineering experience; native Android experience is not assumed.
- Android first, English first, Roman Rite in its current ordinary form.
- Start with one verified calendar scope. Nigeria is a proposed initial audience, not a confirmed requirement. Confirm national and diocesan sources before claiming local accuracy. If only the General Roman Calendar is supported, label that limitation prominently.
- Basic reading and prayer features work without an account. AI requires connectivity; saved explanations remain readable offline.
- Initial estimate: 12–16 focused development weeks after essential content access is resolved. Part-time work, learning Android, licensing, and reviewer availability can extend this substantially.
- This plan proposes architecture and scope; it does not establish distribution rights, ecclesiastical endorsement, or an available Nigerian calendar provider.

## 3. First-release scope

| Area | Include | Acceptance condition |
|---|---|---|
| Today | Date, supported calendar, celebration, rank, season, colour, reading references, and permitted reading text | Correct supported date and calendar shown; data provenance and coverage visible |
| Calendar | Date navigation, local-date handling, supported observances and published exceptions | Compare the entire supported launch year against a verified reference; resolve every discrepancy |
| Rosary | Reviewed prayers, all four sets of mysteries, suggested set with manual override, bead progress, optional haptics, pause/resume | Complete a five-decade Rosary offline and resume after app termination |
| Learn | Explain today's liturgy, explain a mystery, and contextual follow-up questions | Substantive sourced claims link to passages; unsupported questions receive an explicit limitation |
| Reflection | Short optional AI-generated reflection, separately labelled | Generated material never appears as Scripture or an official prayer text |
| Offline | Bundled Rosary and downloaded calendar/readings within permitted rights | Cached content works in airplane mode; unavailable dates are clearly marked |
| Preferences | Text size, dark mode, calendar selection, optional local reminders | Core navigation works with TalkBack and enlarged text |
| Content operations | Import, review, version, publish, withdraw, and correct content | A bad content release can be rolled back without an app release |

Defer full voice conversations, recorded audio production, social prayer groups, parish management, Mass-location search, multiple rites, broad multilingual support, subscriptions, and account sync. Add downloadable Rosary audio after the text experience is reliable and recording rights are settled.

## 4. Screen behaviour

**Today:** show the celebration and calendar scope first, then readings, an “Explain today's readings” action, and a Rosary shortcut. Distinguish source text from generated explanation. Cached content opens immediately; refresh occurs in the background.

**Rosary:** choose mysteries and optional intention → opening prayers → five decades → closing prayers. Offer clear next/back controls, a progress indicator, haptics, and resume. Keep optional meditations before a decade, off by default. Retain intentions only when the user explicitly chooses to save them.

**Learn:** launch with contextual prompts such as “Explain this Gospel” or “Explain the third mystery.” Show an answer with selectable citations. Open the source excerpt and document reference from each citation. Preserve the selected date and mystery across follow-up questions.

**Settings:** select supported calendar, manage downloads, reminders, text size, data deletion, and privacy choices. Do not require GPS to choose a region.

## 5. Content and calendar strategy

Create a source register before ingestion. For every text or dataset record its publisher, edition, language, scope, source URL, permission status, allowed uses, version, reviewer, and review date. Public access is not evidence of permission to redistribute, embed, generate audio, or send content to an AI provider.

Identify the appropriate liturgical reference for the launch region and year. Support movable celebrations, precedence, transfers, optional observances, and local overrides only to the extent validated. Model alternative celebrations and reading selections explicitly rather than forcing every date into one universal answer. Scope the first release to the standard daily Mass selection; label any unsupported vigil or ritual-Mass variants.

Use deterministic calendar resolution and publish versioned daily records. Do not let an LLM invent dates, readings, precedence, or prayer wording. A verified precomputed calendar can ship before a comprehensive rules engine; a small importer and explicit reviewed overrides are a reasonable starting point.

If reading-text permissions are unresolved, offer permitted references and publisher links while resolving access. Do not present a different Bible translation as the official local lectionary text. Recruit a priest, catechist, or qualified reviewer for a bounded review of prayer content, liturgical fixtures, and AI evaluation examples; review does not itself imply formal Church approval.

## 6. Proposed architecture

| Component | Proposed implementation | Responsibility |
|---|---|---|
| Android | Kotlin and Jetpack Compose | Screens, accessible prayer interaction, lifecycle handling |
| Local storage | Room for structured content; DataStore for preferences | Offline content, progress, download metadata |
| Background work | WorkManager | Content refresh and retry when conditions permit |
| Backend | FastAPI modular application | Calendar/content APIs, retrieval, AI orchestration |
| Database | PostgreSQL; evaluate pgvector for the initial corpus | Content metadata, versioning, text and semantic retrieval |
| Files | Object storage | Permitted downloadable bundles and later audio |
| Jobs | A simple worker when ingestion requires it | Import, validation, embeddings, review queues |

Keep one backend and one database initially. Introduce a dedicated vector service only if retrieval requirements justify it. Keep provider API keys on the server. Guest AI requests still need quotas, abuse controls, and request-size limits; accounts can be added later if necessary.

Android reads from local storage and updates it from the network, consistent with the official [offline-first guidance](https://developer.android.com/topic/architecture/data-layer/offline-first). The proposed UI organisation follows Android's [architecture guidance](https://developer.android.com/topic/architecture).

Core records: CalendarScope, LiturgicalDay, CelebrationOption, ReadingReference, SourceDocument, SourcePassage, PrayerText, Mystery, ContentRelease, and ReviewedExplanation. Keep preferences and RosarySession local in the first release. Avoid server-side intention and prayer-history records by default.

Initial API boundaries:

- GET /v1/calendar-scopes — supported scopes and coverage.
- GET /v1/liturgy/day?date=...&calendar_id=... — resolved day with source/version metadata.
- GET /v1/rosary — reviewed prayer and mystery content.
- GET /v1/content/manifest — downloadable releases and checksums.
- POST /v1/ai/explain — bounded explanation using a day or mystery identifier.
- POST /v1/ai/ask — contextual follow-up with citations.

## 7. AI design

Pipeline: validate selected context → fetch deterministic day/mystery data → retrieve permitted passages → generate a bounded answer → verify source identifiers and quoted spans → return answer, citations, and limitations.

Start with three tasks: explain readings, explain a liturgical term or celebration, and explain a Rosary mystery. Introduce broader Catholic Q&A only after these pass evaluation.

Preserve paragraph and verse identifiers during ingestion. Attach source type and authority metadata, distinguishing Scripture, teaching documents, and commentary. A citation being present does not prove that it supports the claim: combine automated citation checks with human review of theological fidelity and entailment.

Treat retrieved text as evidence, never as instructions. Explicitly label generated reflections. Do not speak as God or claim sacramental authority. Let users flag an answer for correction.

Cache only reusable, non-personal explanations using calendar, date/mystery, language, corpus version, prompt version, and model version. Never reuse personal intentions across users. Review daily explanations before broad reuse where feasible. Set request limits and a configurable monthly spending ceiling; choose a model after evaluating quality, latency, and measured cost.

## 8. Development sequence

| Phase | Estimate | Deliverable and exit gate |
|---|---|---|
| 1. Validate scope and sources | 1–2 weeks | Interview 5–8 intended users; choose calendar; source/permission register; screen sketches; reviewer identified |
| 2. Android foundation and Rosary | 2–3 weeks | Navigation, local content, full Rosary, progress persistence, font/accessibility support; offline walkthrough passes |
| 3. Liturgical companion | 2–3 weeks | Importer, versioned day records, Today screen, downloads; supported-year calendar comparison passes |
| 4. Grounded AI | 3 weeks | Small curated corpus, retrieval, contextual explanations, citations, failure handling, quotas; evaluation gate passes |
| 5. Private beta | 2–3 weeks | 15–30 consenting testers; fix content, lifecycle, accessibility, and retrieval issues; measure real operating cost |
| 6. Release preparation | 1–2 weeks | Current store requirements checked, privacy disclosures, assets, release build, content correction process, staged release plan |

The first usable milestone is an offline Rosary with the three-tab shell. The beta milestone must also contain Today and the three bounded AI tasks so the app delivers the intended AI companion experience.

## 9. Verification and release gates

- Calendar: no unresolved mismatches against the reviewed reference for the supported launch year; test year boundaries, leap dates, transferred feasts, time-zone changes, and unavailable coverage.
- Prayer: every shipped prayer and mystery reviewed against the selected edition; no missing, repeated, or skipped sequence steps.
- Android: airplane-mode use, process death, download interruption, large text, TalkBack, and progress recovery pass on representative lower-spec hardware.
- AI: build roughly 100 reviewer-approved cases spanning readings, mysteries, factual questions, unsupported requests, misleading premises, and prompt injection. Proposed beta gate: every citation resolves, at least 95% of sampled factual claims are supported, and no unresolved serious doctrinal misrepresentation. These are targets, not measured results.
- Privacy: exclude intentions and full private prompts from routine logs; verify local deletion and backup behaviour. Sending an intention to the model requires a clear user action explaining that it leaves the device.
- Operations: upstream/model failure leaves the prayer and cached reading experience usable; rollback of a content release works; budget limits are enforced.

## 10. Beta learning and ongoing costs

Measure activation, voluntary seven-day return, user-rated helpfulness, citation defects, crashes, slow requests, and AI cost per active user. A completed prayer session is optional product feedback, not a measure of faith. Collect minimal aggregate events; do not collect intention text for analytics.

Budget categories: Android/store setup, backend and database, storage/egress, model usage, embeddings, content permissions, reviewer time, and later audio. Estimate monthly AI spend from active users × AI requests per user × measured average request cost, adjusted for cache hits. Verify actual supplier pricing when selecting providers.

Before public release, check current Google Play requirements and relevant content/data obligations. Do not add monetisation until beta evidence shows what users value. A possible later model keeps basic prayers and permitted daily content free while charging for optional costly AI/audio features; this remains a hypothesis.

## 11. Immediate first-week backlog

1. Confirm launch calendar and English text editions; start the source register.
2. Sketch Today, Rosary, and Learn and test the flow with five Catholics.
3. Ask a potential content reviewer about a small, clearly defined review commitment.
4. Create the Android shell and FastAPI project once implementation begins.
5. Build one full offline five-decade Rosary using reviewed content.
6. Assemble sample day records and 20 AI evaluation questions; expand to the launch-year dataset and full evaluation set in later phases.

The first decision to settle is calendar coverage and usable content. It determines both the accuracy of Today and the evidence available to the AI.
