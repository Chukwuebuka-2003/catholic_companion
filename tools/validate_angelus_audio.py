#!/usr/bin/env python3
"""Validate generated Angelus narration before it replaces the bundled assets.

Checks every expected segment for codec, channel count, sample rate, plausible
duration and absence of long internal silence. Qwen3-TTS does not officially
support Latin, so an implausible Latin duration is the most likely failure and
must block the Android resource swap.
"""

from __future__ import annotations

import argparse
import json
import subprocess
import sys
from pathlib import Path

EXPECTED_CODEC = "aac"
EXPECTED_SAMPLE_RATE = 24000
EXPECTED_CHANNELS = 1

SILENCE_NOISE_DB = -45
SILENCE_MIN_SECONDS = 1.2

# Per-segment plausible duration bounds in seconds. Short responses must not run
# long: an overrunning short line is the signature of a model that kept talking.
DURATION_BOUNDS = {
    "angelus_en_declaration": (2.0, 8.0),
    "angelus_en_conceived": (1.5, 7.0),
    "angelus_en_hail_mary": (12.0, 32.0),
    "angelus_en_handmaid": (1.2, 6.0),
    "angelus_en_according": (1.5, 7.0),
    "angelus_en_word_flesh": (1.5, 7.0),
    "angelus_en_dwelt": (1.2, 6.0),
    "angelus_en_pray_for_us": (1.5, 7.0),
    "angelus_en_promises": (2.5, 10.0),
    "angelus_en_closing": (20.0, 50.0),
    "angelus_la_declaration": (1.5, 8.0),
    "angelus_la_conceived": (1.5, 7.0),
    "angelus_la_hail_mary": (12.0, 38.0),
    "angelus_la_handmaid": (1.0, 6.0),
    "angelus_la_according": (1.5, 7.0),
    "angelus_la_word_flesh": (1.5, 7.0),
    "angelus_la_dwelt": (1.0, 6.0),
    "angelus_la_pray_for_us": (1.5, 9.0),
    "angelus_la_promises": (2.0, 10.0),
    "angelus_la_closing": (20.0, 60.0),
}


def probe(path: Path) -> dict:
    result = subprocess.run(
        [
            "ffprobe",
            "-v",
            "error",
            "-select_streams",
            "a:0",
            "-show_entries",
            "stream=codec_name,channels,sample_rate",
            "-show_entries",
            "format=duration",
            "-of",
            "json",
            str(path),
        ],
        capture_output=True,
        text=True,
        check=True,
    )
    payload = json.loads(result.stdout)
    stream = payload["streams"][0]
    return {
        "codec": stream["codec_name"],
        "channels": int(stream["channels"]),
        "sample_rate": int(stream["sample_rate"]),
        "duration": float(payload["format"]["duration"]),
    }


def count_long_silences(path: Path) -> int:
    result = subprocess.run(
        [
            "ffmpeg",
            "-hide_banner",
            "-v",
            "error",
            "-i",
            str(path),
            "-af",
            f"silencedetect=noise={SILENCE_NOISE_DB}dB:d={SILENCE_MIN_SECONDS}",
            "-f",
            "null",
            "-",
        ],
        capture_output=True,
        text=True,
        check=True,
    )
    return result.stderr.count("silence_start")


def validate(directory: Path) -> int:
    failures = 0
    for name, (low, high) in DURATION_BOUNDS.items():
        path = directory / f"{name}.m4a"
        if not path.is_file():
            print(f"FAIL {name}: missing")
            failures += 1
            continue

        info = probe(path)
        problems = []
        if info["codec"] != EXPECTED_CODEC:
            problems.append(f"codec={info['codec']}")
        if info["channels"] != EXPECTED_CHANNELS:
            problems.append(f"channels={info['channels']}")
        if info["sample_rate"] != EXPECTED_SAMPLE_RATE:
            problems.append(f"sample_rate={info['sample_rate']}")
        if not low <= info["duration"] <= high:
            problems.append(
                f"duration={info['duration']:.2f}s outside [{low}, {high}]"
            )
        silences = count_long_silences(path)
        if silences:
            problems.append(f"long_silences={silences}")

        if problems:
            print(f"FAIL {name}: {', '.join(problems)}")
            failures += 1
        else:
            print(f"ok   {name}: {info['duration']:.2f}s")

    print()
    if failures:
        print(f"{failures} segment(s) failed validation; do not swap the assets")
    else:
        print(f"All {len(DURATION_BOUNDS)} segments passed validation")
    return failures


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("directory", type=Path)
    args = parser.parse_args()
    sys.exit(1 if validate(args.directory) else 0)


if __name__ == "__main__":
    main()
