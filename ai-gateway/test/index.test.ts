import { describe, expect, it } from "vitest";
import { buildOpenAiBody, parseOpenAiResponse } from "../src/index";

describe("OpenAI request", () => {
  it("does not store responses and restricts web sources", () => {
    const body = buildOpenAiBody(
      {
        task: "explain_mystery",
        question: "Explain the Annunciation",
        context: { mystery: "The Annunciation", reference: "Luke 1:26-38" },
        history: [],
      },
      "test-model",
    );

    expect(body.store).toBe(false);
    expect(body.model).toBe("test-model");
    expect(body.tools).toEqual([
      {
        type: "web_search",
        search_context_size: "low",
        filters: { allowed_domains: ["vatican.va", "usccb.org"] },
      },
    ]);
  });
});

describe("OpenAI response", () => {
  it("extracts output text and clickable citations", () => {
    const result = parseOpenAiResponse({
      id: "resp_test",
      output: [
        {
          type: "message",
          content: [
            {
              type: "output_text",
              text: "A sourced explanation.",
              annotations: [
                {
                  type: "url_citation",
                  start_index: 2,
                  end_index: 10,
                  title: "Catechism of the Catholic Church",
                  url: "https://www.vatican.va/archive/ENG0015/_INDEX.HTM",
                },
              ],
            },
          ],
        },
      ],
    });

    expect(result.answer).toBe("A sourced explanation.");
    expect(result.citations[0]?.title).toBe("Catechism of the Catholic Church");
  });
});

