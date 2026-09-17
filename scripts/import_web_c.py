#!/usr/bin/env python3
"""Build compact Android assets from the official eBible.org WEBC archives.

The verse-per-line archive is used without editing its verse text. The current
archive omits Genesis, so Genesis is recovered from the matching USFM archive.
"""

from __future__ import annotations

import argparse
import hashlib
import json
import re
import shutil
import zipfile
from pathlib import Path


BOOKS = {
    "GEN": "Genesis",
    "EXO": "Exodus",
    "LEV": "Leviticus",
    "NUM": "Numbers",
    "DEU": "Deuteronomy",
    "JOS": "Joshua",
    "JDG": "Judges",
    "RUT": "Ruth",
    "1SA": "1 Samuel",
    "2SA": "2 Samuel",
    "1KI": "1 Kings",
    "2KI": "2 Kings",
    "1CH": "1 Chronicles",
    "2CH": "2 Chronicles",
    "EZR": "Ezra",
    "NEH": "Nehemiah",
    "TOB": "Tobit",
    "JDT": "Judith",
    "ESG": "Esther (Greek)",
    "1MA": "1 Maccabees",
    "2MA": "2 Maccabees",
    "JOB": "Job",
    "PSA": "Psalms",
    "PRO": "Proverbs",
    "ECC": "Ecclesiastes",
    "SOL": "Song of Solomon",
    "WIS": "Wisdom",
    "SIR": "Sirach",
    "ISA": "Isaiah",
    "JER": "Jeremiah",
    "LAM": "Lamentations",
    "BAR": "Baruch",
    "EZE": "Ezekiel",
    "DNG": "Daniel (Greek)",
    "HOS": "Hosea",
    "JOE": "Joel",
    "AMO": "Amos",
    "OBA": "Obadiah",
    "JON": "Jonah",
    "MIC": "Micah",
    "NAH": "Nahum",
    "HAB": "Habakkuk",
    "ZEP": "Zephaniah",
    "HAG": "Haggai",
    "ZEC": "Zechariah",
    "MAL": "Malachi",
    "MAT": "Matthew",
    "MAR": "Mark",
    "LUK": "Luke",
    "JOH": "John",
    "ACT": "Acts",
    "ROM": "Romans",
    "1CO": "1 Corinthians",
    "2CO": "2 Corinthians",
    "GAL": "Galatians",
    "EPH": "Ephesians",
    "PHI": "Philippians",
    "COL": "Colossians",
    "1TH": "1 Thessalonians",
    "2TH": "2 Thessalonians",
    "1TI": "1 Timothy",
    "2TI": "2 Timothy",
    "TIT": "Titus",
    "PHM": "Philemon",
    "HEB": "Hebrews",
    "JAM": "James",
    "1PE": "1 Peter",
    "2PE": "2 Peter",
    "1JO": "1 John",
    "2JO": "2 John",
    "3JO": "3 John",
    "JUD": "Jude",
    "REV": "Revelation",
}

VPL_LINE = re.compile(r"^([1-3A-Z]{3}) (\d+):(\d+(?:-\d+)?) (.*)$")
VERSE_MARKER = re.compile(r"^\\v\s+(\d+(?:-\d+)?)\s*(.*)$")
CHAPTER_MARKER = re.compile(r"^\\c\s+(\d+)")


def sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as source:
        for chunk in iter(lambda: source.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def clean_usfm_verse(value: str) -> str:
    value = re.sub(r"\\f\s.*?\\f\*", "", value, flags=re.DOTALL)
    value = re.sub(r"\\x\s.*?\\x\*", "", value, flags=re.DOTALL)
    value = re.sub(
        r"\\\+?w\s+([^|\\]*?)(?:\|[^\\]*?)?\\\+?w\*",
        r"\1",
        value,
        flags=re.DOTALL,
    )
    value = re.sub(r"\\\+?[A-Za-z0-9-]+\*?", "", value)
    return re.sub(r"\s+", " ", value).strip()


def genesis_from_usfm(archive: zipfile.ZipFile) -> list[tuple[int, int, str]]:
    filename = next(name for name in archive.namelist() if "-GEN" in name and name.endswith(".usfm"))
    lines = archive.read(filename).decode("utf-8-sig").splitlines()
    chapter: int | None = None
    verse_number: str | None = None
    verse_parts: list[str] = []
    verses: list[tuple[int, int, str]] = []

    def flush() -> None:
        nonlocal verse_number, verse_parts
        if chapter is None or verse_number is None:
            return
        text = clean_usfm_verse(" ".join(verse_parts))
        if text and verse_number.isdigit():
            verses.append((chapter, int(verse_number), text))
        verse_number = None
        verse_parts = []

    for line in lines:
        chapter_match = CHAPTER_MARKER.match(line)
        if chapter_match:
            flush()
            chapter = int(chapter_match.group(1))
            continue
        verse_match = VERSE_MARKER.match(line)
        if verse_match:
            flush()
            verse_number = verse_match.group(1)
            verse_parts = [verse_match.group(2)]
            continue
        if verse_number is not None:
            verse_parts.append(line)
    flush()
    return verses


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--vpl-zip", required=True, type=Path)
    parser.add_argument("--usfm-zip", required=True, type=Path)
    parser.add_argument("--output", required=True, type=Path)
    args = parser.parse_args()

    by_book: dict[str, list[tuple[int, int, str]]] = {code: [] for code in BOOKS}
    with zipfile.ZipFile(args.vpl_zip) as archive:
        vpl_name = next(name for name in archive.namelist() if name.endswith("_vpl.txt"))
        for raw_line in archive.read(vpl_name).decode("utf-8-sig").splitlines():
            match = VPL_LINE.match(raw_line)
            if not match:
                raise ValueError(f"Unrecognised VPL line: {raw_line[:100]}")
            code, chapter, verse, text = match.groups()
            if code not in by_book:
                raise ValueError(f"Unexpected book code: {code}")
            if "-" not in verse and text:
                by_book[code].append((int(chapter), int(verse), text))

    if by_book["GEN"]:
        raise ValueError("The VPL archive now includes Genesis; remove the fallback before importing")
    with zipfile.ZipFile(args.usfm_zip) as archive:
        by_book["GEN"] = genesis_from_usfm(archive)

    missing = [code for code, verses in by_book.items() if not verses]
    if missing:
        raise ValueError(f"Missing Bible books: {missing}")

    if args.output.exists():
        shutil.rmtree(args.output)
    args.output.mkdir(parents=True)

    for code, verses in by_book.items():
        destination = args.output / f"{code.lower()}.vpl.txt"
        destination.write_text(
            "".join(f"{chapter}:{verse}\t{text}\n" for chapter, verse, text in verses),
            encoding="utf-8",
        )

    manifest = {
        "id": "eng-web-c",
        "title": "World English Bible, Catholic Edition",
        "edition": "2020 stable text edition",
        "language": "English",
        "sourceUrl": "https://ebible.org/eng-web-c/",
        "permission": "Public domain; keep the trademarked edition name only for unmodified text.",
        "vplArchiveSha256": sha256(args.vpl_zip),
        "usfmArchiveSha256": sha256(args.usfm_zip),
        "books": [
            {"code": code, "name": name, "verseCount": len(by_book[code])}
            for code, name in BOOKS.items()
        ],
        "verseCount": sum(len(verses) for verses in by_book.values()),
    }
    (args.output / "manifest.json").write_text(
        json.dumps(manifest, ensure_ascii=False, indent=2) + "\n",
        encoding="utf-8",
    )
    (args.output / "NOTICE.txt").write_text(
        "World English Bible, Catholic Edition\n"
        "2020 stable text edition\n\n"
        "The Bible text is in the public domain. World English Bible is a trademark "
        "of eBible.org and is used here only to identify an unmodified copy of that text.\n"
        "Source and terms: https://ebible.org/eng-web-c/copyright.htm\n",
        encoding="utf-8",
    )
    print(f"Wrote {len(BOOKS)} books and {manifest['verseCount']} verses to {args.output}")


if __name__ == "__main__":
    main()
