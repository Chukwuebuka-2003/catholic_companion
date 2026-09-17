# Archived Cloudflare AI gateway prototype

The Android app now uses the FastAPI implementation in `backend/app/ai.py` at
the same `/v1/ai/ask` contract. This Worker prototype is retained only as a
reference and is not the supported deployment path.

A small Cloudflare Worker that protects the OpenAI key. It accepts bounded,
contextual Catholic-study questions, calls the OpenAI Responses API with web
search restricted to `vatican.va` and `usccb.org`, and returns answer text plus
clickable citation ranges.

It is separate from the calendar: the calendar and Rosary remain fully offline.

## Local verification

```shell
npm install
npm run types
npm run check
```

Create `ai-gateway/.dev.vars` for local development only:

```text
OPENAI_API_KEY=your-key
```

Never put that key in Android code, Gradle properties, source control, or
`wrangler.jsonc`.

## Deployment

After logging into Cloudflare, add the secret interactively and deploy:

```shell
npx wrangler secret put OPENAI_API_KEY
npm run deploy
```

Then build Android with the deployed HTTPS origin:

```shell
./gradlew -PAI_BASE_URL=https://YOUR-WORKER.workers.dev assembleDebug
```

The Worker applies six requests per minute per installation identifier. Also set
an account-level OpenAI project budget; the Worker rate limit is abuse resistance,
not exact billing enforcement.
