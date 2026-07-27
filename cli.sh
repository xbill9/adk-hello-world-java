#!/usr/bin/env bash

set -euo pipefail
SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"
source "$SCRIPT_DIR/set_env.sh"

mvn compile exec:java \
  -Dexec.mainClass="agents.multitool.MultiToolAgent" \
  -Dexec.classpathScope="compile"
