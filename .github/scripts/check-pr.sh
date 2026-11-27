#!/usr/bin/env bash
# Checks the version and patch notes of a pull request.
#
# Usage: check-pr.sh <base branch> <head branch>
#
# Into develop:
#   feature/*  bumps the minor version (1.2.3 -> 1.3.0)
#   fix/*      bumps the patch version (1.2.3 -> 1.2.4)
#   others     keep the version (nothing is published)
#   A new version needs its patch notes, patch-notes/<version>.md.
# Into main:
#   A version that is not released yet needs its patch notes.
set -euo pipefail
source "$(dirname "$0")/lib.sh"

BASE_BRANCH=$1
HEAD_BRANCH=$2

version=$(mod_version)
[ -n "$version" ] || error "Can't read mod_version in gradle.properties."
require_semver "$version"

case "$BASE_BRANCH" in
  develop)
    base_version=$(mod_version "origin/$BASE_BRANCH")
    [ -n "$base_version" ] || error "Can't read mod_version on origin/$BASE_BRANCH."
    require_semver "$base_version"
    IFS=. read -r major minor patch <<< "$base_version"

    case "$HEAD_BRANCH" in
      feature/*)
        expected="$major.$((minor + 1)).0"
        rule="a feature/ branch bumps the minor version"
        ;;
      fix/*)
        expected="$major.$minor.$((patch + 1))"
        rule="a fix/ branch bumps the patch version"
        ;;
      *)
        expected=$base_version
        rule="only feature/ and fix/ branches change the version"
        ;;
    esac

    if [ "$version" != "$expected" ]; then
      error "mod_version is $version but must be $expected: $rule (develop is at $base_version)."
    fi

    if [ "$version" != "$base_version" ]; then
      require_notes "$version"
      echo "Version $base_version -> $version, patch notes $(notes_file "$version") found."
    else
      echo "Version unchanged ($version): nothing will be published."
    fi
    ;;
  main)
    if is_released "$version"; then
      notice "$version is already released: merging into main will not publish anything."
    else
      require_notes "$version"
      echo "Release $version, patch notes $(notes_file "$version") found."
    fi
    ;;
  *)
    echo "No version rule for pull requests into $BASE_BRANCH."
    ;;
esac
