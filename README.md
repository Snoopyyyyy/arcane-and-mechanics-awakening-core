# Arcane & Mechanics Core

Core mod of the **Arcane & Mechanics** modpack for Minecraft 1.21.1 on NeoForge.

It provides the content the modpack needs that KubeJS scripts cannot handle cleanly. Content is declared in Java, and its resources (models, blockstates, translations) are produced by data generation.

## Requirements

| Component | Version |
|-----------|---------|
| Minecraft | 1.21.1 |
| NeoForge  | 21.1.248 or newer |
| Create    | 6.0.10 or newer |
| Java      | 21 |

KubeJS (with KubeJS Create) and JEI are optional; amcore adds recipe schemas and categories when they are present.

## Features

- [Create machine tiers](docs/machine-tiers.md): tiered recipe types, speed and spout capacity for Create's press,
  mixer, saw, deployer and spout, with their KubeJS schemas and JEI categories.

## Development

Work happens on the `develop` branch.

The build runs in the dev container defined in `.devcontainer/`, so Java is not needed on the host:

```sh
docker compose -f .devcontainer/compose.yaml up -d
docker exec arcane-mechanics-core sh -c 'cd /app && ./gradlew build'
```

The mod jar is written to `output/amcore-<version>.jar`.

### Data generation

Generated resources live in `src/generated/resources` and are committed. They are never edited by hand: change the providers in `com.snoopycorp.amcore.data`, then regenerate and rebuild:

```sh
docker exec arcane-mechanics-core sh -c 'cd /app && ./gradlew runData build'
```

Translations are provided for `en_us` and `fr_fr`.

### Publishing

Work is merged into `develop` through pull requests. The branch name sets how `mod_version` in `gradle.properties` must change:

| Branch      | Version change           | Example         |
|-------------|--------------------------|-----------------|
| `feature/*` | Minor bump               | 1.2.3 → 1.3.0   |
| `fix/*`     | Patch bump               | 1.2.3 → 1.2.4   |
| other       | None, nothing is published | 1.2.3 → 1.2.3 |

Bump the version once, on the branch, and describe the changes in `patch-notes/<version>.md`.

The `Pipeline` workflow runs on every pull request into `develop` or `main`, and on pushes to these branches that change files under `src/`. It chains three jobs:

1. **Check**: on a pull request, fails when the version doesn't follow the rules above or its patch notes are missing. On a push, decides what to publish and fails when the version to publish has no patch notes.
2. **Build**, when files under `src/` changed: builds the jar and keeps it as the artifact `amcore-<branch>` (`amcore-pr-<number>` for a pull request). Only the latest jar of each branch is kept.
3. **Publish**, on pushes only: uploads that jar to CurseForge.

`Check` runs on every pull request so that it can be required to merge: in the repository rules of `develop` and `main`, require a pull request and the status check `Check`.

| Branch    | CurseForge type | Published when                         | Changelog |
|-----------|-----------------|----------------------------------------|-----------|
| `develop` | Beta            | the push changed `mod_version`         | `patch-notes/<version>.md` |
| `main`    | Release         | `v<version>` isn't tagged yet (it is tagged after publishing) | every patch note since the previous release |

The workflow needs, in the repository settings:

- the secret `CURSEFORGE_TOKEN`, an API token from the CurseForge account settings;
- the variable `CURSEFORGE_PROJECT_ID`, the project ID shown on the CurseForge project page.

## License

All Rights Reserved, © Snoopy.
