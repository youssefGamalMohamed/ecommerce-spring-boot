#!/bin/bash
# Stop hook: auto-saves the last assistant response to CONTEXT.md
# Fires automatically after every Claude response - no manual action needed.

set -euo pipefail

INPUT=$(cat)
TRANSCRIPT=$(echo "$INPUT" | jq -r '.transcript_path // empty')
PROJECT_DIR="${CLAUDE_PROJECT_DIR:-$(pwd)}"
CONTEXT_FILE="$PROJECT_DIR/CONTEXT.md"

[ -z "$TRANSCRIPT" ] && exit 0
[ ! -f "$TRANSCRIPT" ] && exit 0

# Extract the last assistant text block from the transcript
LAST_ASSISTANT=$(jq -r '
  select(.type == "assistant") |
  .message.content[]? |
  select(.type == "text") |
  .text
' "$TRANSCRIPT" 2>/dev/null | tail -c 4000)

[ -z "$LAST_ASSISTANT" ] && exit 0

# Write a dated entry into CONTEXT.md
{
  echo ""
  echo "## $(date '+%Y-%m-%d %H:%M')"
  echo ""
  echo "$LAST_ASSISTANT"
  echo ""
  echo "---"
} >> "$CONTEXT_FILE"

# Keep the file lean — retain only the last 300 lines
if [ -f "$CONTEXT_FILE" ]; then
  LINES=$(wc -l < "$CONTEXT_FILE")
  if [ "$LINES" -gt 300 ]; then
    tail -n 300 "$CONTEXT_FILE" > "$CONTEXT_FILE.tmp" && mv "$CONTEXT_FILE.tmp" "$CONTEXT_FILE"
  fi
fi

exit 0
