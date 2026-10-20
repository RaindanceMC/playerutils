# RaindanceMC/playerutils

- **`playerutils-module`** — Rain's Flexible Modules — XP Bottles: Everything a server owner does to a player's body and wants to know about it: flight that survives a relog, heal and feed, hurt and starve and drown, breath, size, speed, a launch, a clean slate, a bang, somebody else's eyes, somebody else's voice — and what their status, hunger, effects, position and ping are right now. Every target can be named by nickname.
- **`playerutils-standalone`** — Rain's Player Utils: Rain's Player Utils, as a plugin of its own. Nothing here but a build file and a descriptor: the playerutils module and the standard wrapper are shaded together, and that is the whole plugin.

Part of [RaindanceMC](https://github.com/RaindanceMC); built on [RainsCore](https://github.com/RaindanceMC/RainsCore) and the shared parent in [framework](https://github.com/RaindanceMC/framework). Split out of `rains-flexible-modules` on 16.10.2026, history kept.

## Build

```
mvn install
```

Needs Java 25 and read access to `packages.tstieh.de` (server ids `packages-releases` / `packages-snapshots` in `~/.m2/settings.xml`).

## Plugins

- `playerutils-standalone/target/RainsPlayerUtils-0.4.1.jar` — also published as `de.raindancer.modules:playerutils-standalone:0.4.1`

## Versions

A module's version is a property in this repository's root `pom.xml`, a plugin's is `plugin.version` in its own pom. CI publishes every version that is not in the registry yet on the next push to master.
