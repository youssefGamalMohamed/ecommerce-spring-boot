#!/bin/bash
# Stop hook: appends the latest user+assistant exchange to CONTEXT.md as a full chat log.
# No truncation. No trimming. Every message is stored in full.
# Fires automatically after every Claude response.

set -euo pipefail

INPUT=$(cat)
TRANSCRIPT=$(echo "$INPUT" | jq -r '.transcript_path // empty')
PROJECT_DIR="${CLAUDE_PROJECT_DIR:-$(pwd)}"
CONTEXT_FILE="$PROJECT_DIR/CONTEXT.md"

[ -z "$TRANSCRIPT" ] && exit 0
[ ! -f "$TRANSCRIPT" ] && exit 0

# Count total turns already logged to find the new one(s)
# We track the last written turn index in a small state file
STATE_FILE="$PROJECT_DIR/.claude/.context-state"
LAST_INDEX=0
[ -f "$STATE_FILE" ] && LAST_INDEX=$(cat "$STATE_FILE")

# Read all turns from the transcript as a JSON array
TURNS=$(jq -c 'select(.type == "user" or .type == "assistant")' "$TRANSCRIPT" 2>/dev/null)

TOTAL=$(echo "$TURNS" | wc -l)

# Only write turns we haven't written yet
NEW_TURNS=$(echo "$TURNS" | tail -n +"$((LAST_INDEX + 1))")

[ -z "$NEW_TURNS" ] && exit 0

while IFS= read -r turn; do
    TYPE=$(echo "$turn" | jq -r '.type')
    TEXT=$(echo "$turn" | jq -r '.message.content[]? | select(.type == "text") | .text' 2>/dev/null)

    [ -z "$TEXT" ] && continue

    if [ "$TYPE" = "user" ]; then
        {
            echo ""
            echo "### 🧑 User — $(date '+%Y-%m-%d %H:%M')"
            echo ""
            echo "$TEXT"
            echo ""
        } >> "$CONTEXT_FILE"
    elif [ "$TYPE" = "assistant" ]; then
        {
            echo "### 🤖 Claude — $(date '+%Y-%m-%d %H:%M')"
            echo ""
            echo "$TEXT"
            echo ""
            echo "---"
        } >> "$CONTEXT_FILE"
    fi
done <<< "$NEW_TURNS"

# Save the new last index so next run only writes new turns
echo "$TOTAL" > "$STATE_FILE"

exit 0
