#!/usr/bin/env bash
# Decides what a push publishes, and writes the CurseForge changelog.
#
# Usage: release-meta.sh <branch> <changelog file>
#
# develop: publishes a beta when the push changed mod_version. Its changelog is patch-notes/<version>.md.
# main:    publishes a release when the version has no v<version> tag yet. Its changelog gathers the patch notes
#          of every version since the previous release.
#
# Outputs: version, type (beta or release), publish (true or false).
# Fails when a version to publish has no patch notes.
set -euo pipefail
source "$(dirname "$0")/lib.sh"

BRANCH=$1
CHANGELOG=$2

version=$(mod_version)
[ -n "$version" ] || error "Can't read mod_version in gradle.properties."
require_semver "$version"

output version "$version"

publish() {
  output publish true
  echo "Publishing $version as $1."
}

skip() {
  output publish false
  notice "$1"
}

mkdir -p "$(dirname "$CHANGELOG")"

case "$BRANCH" in
  develop)
    output type beta
    previous=$(mod_version HEAD^1)

    if [ "$version" = "$previous" ]; then
      skip "mod_version is still $version: nothing to publish."
      exit 0
    fi

    require_notes "$version"
    cp "$(notes_file "$version")" "$CHANGELOG"
    publish beta
    ;;
  main)
    output type release

    if is_released "$version"; then
      skip "$version is already released: bump mod_version to publish a new release."
      exit 0
    fi

    require_notes "$version"
    last=$(git tag -l 'v*' --sort=-v:refname | head -n 1 | sed 's/^v//')

    : > "$CHANGELOG"
    for note in $(ls "$NOTES_DIR"/*.md | sed "s|^$NOTES_DIR/||; s|\.md$||" | sort -V -r); do
      [[ "$note" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]] || continue
      if ! version_gt "$note" "$version" && { [ -z "$last" ] || version_gt "$note" "$last"; }; then
        { echo "## $note"; echo; cat "$(notes_file "$note")"; echo; } >> "$CHANGELOG"
      fi
    done

    publish "release (changes since ${last:-the first version})"
    ;;
  *)
    error "Nothing is published from $BRANCH."
    ;;
esac
