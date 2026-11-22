# Arcane & Mechanics Core

Core mod of the **Arcane & Mechanics** modpack for Minecraft 1.21.1 on NeoForge.

It provides the content the modpack needs that KubeJS scripts cannot handle cleanly. Content is declared in Java, and its resources (models, blockstates, translations) are produced by data generation.

## Requirements

| Component | Version |
|-----------|---------|
| Minecraft | 1.21.1 |
| NeoForge  | 21.1.248 or newer |
| Java      | 21 |

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

The `Build and publish` workflow builds the jar and uploads it to CurseForge on every push:

| Branch    | CurseForge type | Version |
|-----------|-----------------|---------|
| `main`    | Release         | `mod_version`, tagged `v<version>` |
| `develop` | Beta            | `mod_version-beta.<run number>` |

A version that is already tagged is not published again, so bump `mod_version` in `gradle.properties` before merging into `main`.

The workflow needs, in the repository settings:

- the secret `CURSEFORGE_TOKEN`, an API token from the CurseForge account settings;
- the variable `CURSEFORGE_PROJECT_ID`, the project ID shown on the CurseForge project page.

## License

All Rights Reserved, © Snoopy.
