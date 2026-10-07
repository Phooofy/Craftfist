# Craftfist

Doomfist in Minecraft: craft a gauntlet, equip it, and chain Rocket Punch, Seismic Slam, Rising Uppercut, Power Block, and Meteor Strike.

Built for **Minecraft Java 1.21.1**, **Fabric Loader 0.16.14+**, **Fabric API**, and **Java 21**. Install the mod on both the client and server for multiplayer.

## Getting started

Put the Craftfist JAR and Fabric API in your `mods` folder. Craft the gauntlet using this recipe:

```text
I N I
R D R
  I I
```

`I` = iron block, `N` = netherite ingot, `R` = redstone block, `D` = diamond.

You can also use `/give @s craftfist:gauntlet`. Hold it in either hand to equip the Thunder Doomfist skin and fist model. Put it away to return to your normal skin.

## Controls

| Input | Ability |
| --- | --- |
| Left click | Hand Cannon: four rounds, seven pellets per shot |
| Hold right click or R, then release | Rocket Punch: charge for up to 1.5 seconds and dash forward |
| Left Shift | Seismic Slam: leap forward and damage enemies in a seven-block, 90-degree cone on landing |
| X | Rising Uppercut: launch yourself and nearby enemies |
| V | Power Block: block for three seconds; press again to cancel |
| Z | Meteor Strike: rise and reposition, then dive down; press again after half a second to dive early |
| Space during Rocket Punch | Jump cancel |

Change bindings under **Craftfist** in Minecraft's Controls menu. The bottom-left HUD shows ammo, cooldowns, charge, ultimate progress, and current bindings in a compact text layout above the hotbar.

While equipped, right click charges Rocket Punch and Left Shift is reserved for Slam. With the gauntlet in your offhand, left click still fires Hand Cannon rather than using your main-hand item.

## Combos and combat

- Hold a charged punch and press Slam to turn the charge into extra launch speed. You can also Slam during a punch dash.
- Uppercut keeps your horizontal momentum. Punch keeps upward momentum, and jump cancel gives a small boost.
- Charging slows movement and air steering. Momentum carried from another ability survives until you land; ordinary walking and sprint-jumping still slow down.
- Punch stops on the first enemy it hits and also damages and knocks back visible enemies within a 1.5-block impact radius in front of you. Each enemy knocked into a wall takes eight extra damage.
- Power Block reduces frontal damage by 80%. Blocking eight damage empowers your next punch. Empowerment lasts ten seconds and is spent when you launch the punch or convert it into Slam.
- Hand Cannon waits one second after your last shot before restoring a round. Remaining rounds return every 13 ticks. Firing restarts the wait.
- Ability hits grant temporary shields, up to eight hearts, and build ultimate charge. Ultimate also charges over time.

Meteor Strike drops at six blocks per tick and impacts the first surface you land on. Its damage reaches eight blocks in all directions, with an expanding circular particle wave. The airborne aiming phase lasts five seconds. Use WASD during this phase to move relative to your camera at 1.2 blocks per tick. Releasing the keys makes you hover. Diagonal movement keeps the same speed, and looking up or down does not affect it.

## Building

Install JDK 21, then run:

```powershell
.\gradlew.bat build
```

On Linux or macOS, use `./gradlew build`. The playable mod is `build/libs/craftfist-<version>.jar`; the sources JAR is for development.

Use `gradlew.bat runClient` for a development client. Builds also save a numbered JAR, source ZIP, and checksums under `revisions/`. See [REVISIONS.md](REVISIONS.md) for changes and rollback instructions.

## Assets

The mod uses a Thunder Doomfist skin and Minecraft sound effects. Custom sounds can be supplied through a resource pack using the events in `assets/craftfist/sounds.json` and mono OGG files.

This is an unofficial fan mod. Doomfist and Overwatch belong to Blizzard; the character texture belongs to its creator.



