#!/usr/bin/env bash

set -euo pipefail

PROJECT_FILE="$HOME/project_id.txt"
KEY_FILE="$HOME/gemini.key"
AUTH_MODE_FILE="$HOME/.adk-hello-world-java-auth"

echo "--- Authentication method ---"
read -r -p "Use a Gemini API key for local development? (y/n): " use_gemini_key

if [[ "$use_gemini_key" =~ ^[Yy]$ ]]; then
  read -r -s -p "Please enter your Gemini API key: " user_gemini_key
  echo
  if [[ -z "$user_gemini_key" ]]; then
    echo "Error: No Gemini API key was entered." >&2
    exit 1
  fi

  (umask 077 && printf '%s\n' "$user_gemini_key" > "$KEY_FILE")
  printf '%s\n' "api-key" > "$AUTH_MODE_FILE"
  echo "Saved the API key with user-only permissions."
else
  if [[ ! -f "$PROJECT_FILE" ]]; then
    read -r -p "Please enter your Google Cloud project ID: " user_project_id
    if [[ -z "$user_project_id" ]]; then
      echo "Error: No project ID was entered." >&2
      exit 1
    fi
    printf '%s\n' "$user_project_id" > "$PROJECT_FILE"
  fi

  command -v gcloud >/dev/null 2>&1 || {
    echo "Error: gcloud is required for Vertex AI authentication." >&2
    exit 1
  }
  gcloud auth list --filter=status:ACTIVE --format="value(account)" | grep -q . \
    || gcloud auth login
  gcloud auth application-default print-access-token >/dev/null 2>&1 \
    || gcloud auth application-default login
  gcloud config set project "$(tr -d '\r\n' < "$PROJECT_FILE")" --quiet
  printf '%s\n' "vertex-ai" > "$AUTH_MODE_FILE"
  echo "Configured Vertex AI with Application Default Credentials."
fi

echo "--- Setup complete ---"
