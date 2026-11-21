# Arcane & Mechanics Core

Core mod of the **Arcane & Mechanics: Awakening** modpack for Minecraft 1.21.1 on NeoForge.

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

## License

All Rights Reserved, © Snoopy.

Files taken from the NeoForge MDK template remain under the MIT license, see `TEMPLATE_LICENSE.txt`.
