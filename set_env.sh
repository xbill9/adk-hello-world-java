#!/usr/bin/env bash

# Source this file from a launcher or an interactive shell.

PROJECT_FILE="$HOME/project_id.txt"
KEY_FILE="$HOME/gemini.key"
AUTH_MODE_FILE="$HOME/.adk-hello-world-java-auth"

if [[ -f "$AUTH_MODE_FILE" ]]; then
  ADK_AUTH_MODE="$(tr -d '\r\n' < "$AUTH_MODE_FILE")"
elif [[ -f "$KEY_FILE" ]]; then
  ADK_AUTH_MODE="api-key"
else
  ADK_AUTH_MODE="vertex-ai"
fi

export GOOGLE_CLOUD_LOCATION="${GOOGLE_CLOUD_LOCATION:-us-central1}"
export REGION="$GOOGLE_CLOUD_LOCATION"
export SERVICE_NAME="adk-hello-world-java"
export APP_NAME="adk-hello-world-java"
export AGENT_PATH="src/main/java/agents/multitool/MultiToolAgent.java"

if [[ "$ADK_AUTH_MODE" == "api-key" ]]; then
  if [[ ! -s "$KEY_FILE" ]]; then
    echo "Error: Gemini API key not found at $KEY_FILE. Run ./init.sh." >&2
    return 1
  fi
  export GOOGLE_API_KEY="$(tr -d '\r\n' < "$KEY_FILE")"
  export GOOGLE_GENAI_USE_VERTEXAI="FALSE"
  unset GOOGLE_CLOUD_PROJECT PROJECT_ID PROJECT_NUMBER SERVICE_ACCOUNT_NAME
  echo "Using Gemini API-key authentication."
elif [[ "$ADK_AUTH_MODE" == "vertex-ai" ]]; then
  if [[ ! -s "$PROJECT_FILE" ]]; then
    echo "Error: Project ID not found at $PROJECT_FILE. Run ./init.sh." >&2
    return 1
  fi
  command -v gcloud >/dev/null 2>&1 || {
    echo "Error: gcloud is required for Vertex AI authentication." >&2
    return 1
  }
  gcloud auth application-default print-access-token >/dev/null 2>&1 || {
    echo "Error: Application Default Credentials are unavailable. Run ./init.sh." >&2
    return 1
  }

  export PROJECT_ID="$(tr -d '\r\n' < "$PROJECT_FILE")"
  export GOOGLE_CLOUD_PROJECT="$PROJECT_ID"
  export GOOGLE_GENAI_USE_VERTEXAI="TRUE"
  unset GOOGLE_API_KEY
  gcloud config set project "$PROJECT_ID" --quiet
  echo "Using Vertex AI authentication for project $PROJECT_ID."
else
  echo "Error: Unknown authentication mode '$ADK_AUTH_MODE'. Run ./init.sh." >&2
  return 1
fi

echo "Environment configured for $APP_NAME in $GOOGLE_CLOUD_LOCATION."
