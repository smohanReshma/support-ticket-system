#!/bin/bash
# Script to record prompts to the project's history
# This script is called by the Kiro hook on UserPromptSubmit

set -e

# Read session context from stdin
# The prompt is in the JSON input - we'll extract it using grep/sed
SESSION_CONTEXT=$(cat)

# Extract the prompt from the JSON
# Looking for "prompt": "..." pattern
PROMPT=$(echo "${SESSION_CONTEXT}" | grep -oP '"prompt":\s*"\K[^"]*'| head -1)

# Check if prompt is empty or just whitespace
if [ -z "${PROMPT}" ]; then
    # Empty prompt, skip recording
    exit 0
fi

# Create timestamp in ISO 8601 format
TIMESTAMP=$(date -u +"%Y-%m-%dT%H:%M:%SZ")

# Create a unique filename for this prompt record
PROMPT_FILENAME="${TIMESTAMP}.md"
HISTORY_DIR="/home/reshma/support-ticket-system/.specstory/history"

# Ensure the history directory exists
mkdir -p "${HISTORY_DIR}"

# Write the prompt record to the history directory as Markdown
{
    echo "---"
    echo "timestamp: \"${TIMESTAMP}\""
    echo "prompt: |"
    echo "${PROMPT}"
} > "${HISTORY_DIR}/${PROMPT_FILENAME}"

# Update the cumulative prompt history document
PROMPT_HISTORY_FILE="/home/reshma/support-ticket-system/docs/prompt-history.md"

# Read the existing content
if [ -f "${PROMPT_HISTORY_FILE}" ]; then
    EXISTING_CONTENT=$(cat "${PROMPT_HISTORY_FILE}")
else
    EXISTING_CONTENT=""
fi

# Check if this is the first prompt (file is just a placeholder)
if echo "${EXISTING_CONTENT}" | grep -q "Placeholder for tracking"; then
    # Replace placeholder with the first entry
    {
        echo "# Prompt History"
        echo ""
        echo "This file tracks all prompts given to the AI assistant during this project."
        echo ""
        echo "---"
        echo ""
        echo "## ${TIMESTAMP}"
        echo ""
        echo "\`\`\`prompt"
        echo "${PROMPT}"
        echo "\`\`\`"
    } > "${PROMPT_HISTORY_FILE}"
else
    # Prepend new entry to existing content
    {
        echo "# Prompt History"
        echo ""
        echo "This file tracks all prompts given to the AI assistant during this project."
        echo ""
        echo "---"
        echo ""
        echo "## ${TIMESTAMP}"
        echo ""
        echo "\`\`\`prompt"
        echo "${PROMPT}"
        echo "\`\`\`"
        echo ""
        echo "${EXISTING_CONTENT}"
    } > "${PROMPT_HISTORY_FILE}"
fi

# Exit successfully to allow the session to continue
exit 0
