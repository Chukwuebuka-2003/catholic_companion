#!/usr/bin/env python3
"""Build the app's 2026 General Roman daily Mass reading schedule.

The input dataset follows the calendar used in the United States. This importer
uses its citation-only daily records, then applies the documented universal
General Roman date rules for Epiphany, Ascension and Corpus Christi. It also
removes two United States-only proper selections (Thanksgiving and Our Lady of
Guadalupe) so the generated bundle stays within the app's signed-off scope.

Only Scripture citations are imported. No copyrighted Lectionary or Bible
wording is copied by this script.
"""

from __future__ import annotations

import argparse
import datetime as dt
import json
import re
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
DEFAULT_CALENDAR = ROOT / "app/src/main/assets/calendars/general-roman-2026.json"
CONTENT_VERSION = "grc-en-2026-lectionary-a-ii-973e9864-v1"
SOURCE_TITLE = "Catholic Readings API 2026 citation dataset"
SOURCE_COMMIT_URL = (
    "https://github.com/cpbjr/catholic-readings-api/"
    "tree/973e9864eb0f15accadfc48750f5b243a95b7a2c/readings/2026"
)


# These are the dates on which the United States calendar dataset differs from
# the universal General Roman schedule used by this app. Sources for each
# override are recorded on the generated reading records.
GENERAL_ROMAN_OVERRIDES = {
    "2026-01-04": {
        "source": "https://www.liturgyoffice.org/Calendar/Sunday/ChristmasSunday.shtml",
        "readings": {
            "firstReading": "Sirach 24:1-2, 8-12",
            "psalm": "Psalm 147:12-13, 14-15, 19-20",
            "secondReading": "Ephesians 1:3-6, 15-18",
            "gospel": "John 1:1-18|John 1:1-5, 9-14",
        },
    },
    "2026-01-05": {
        "source": "https://www.liturgyoffice.org/Calendar/Weekday/Christmas.shtml",
        "readings": {
            "firstReading": "1 John 3:11-21",
            "psalm": "Psalm 100:1b-2, 3, 4, 5",
            "gospel": "John 1:43-51",
        },
    },
    "2026-01-06": {
        "source": "https://www.liturgyoffice.org/Calendar/Sunday/ChristmasSunday.shtml",
        "readings": {
            "firstReading": "Isaiah 60:1-6",
            "psalm": "Psalm 72:1-2, 7-8, 10-11, 12-13",
            "secondReading": "Ephesians 3:2-3a, 5-6",
            "gospel": "Matthew 2:1-12",
        },
    },
    "2026-05-17": {
        "source": "https://www.liturgyoffice.org/Calendar/Sunday/EasterSunday.shtml",
        "readings": {
            "firstReading": "Acts 1:12-14",
            "psalm": "Psalm 27:1, 4, 7-8",
            "secondReading": "1 Peter 4:13-16",
            "gospel": "John 17:1-11a",
        },
    },
    "2026-06-04": {
        "source": "https://www.liturgyoffice.org/Calendar/Sunday/OT2Solemnities.shtml",
        "readings": {
            "firstReading": "Deuteronomy 8:2-3, 14b-16a",
            "psalm": "Psalm 147:12-13, 14-15, 19-20",
            "secondReading": "1 Corinthians 10:16-17",
            "gospel": "John 6:51-58",
        },
    },
    "2026-06-07": {
        "source": "https://www.liturgyoffice.org.uk/Calendar/Sunday/OT3Sunday.shtml",
        "readings": {
            "firstReading": "Hosea 6:3-6",
            "psalm": "Psalm 50:1, 8, 12-15",
            "secondReading": "Romans 4:18-25",
            "gospel": "Matthew 9:9-13",
        },
    },
    "2026-11-26": {
        "source": "https://bible.usccb.org/bible/readings/112626-Ordinary",
        "readings": {
            "firstReading": "Revelation 18:1-2, 21-23; 19:1-3, 9a",
            "psalm": "Psalm 100:1b-2, 3, 4, 5",
            "gospel": "Luke 21:20-28",
        },
    },
    "2026-12-12": {
        "source": "https://bible.usccb.org/bible/readings/121127.cfm",
        "readings": {
            "firstReading": "Sirach 48:1-4, 9-11",
            "psalm": "Psalm 80:2ac, 3b, 15-16, 18-19",
            "gospel": "Matthew 17:9a, 10-13",
        },
    },
}


READING_FIELDS = (
    ("firstReading", "First reading"),
    ("psalm", "Responsorial psalm"),
    ("secondReading", "Second reading"),
    ("gospel", "Gospel"),
)

# NAB/USCCB numbers Psalm superscriptions as one or two verses in these Psalms.
# WEBC keeps the superscription inside its first verse without incrementing the
# poem's verse numbers. Values are therefore subtracted from imported verse
# references so the displayed WEBC words match the appointed Psalm portions.
# The offsets are derived from the open WLC/OSHB Masoretic verse map versus the
# WEBC VPL chapter counts.
PSALM_VERSE_OFFSETS = {
    **{number: 1 for number in (
        3, 4, 5, 6, 7, 8, 9, 12, 18, 19, 20, 21, 22, 30, 31, 34, 36, 38,
        39, 40, 41, 42, 44, 45, 46, 47, 48, 49, 53, 55, 56, 57, 58, 59, 61,
        62, 63, 64, 65, 67, 68, 69, 70, 75, 76, 77, 80, 81, 83, 84, 85, 88,
        89, 92, 102, 108, 140, 142,
    )},
    **{number: 2 for number in (51, 52, 54, 60)},
}


def normalize_psalm_versification(value: str) -> str:
    match = re.fullmatch(r"Psalm\s+(\d+)\s*:\s*(.+)", value, flags=re.IGNORECASE)
    if not match:
        return value
    psalm = int(match.group(1))
    offset = PSALM_VERSE_OFFSETS.get(psalm, 0)
    if offset == 0:
        return value

    def shift_verse(token: re.Match[str]) -> str:
        verse = int(token.group(1))
        shifted = verse - offset
        if shifted < 1:
            raise SystemExit(f"Cannot map Psalm {psalm} verse {verse} to WEBC")
        return f"{shifted}{token.group(2)}"

    body = re.sub(r"(\d+)([a-e]*)", shift_verse, match.group(2), flags=re.IGNORECASE)
    return f"Psalm {psalm}:{body}"


def normalize_citation(citation: str) -> str:
    value = " ".join(citation.replace("\xa0", " ").split())
    value = value.replace("–", "-").replace("—", "-").rstrip(".")
    value = value.replace("Phiippians", "Philippians")
    value = re.sub(r"\s+and\s+", ", ", value, flags=re.IGNORECASE)

    # Expand abbreviated alternative forms so every option can be parsed on its
    # own by Android (for example, "Luke 2:22-40 or 2:22-32").
    alternative = re.fullmatch(
        r"(.+?)\s+(\d+:[^|]+?)\s+or\s+(\d+:[^|]+)", value, flags=re.IGNORECASE
    )
    if alternative:
        book, first, second = alternative.groups()
        value = f"{book} {first}|{book} {second}"

    # The US Lectionary uses the Vulgate lettered additions to Esther. The
    # bundled Greek Esther integrates the same passage into chapter 4.
    if value == "Esther C:12, 14-16, 23-25":
        value = "Esther 4:29, 31-33, 40-42"
    if value == "Romans 16:25-27":
        value = "Romans 14:24-26"
    if value == "Malachi 3:1-4, 23-24":
        value = "Malachi 3:1-4; 4:5-6"
    return normalize_psalm_versification(value)


def expected_dates() -> list[str]:
    first = dt.date(2026, 1, 1)
    return [(first + dt.timedelta(days=offset)).isoformat() for offset in range(365)]


def load_source(source_dir: Path) -> dict[str, dict]:
    records: dict[str, dict] = {}
    for date in expected_dates():
        path = source_dir / f"{date[5:]}.json"
        if not path.is_file():
            raise SystemExit(f"Missing source record: {path}")
        record = json.loads(path.read_text())
        if record.get("date") != date:
            raise SystemExit(f"Source date mismatch in {path}: {record.get('date')!r}")
        readings = record.get("readings", {})
        for required in ("firstReading", "psalm", "gospel"):
            if not readings.get(required):
                raise SystemExit(f"{date} is missing {required}")
        records[date] = record
    return records


def build_bundle(calendar_path: Path, source_dir: Path) -> dict:
    bundle = json.loads(calendar_path.read_text())
    records = load_source(source_dir)
    primary_by_date = {
        celebration["date"]: celebration
        for celebration in bundle["celebrations"]
        if celebration["isPrimary"]
    }
    wanted_dates = expected_dates()
    if sorted(primary_by_date) != wanted_dates:
        missing = sorted(set(wanted_dates) - set(primary_by_date))
        extra = sorted(set(primary_by_date) - set(wanted_dates))
        raise SystemExit(f"Primary celebration coverage mismatch; missing={missing}, extra={extra}")

    generated = []
    for date in wanted_dates:
        source_record = records[date]
        source_url = source_record["apiEndpoint"]
        source_title = SOURCE_TITLE
        readings = source_record["readings"]
        if date in GENERAL_ROMAN_OVERRIDES:
            override = GENERAL_ROMAN_OVERRIDES[date]
            source_url = override["source"]
            source_title = "General Roman Lectionary reference override"
            readings = override["readings"]

        celebration_id = primary_by_date[date]["id"]
        order_index = 0
        for field, label in READING_FIELDS:
            citation = readings.get(field)
            if not citation:
                continue
            generated.append(
                {
                    "id": f"{celebration_id}:lectionary:{order_index}",
                    "celebrationId": celebration_id,
                    "orderIndex": order_index,
                    "label": label,
                    "citation": normalize_citation(citation),
                    "permittedText": None,
                    "sourceDocumentTitle": source_title,
                    "sourceUrl": source_url,
                }
            )
            order_index += 1

    bundle["readings"] = generated
    bundle["scope"].update(
        {
            "sourceName": "LitCal calendar + Catholic Readings API citation schedule",
            "sourceUrl": SOURCE_COMMIT_URL,
            "permissionStatus": (
                "Project-owner calendar accuracy sign-off. Daily Mass Scripture citations are "
                "normalized to the universal General Roman Calendar; bundled public-domain WEBC "
                "wording is not represented as an official English Lectionary translation."
            ),
            "contentVersion": CONTENT_VERSION,
            "reviewedAt": "2026-09-13",
        }
    )
    for day in bundle["days"]:
        day["contentReleaseId"] = CONTENT_VERSION
    validate_generated(bundle)
    return bundle


def validate_generated(bundle: dict) -> None:
    celebrations = {item["id"]: item for item in bundle["celebrations"]}
    grouped: dict[str, list[dict]] = {}
    for reading in bundle["readings"]:
        grouped.setdefault(reading["celebrationId"], []).append(reading)
    problems = []
    for date in expected_dates():
        primary = next(
            item
            for item in celebrations.values()
            if item["date"] == date and item["isPrimary"]
        )
        day_readings = sorted(grouped.get(primary["id"], []), key=lambda item: item["orderIndex"])
        labels = {item["label"] for item in day_readings}
        required = {"First reading", "Responsorial psalm", "Gospel"}
        if not required.issubset(labels):
            problems.append(f"{date}: missing {sorted(required - labels)}")
        if [item["orderIndex"] for item in day_readings] != list(range(len(day_readings))):
            problems.append(f"{date}: reading order is not contiguous")
        if any(not item["citation"].strip() for item in day_readings):
            problems.append(f"{date}: blank citation")
    if problems:
        raise SystemExit("Generated schedule is invalid:\n" + "\n".join(problems))


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "source_dir",
        type=Path,
        help="Path to catholic-readings-api/readings/2026 at commit 973e9864",
    )
    parser.add_argument("--calendar", type=Path, default=DEFAULT_CALENDAR)
    parser.add_argument("--output", type=Path, default=DEFAULT_CALENDAR)
    args = parser.parse_args()

    bundle = build_bundle(args.calendar, args.source_dir)
    args.output.write_text(json.dumps(bundle, indent=2, ensure_ascii=False) + "\n")
    second_readings = sum(item["label"] == "Second reading" for item in bundle["readings"])
    print(
        f"Wrote {len(bundle['readings'])} references for 365 days "
        f"({second_readings} second readings) to {args.output}"
    )


if __name__ == "__main__":
    main()
