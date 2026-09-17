# Angelus audio provenance

The bundled Angelus narration is generated at build time with
`Qwen/Qwen3-TTS-12Hz-1.7B-CustomVoice`, using the English-native `serena` voice
and a fixed instruction for a calm, reverent, prayerful delivery.
The model is licensed under Apache-2.0. Model weights and the Python runtime are
not distributed in the Android application.

English is generated in the model's English mode. Qwen3-TTS does not officially
support Latin, so the Latin recording is generated in Italian mode as an
ecclesiastical-pronunciation approximation. A qualified reviewer should approve
the Latin recording before a public release.

Regenerate from the repository root with an isolated environment containing
Python 3.11 or 3.12 and FFmpeg:

```shell
python3 -m venv .venv-qwen-tts
.venv-qwen-tts/bin/python -m pip install -r tools/qwen-tts-requirements.txt
.venv-qwen-tts/bin/python tools/generate_angelus_audio.py
```

The script stores mono 64 kbps AAC/M4A segments in `app/src/main/res/raw`.
