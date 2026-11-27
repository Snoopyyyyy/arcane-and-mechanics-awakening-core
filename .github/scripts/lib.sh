# Helpers shared by the CI scripts. Run from the repository root.

NOTES_DIR=patch-notes

error() {
  echo "::error::$1" >&2
  exit 1
}

notice() {
  echo "::notice::$1"
}

# Writes a key=value pair to the step outputs, or to stdout outside GitHub Actions.
output() {
  echo "$1=$2" >> "${GITHUB_OUTPUT:-/dev/stdout}"
}

# Prints mod_version from gradle.properties: at a git revision when one is given, else from the working tree.
# Prints nothing when the revision or the file doesn't exist.
mod_version() {
  local content
  if [ $# -gt 0 ]; then
    content=$(git show "$1:gradle.properties" 2>/dev/null) || return 0
  else
    content=$(cat gradle.properties)
  fi
  printf '%s\n' "$content" | sed -n 's/^mod_version=//p' | tr -d '\r[:space:]'
}

# Fails unless the version has the MAJOR.MINOR.PATCH form.
require_semver() {
  [[ "$1" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]] || error "mod_version '$1' must have the form MAJOR.MINOR.PATCH."
}

notes_file() {
  echo "$NOTES_DIR/$1.md"
}

# Fails unless the patch notes of the version exist and are not empty.
require_notes() {
  local file
  file=$(notes_file "$1")
  [ -f "$file" ] || error "Missing patch notes for $1: add $file."
  grep -q '[^[:space:]]' "$file" || error "Patch notes $file are empty."
}

# True when version $1 is strictly greater than version $2.
version_gt() {
  [ "$1" != "$2" ] && [ "$(printf '%s\n%s\n' "$1" "$2" | sort -V | tail -n 1)" = "$1" ]
}

# True when version $1 has been released on main (tag v<version>).
is_released() {
  git rev-parse -q --verify "refs/tags/v$1" >/dev/null
}
