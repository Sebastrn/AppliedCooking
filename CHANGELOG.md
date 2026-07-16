# Changelog

All notable changes to **Applied Cooking** are listed here.

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
- Applied Cooking now depends on **Balm** directly. **Jade, The One Probe, JEI, and Patchouli are all optional** — the mod runs fine without any of them.
