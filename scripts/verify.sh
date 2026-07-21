#!/usr/bin/env bash
set -euo pipefail

echo "==> Running tests"
bash ./gradlew test
echo "==> Verification passed"
