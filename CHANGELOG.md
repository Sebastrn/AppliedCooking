# Changelog

All notable changes to **Applied Cooking** are listed here.

## 6.2.1

### Fixed
- **The Kitchen Station screen now glows.** The emissive display intended for 6.2.0 was missing from the model,
  so the screen never lit up. It now renders bright: brightest when online, dimmer for the linked-but-unreachable
  state, and off when unlinked.

## 6.2.0

A visual overhaul of the ME Kitchen Station. No gameplay changes.

### Added
- **The screen now reads its status in three states.** Purple means linked and powered. Red means it's
  linked but can't reach the network (no power to spare, the Access Point inactive, or its chunk unloaded).
  A dark screen means it isn't linked yet. Before, the linked-but-unreachable case looked identical to
  unlinked; now it's distinct, and Jade and The One Probe report the same three states in words.

### Changed
- **New model and textures:** a retro desktop-computer look with a tilted screen, replacing the old block.
- **The item shows the powered (purple) look** in your inventory, hotbar, and tooltips.
- **Rebuilt the block's hitbox** to fit the new model.
- Updated the guide's "Reading the Screen" page and screenshots for the three states.
- **Restyled the guide book** with a new book texture and item icon.

## 6.1.0

Recipe-viewer support beyond JEI. No gameplay changes.

### Added
- **Roughly Enough Items and EMI support.** The ME Kitchen Station's info page that already appeared in JEI now shows
  in REI and EMI too. Use whichever recipe viewer you like; all three are optional.

## 6.0.0

Updated to **Minecraft 1.21.1** on NeoForge. Everything from 5.1.0 carries over: ME items and fluids in the
Cooking Table, the configurable power draw, Jade/The One Probe, JEI, and the guide book.

### Changed
- **The guide book is now crafted with green wool** instead of red, to match the book's own colour.
- **Requires Cooking for Blockheads 21.1.7 or newer.** From that version on, Cooking for Blockheads returns
  leftover containers itself, and Applied Cooking now leaves that to it.
- Leftover containers (the empty bucket from a milk or water recipe) now come back through Cooking for
  Blockheads, which returns them to you. In 5.1.0 the Station dropped them on the floor when the network had
  nowhere to put them; it no longer needs to.
- A Kitchen Station **linked in an older version loses its link** and needs re-linking at the Wireless Access
  Point. Minecraft 1.21 replaced item NBT with data components, and the link moved with it.

## 5.1.0

### Added
- **The Station's power draw is now configurable.** It was fixed at 5 AE/t; it's now a server config option
  (`kitchenStation.idlePowerDrain`), still 5 AE/t by default, and set it to 0 to make the Station free to run.
  Changing it takes effect on a config reload, and Jade/The One Probe report whatever value is actually in use.

### Fixed
- An empty bucket left over from cooking (milk, water) could be **destroyed** if your ME network had nowhere to put
  it back: full, or partitioned so nothing accepted it. It's now dropped at the Station instead of vanishing.

## 5.0.0

First **NeoForge 1.20.4** release, ported from Forge 1.20.1.

### Added
- **Fluids from your ME network.** The Kitchen Station can now satisfy recipes that need **water and milk** by draining them straight from network storage (matched by tag, like a Sink or Milk Jar), with a network-driven fallback for other fluids such as lava.
- **Network power draw.** While connected, the Station uses a small amount of power from the linked network. If the network can't spare it, the Station goes offline until power returns. The draw is shown in Jade and The One Probe.
- **JEI info page** for the ME Kitchen Station, explaining how to link and use it.
- **10 translations:** German, Spanish, French, Italian, Korean, Brazilian Portuguese, Russian, Swedish, and Simplified & Traditional Chinese.

### Changed
- **Rewrote the in-game guide** for current AE2: you now link the Station at a **Wireless Access Point's GUI** (the old ME Security Terminal is gone). Added a rotatable 3D multiblock preview and a dedicated "Reading the Screen" page, and localized the whole guide.
- Made the connected/disconnected screen and the Jade/TOP tooltips reliable, and lightened the Station's per-tick network work.
- Applied Cooking now depends on **Balm** directly. **Jade, The One Probe, JEI, and Patchouli are all optional**: the mod runs fine without any of them.
