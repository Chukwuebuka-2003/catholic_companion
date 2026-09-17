#!/usr/bin/env python3
"""Generate the bundled Angelus narration with Qwen3-TTS 1.7B.

Qwen3-TTS does not officially support Latin. The Latin text is generated using
its Italian language mode and must be reviewed for ecclesiastical pronunciation.
The model and Python environment are build-time tools; only compressed M4A files
belong in the Android application.
"""

from __future__ import annotations

import argparse
import shutil
import subprocess
import tempfile
from pathlib import Path

import soundfile as sf
import torch
from qwen_tts import Qwen3TTSModel


MODEL_ID = "Qwen/Qwen3-TTS-12Hz-1.7B-CustomVoice"
DEFAULT_OUTPUT = Path(__file__).resolve().parents[1] / "app/src/main/res/raw"
DEFAULT_INSTRUCT = (
    "Speak slowly and reverently in a calm, warm, prayerful tone, with clear "
    "diction and natural pauses. Do not sing or dramatize."
)

ENGLISH_SEGMENTS = {
    "angelus_en_declaration": "The Angel of the Lord declared unto Mary.",
    "angelus_en_conceived": "And she conceived of the Holy Spirit.",
    "angelus_en_hail_mary": (
        "Hail, Mary, full of grace, the Lord is with thee. Blessed art thou among women, "
        "and blessed is the fruit of thy womb, Jesus. Holy Mary, Mother of God, pray for "
        "us sinners, now and at the hour of our death. Amen."
    ),
    "angelus_en_handmaid": "Behold the handmaid of the Lord.",
    "angelus_en_according": "Be it done unto me according to thy word.",
    "angelus_en_word_flesh": "And the Word was made flesh.",
    "angelus_en_dwelt": "And dwelt among us.",
    "angelus_en_pray_for_us": "Pray for us, O holy Mother of God.",
    "angelus_en_promises": "That we may be made worthy of the promises of Christ.",
    "angelus_en_closing": (
        "Let us pray. Pour forth, we beseech thee, O Lord, thy grace into our hearts; "
        "that we, to whom the Incarnation of Christ, thy Son, was made known by the "
        "message of an angel, may by his Passion and Cross be brought to the glory of "
        "his Resurrection. Through the same Christ, our Lord. Amen."
    ),
}

LATIN_SEGMENTS = {
    "angelus_la_declaration": "Ángelus Dómini nuntiávit Maríae.",
    "angelus_la_conceived": "Et concépit de Spíritu Sancto.",
    "angelus_la_hail_mary": (
        "Ave, María, grátia plena, Dóminus tecum. Benedícta tu in muliéribus, et "
        "benedíctus fructus ventris tui, Iesus. Sancta María, Mater Dei, ora pro nobis "
        "peccatóribus, nunc et in hora mortis nostrae. Amen."
    ),
    "angelus_la_handmaid": "Ecce ancílla Dómini.",
    "angelus_la_according": "Fiat mihi secúndum verbum tuum.",
    "angelus_la_word_flesh": "Et Verbum caro factum est.",
    "angelus_la_dwelt": "Et habitávit in nobis.",
    "angelus_la_pray_for_us": "Ora pro nobis, sancta Dei Génetrix.",
    "angelus_la_promises": "Ut digni efficiámur promissiónibus Christi.",
    "angelus_la_closing": (
        "Orémus. Grátiam tuam, quáesumus, Dómine, méntibus nostris infúnde; ut qui, "
        "Ángelo nuntiánte, Christi Fílii tui incarnatiónem cognóvimus, per passiónem "
        "eius et crucem ad resurrectiónis glóriam perducámur. Per eúndem Christum "
        "Dóminum nostrum. Amen."
    ),
}


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser()
    parser.add_argument("--output", type=Path, default=DEFAULT_OUTPUT)
    parser.add_argument("--speaker", default="serena")
    parser.add_argument(
        "--instruct",
        default=DEFAULT_INSTRUCT,
        help="Voice-style instruction supported by the 1.7B CustomVoice model.",
    )
    parser.add_argument(
        "--language",
        choices=("english", "latin", "all"),
        default="all",
    )
    parser.add_argument(
        "--device",
        choices=("auto", "cpu", "mps", "cuda"),
        default="auto",
    )
    parser.add_argument(
        "--smoke-test",
        action="store_true",
        help="Generate one short English file to validate the runtime.",
    )
    return parser.parse_args()


def choose_device(requested: str) -> str:
    if requested != "auto":
        return requested
    if torch.cuda.is_available():
        return "cuda:0"
    if torch.backends.mps.is_available():
        return "mps"
    return "cpu"


def encode_m4a(wav_path: Path, output_path: Path) -> None:
    ffmpeg = shutil.which("ffmpeg")
    if ffmpeg is None:
        raise RuntimeError("ffmpeg is required to encode the Android audio assets")
    subprocess.run(
        [
            ffmpeg,
            "-hide_banner",
            "-loglevel",
            "error",
            "-y",
            "-i",
            str(wav_path),
            "-ac",
            "1",
            "-ar",
            "24000",
            "-af",
            "loudnorm=I=-20:TP=-2:LRA=7",
            "-c:a",
            "aac",
            "-b:a",
            "64k",
            "-movflags",
            "+faststart",
            str(output_path),
        ],
        check=True,
    )


def generate_group(
    model: Qwen3TTSModel,
    segments: dict[str, str],
    model_language: str,
    speaker: str,
    instruct: str,
    output: Path,
) -> None:
    for index, (name, text) in enumerate(segments.items(), start=1):
        target = output / f"{name}.m4a"
        print(f"[{index}/{len(segments)}] {target.name}", flush=True)
        torch.manual_seed(2026 + index)
        wavs, sample_rate = model.generate_custom_voice(
            text=text,
            language=model_language,
            speaker=speaker,
            instruct=instruct,
            do_sample=True,
        )
        with tempfile.NamedTemporaryFile(suffix=".wav") as temporary:
            sf.write(temporary.name, wavs[0], sample_rate)
            encode_m4a(Path(temporary.name), target)


def main() -> None:
    args = parse_args()
    args.output.mkdir(parents=True, exist_ok=True)
    device = choose_device(args.device)
    print(f"Loading {MODEL_ID} on {device}", flush=True)
    model_dtype = torch.float16 if device.startswith("cuda") else torch.float32
    model = Qwen3TTSModel.from_pretrained(
        MODEL_ID,
        device_map=device,
        dtype=model_dtype,
    )

    if args.smoke_test:
        generate_group(
            model,
            {"angelus_qwen_smoke_test": ENGLISH_SEGMENTS["angelus_en_declaration"]},
            "English",
            args.speaker,
            args.instruct,
            args.output,
        )
        return

    if args.language in ("english", "all"):
        generate_group(
            model,
            ENGLISH_SEGMENTS,
            "English",
            args.speaker,
            args.instruct,
            args.output,
        )
    if args.language in ("latin", "all"):
        generate_group(
            model,
            LATIN_SEGMENTS,
            "Italian",
            args.speaker,
            args.instruct,
            args.output,
        )


if __name__ == "__main__":
    main()
