# Create machine tiers

amcore 1.1 gives Create's press, mixer, saw, deployer and spout a tier, read from block tags filled by the pack.
Each tier has its own recipe types. A machine runs the recipes of its tier, of every lower tier and Create's own, and
works faster.

| Tier | Recipe types | Speed (default) | Spout tank (default) |
| --- | --- | --- | --- |
| base (any block outside the tier tags) | Create's, unchanged | ×1 | 1 000 mB |
| advanced | `amcore:advanced_<process>` | ×1.5 | 4 000 mB |
| elite | `amcore:elite_<process>` | ×2 | 8 000 mB |
| ultimate | `amcore:ultimate_<process>` | ×3 | 16 000 mB |

When several recipes fit the same input, the highest tier wins. For each tier, a sequenced assembly step comes before a
plain recipe, and Create's recipes come last.

## Changelog

1.1.0

- Machine tiers for Create's press, mixer, saw, deployer and spout, read from the block tags
  `amcore:machine_tiers/{advanced,elite,ultimate}`.
- 17 recipe types `amcore:<tier>_<process>`, in the same JSON format as Create's. They work as sequenced assembly
  steps (pressing, cutting, deploying, filling).
- Speed bonus per tier on every recipe, Create's included. Bigger spout tank per tier.
- KubeJS schemas `event.recipes.amcore.<tier>_<process>`, identical to KubeJS Create's.
- JEI: one category per tiered type, with the machines that can run it. Each category, its icon and each tiered
  sequenced assembly step draw the machine of the tier (with Create Encased's own mixer head and spout bottom when it
  has them). Tiered machines get a "Tier: ..." tooltip line.
- New server config `config/amcore-server.toml`.
- The KubeJS plugin registers once, although the jar holds two mods (amcore and create_metallurgy).
- Create 6.0.10 or newer is now a required dependency.

## Tags

Block tags, filled by the pack. A block in several tags gets the highest tier. The machine kind comes from the block
entity, so one tag holds the machines of every kind.

| Tag | Tier |
| --- | --- |
| `amcore:machine_tiers/advanced` | advanced |
| `amcore:machine_tiers/elite` | elite |
| `amcore:machine_tiers/ultimate` | ultimate |

```js
ServerEvents.tags('block', event => {
  event.add('amcore:machine_tiers/advanced', ['createcasing:zinc_press', 'createcasing:zinc_mixer',
    'createcasing:zinc_mechanical_saw', 'createcasing:zinc_deployer', 'createcasing:zinc_spout'])
  event.add('amcore:machine_tiers/elite', ['createcasing:brass_press', 'createcasing:brass_mixer',
    'createcasing:brass_mechanical_saw', 'createcasing:brass_deployer', 'createcasing:brass_spout'])
  event.add('amcore:machine_tiers/ultimate', ['createcasing:shadow_steel_press', 'createcasing:shadow_steel_mixer',
    'createcasing:shadow_steel_mechanical_saw', 'createcasing:shadow_steel_deployer'])
})
```

After `/reload`, recipes and speed follow the new tags. A spout's tank size is set when the spout loads (see "Known
limits"). JEI catalysts use the machines that are both in a tier tag and in Create's machine tag of the process
(`create:presses`, `create:mixers`, `create:saws`, `create:deployers`, `create:spouts`). The machine drawn in a
category, and its icon, is the machine of exactly that tier (the first one if the tag holds several), else the lowest
machine above it, else Create's.

## Recipe types

| Process | Machine | Advanced | Elite | Ultimate | Same format as |
| --- | --- | --- | --- | --- | --- |
| pressing (belt, ground) | press | `amcore:advanced_pressing` | `amcore:elite_pressing` | `amcore:ultimate_pressing` | `create:pressing` |
| compacting (basin) | press | `amcore:advanced_compacting` | `amcore:elite_compacting` | `amcore:ultimate_compacting` | `create:compacting` |
| mixing (basin) | mixer | `amcore:advanced_mixing` | `amcore:elite_mixing` | `amcore:ultimate_mixing` | `create:mixing` |
| cutting | saw | `amcore:advanced_cutting` | `amcore:elite_cutting` | `amcore:ultimate_cutting` | `create:cutting` |
| deploying | deployer | `amcore:advanced_deploying` | `amcore:elite_deploying` | `amcore:ultimate_deploying` | `create:deploying` |
| filling | spout | `amcore:advanced_filling` | `amcore:elite_filling` | — | `create:filling` |

Only the `type` differs from the Create recipe: `heat_requirement`, `processing_time`, `keep_held_item`, chances and
fluids work the same.

## KubeJS

Each schema is the KubeJS Create schema of the matching Create type, so it takes the same arguments and methods as
`event.recipes.create.<process>`. Always give an explicit ID.

```js
ServerEvents.recipes(event => {
  // Pressing: output(s), one input
  event.recipes.amcore.advanced_pressing('create:iron_sheet', '#c:ingots/iron')
    .id('kubejs:create/advanced_pressing/iron_sheet')

  // Compacting: outputs, inputs (items and fluids), heat like create:compacting
  event.recipes.amcore.elite_compacting('minecraft:obsidian', ['minecraft:cobblestone', Fluid.of('minecraft:lava', 250)])
    .heated()
    .id('kubejs:create/elite_compacting/obsidian')

  // Mixing: .heated() / .superheated()
  event.recipes.amcore.elite_mixing('2x create:brass_ingot', ['#c:ingots/copper', '#c:ingots/zinc'])
    .heated()
    .id('kubejs:create/elite_mixing/brass_ingot')

  // Cutting: processing time in ticks, like create:cutting
  event.recipes.amcore.ultimate_cutting('6x minecraft:oak_planks', 'minecraft:oak_log')
    .processingTime(50)
    .id('kubejs:create/ultimate_cutting/oak_planks')

  // Deploying: outputs, [processed item, held item], .keepHeldItem()
  event.recipes.amcore.advanced_deploying('create:cogwheel', ['create:shaft', '#minecraft:planks'])
    .keepHeldItem()
    .id('kubejs:create/advanced_deploying/cogwheel')

  // Filling: output, [item, fluid]
  event.recipes.amcore.advanced_filling('minecraft:wet_sponge', ['minecraft:sponge', Fluid.of('minecraft:water', 1000)])
    .id('kubejs:create/advanced_filling/wet_sponge')
})
```

### Sequenced assembly

KubeJS Create's `sequence` accepts any recipe, so amcore steps go in `event.recipes.create.sequenced_assembly` like
Create's. A step only advances on a machine of its tier or above; the other steps keep their own types.

```js
ServerEvents.recipes(event => {
  let inter = 'create:incomplete_precision_mechanism'
  event.recipes.create.sequenced_assembly(['create:precision_mechanism'], '#c:plates/gold', [
    event.recipes.create.deploying(inter, [inter, 'create:cogwheel']),
    event.recipes.amcore.advanced_pressing(inter, inter),
    event.recipes.amcore.elite_filling(inter, [inter, Fluid.of('minecraft:lava', 100)]),
    event.recipes.amcore.advanced_deploying(inter, [inter, '#c:nuggets/iron']),
    event.recipes.amcore.ultimate_cutting(inter, inter)
  ])
    .transitionalItem(inter)
    .loops(3)
    .id('kubejs:create/sequenced_assembly/tiered_precision_mechanism')
})
```

Raw JSON (`event.custom` or a datapack) works too: a step is a Create recipe object whose `type` is the amcore type.

```json
{
  "type": "create:sequenced_assembly",
  "ingredient": { "tag": "c:plates/gold" },
  "transitional_item": { "id": "create:incomplete_precision_mechanism" },
  "loops": 3,
  "results": [ { "id": "create:precision_mechanism" } ],
  "sequence": [
    {
      "type": "amcore:advanced_pressing",
      "ingredients": [ { "item": "create:incomplete_precision_mechanism" } ],
      "results": [ { "id": "create:incomplete_precision_mechanism" } ]
    }
  ]
}
```

Compacting and mixing are not sequenced assembly steps, as in Create.

## Config

Server config: `config/amcore-server.toml`. It is read by the server (the integrated one in single player) and sent to
clients when they join, so on a dedicated server the server's file is the one that counts.

| Key (section `machine_tiers`) | Default | Range |
| --- | --- | --- |
| `advancedSpeedMultiplier` | 1.5 | 1–16 |
| `eliteSpeedMultiplier` | 2.0 | 1–16 |
| `ultimateSpeedMultiplier` | 3.0 | 1–16 |
| `advancedSpoutCapacity` | 4000 | 1000–1000000 mB |
| `eliteSpoutCapacity` | 8000 | 1000–1000000 mB |
| `ultimateSpoutCapacity` | 16000 | 1000–1000000 mB |

A multiplier of 1 disables the bonus for that tier. The existing `config/amcore-common.toml` (hot stuff) is unchanged.

How the bonus applies:

| Machine | Effect |
| --- | --- |
| Press | head speed × multiplier, after Create's cap; Create stops the head at the pressing point, so it never skips it |
| Mixer | processing time ÷ multiplier (at least 1 tick) |
| Saw | processing speed × multiplier, after Create's clamp |
| Deployer | timer speed × multiplier (the hand animation follows) |
| Spout | filling time 20 ticks ÷ multiplier, at least 6 (the spout fills at tick 5) |

## Lang keys

In `assets/amcore/lang/en_us.json` and `fr_fr.json`:

| Key | English |
| --- | --- |
| `amcore.machine_tier.tooltip` | `Tier: %s` |
| `amcore.machine_tier.advanced` / `.elite` / `.ultimate` | `Advanced` / `Elite` / `Ultimate` |
| `amcore.machine_tier.assembly` | `%s (%s+)`, the sequenced assembly step text, e.g. "Pressing (Advanced+)" |
| `amcore.recipe.<tier>_<process>` (17 keys) | JEI category titles: `Advanced Pressing`, `Elite Compacting`, `Ultimate Mixing`, `Advanced Sawing`, `Elite Deploying`, `Advanced Filling`... |

## Known limits

- **JEI sequenced assembly**: Create 6.0.10 never reads the machines a step requires, so the category cannot list the
  tier machines. A tiered step draws the machine of its own tier and shows its tier in its text ("Pressing
  (Advanced+)"), in JEI and on the transitional item; the higher tiers that can also run it are not listed.
- **Spout tank size** is set when the spout loads. After a tag or config change, it applies to spouts placed or
  reloaded afterwards. A spout whose capacity shrinks keeps its fluid (Create does not clamp the tank on load): it can
  still drain it, but accepts no more until it is under the new capacity.
- **Deployers on contraptions** have no block entity and stay at the base tier.
- **The saw** cycles through the recipes that fit, as in Create, but only among those of the highest fitting tier.
- **Mixer and DragonsPlus**: Create: Dragons Plus appends its own mixing recipes after the tiered ones, so a tiered
  recipe still wins over them.

## Testing

Checked on a dev server with Create 6.0.10, using a test datapack. All amcore mixins applied (Mixin audit with no
errors), and every recipe type and the sequenced assembly with amcore steps parsed. An advanced press chose the advanced
recipe over the elite one, still ran Create's recipes, and found an amcore step in a sequenced assembly. Its speed at
256 RPM went from 30 to 45. An advanced spout held 4 000 mB and found its tier filling recipe; a base spout did not. An
elite deployer found its elite recipe.

Still to check in game, in the pack (JEI and the client can't be run in the dev container):

- each machine of each tier runs its recipes and the lower tiers'; Create's machines run only Create's;
- a sequenced assembly step only advances on a machine of the right tier;
- speed and animation in step, at several RPMs; spout never stuck, bottles, buckets and Create Enchantment Industry
  fillings still work;
- Goggles: spout capacity 4 000 / 8 000 mB, `create:spout` 1 000 mB;
- JEI: 17 categories with catalysts, "Tier: ..." tooltip;
- `/reload` after a tag change; `0 failed recipes` in `logs/kubejs/server.log`; clean `logs/latest.log`;
- Create Diesel Generators, Slice and Dice, Create: Dragons Plus, Create Integrated Farming, Deployer lib, Create Heat
  JS, Create Enchantment Industry and Create Encased still work.
