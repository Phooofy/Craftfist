# Craftfist revisions

Each completed change gets a new patch version: 1.0.1, 1.0.2, 1.0.3, and so on. The single version setting is `mod_version` in `gradle.properties`; Gradle embeds it into the mod metadata and JAR filename.

Successful builds archive the installable JAR, complete project source ZIP, and SHA-256 checksums under `revisions/<version>/`. Existing archives cannot be replaced with different contents; the build requires a new version number. Keep this directory to preserve rollback history.

| Version | Change |
| --- | --- |
| 1.0.0 | Preserved baseline: latest momentum-preserving charge implementation, prior to revision numbering. Includes the current slab and wall-sliding fixes. Earlier builds repeatedly used this filename and are not recoverable from this project. |
| 1.0.1 | Revision numbering, embedded version metadata, automatic immutable build archives, and rollback instructions. Gameplay matches the 1.0.0 baseline. |
| 1.0.2 | Punch charging always slows new ground movement input to 35%, including when activated while walking. Existing velocity is not damped by the charge; airborne momentum and normal gravity/friction remain intact. |
| 1.0.3 | Full-precision velocity synchronization for fast combos; directional wall-impact checks; empowerment consumed on launch; menus cancel punch charge; Power Block retains damage attribution; Slam stays armed through long falls; Meteor steering ignores camera pitch; cooldowns continue while unequipped. |
| 1.0.4 | Slam travels about three blocks farther on flat ground and hits a wider 90-degree cone with matching particles. Punch charge slows ground and air steering to 35%, brakes ordinary incoming movement once, and preserves momentum only from a tagged ability launch until landing or cancellation. |
| 1.0.5 | Meteor Strike physically descends at six blocks per tick instead of teleporting, impacts on landing, and sends a 360-degree ground particle wave out to eight blocks. Descent retains fall protection and ends safely if no landing surface is reached. |
| 1.0.6 | Redesigned left-corner HUD with six pixel ability icons, keybind badges, ammo pips, cooldown/charge bars, and ultimate progress. Hand Cannon cannot regenerate during a rapid burst: its first replacement round arrives after one second without firing, followed by the normal 13-tick regeneration interval. |

| 1.0.7 | Shorter player-facing documentation and repository cleanup for publishing. Gameplay matches 1.0.6. |
| 1.0.8 | Meteor Strike gives five seconds to rise and aim before diving. Ascent height and the half-second early-dive unlock stay the same. |
| 1.0.9 | Meteor Strike hovers without movement input and uses WASD relative to camera yaw at the same 1.2-block/tick speed. Diagonal movement is normalized and the dive remains straight down. |
| 1.0.10 | Rocket Punch hits nearby enemies together within a 1.5-block impact radius. Every target receives damage, knockback, and its own wall-impact check; the dash still stops on the first contact. |

| 1.0.11 | Removed ability icons and replaced the HUD with compact text, aligned keybinds, and subtle progress lines in the bottom-left corner above the hotbar. |

## Restore a revision

To install an older version, close Minecraft, remove the current Craftfist JAR from `mods/`, and install `revisions/1.0.1/craftfist-1.0.1.jar`. Keep only one Craftfist JAR installed. Back up your world before switching versions that change saved data.

For development rollback, extract the archived project ZIP into a separate folder and use its source. Preserve the main project's `revisions/` directory. A later modified build must receive a new version rather than overwrite the restored archive.


