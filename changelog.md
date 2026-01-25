# 1.21.1-0.0.9
- Changes:
  - Allowed shift placing a brass lamp to force a direction
  - Added flywheel kinetic storage factor configuration
  - Added chain drive cost factor configuration
  - Added feature flags to all recipes, now they will disable if the relevant block(s) are disabled
  - Added rejection for placing a chain drive if the feature is disabled
  - Chair description includes the fact it can have corners and flat backs
- Fixes:
  - Cog chains being allowed to go vertical
  - Mismatched color between particles and outlines on the cog placement
  - Crash with mod's feature flags
  - Chain placement failing didn't give translated feedback
  - UVS for the headlamp model
# 1.21.1-0.0.10
- Changes:
  - Added proper nixie board tiling
- Fixes:
  - Fixed server crash due to client resources being loaded
# 1.21.1-0.0.11
- New feature:
  - Created "ponderflat" world type, a flat world with no structures and a ponder-style floor
# 1.21.1-0.0.12
- New features:
  - Added brass lamp crafting recipe
# 1.21.1-0.0.13
- Temporary Fixes:
  - Wrapped nixie tube target handling in a try-catch to prevent a crash until proper redo can be done
# 1.21.1-0.0.14
- Changes:
  - Changed default color of nixie tubes and boards to the default orange color
  - Added hint text to placing the cogwheel chains
- Fixes:
  - Redid large nixie tube and board handling to match full base create functionality
# 1.21.1-0.0.15
- Fixes:
  - Fixed nixie board multiblock bounds
# 1.21.1-0.0.16
- Changes:
  - Lamp blocks in the mod have analogue brightness now
# 1.21.1-0.0.17
- New features:
  - Created separate "flanged cogwheel" blocks which takes the model of the original cogwheel chain drive
  - Craftable with a respective cogwheel and an iron nugget
- Changes:
  - Chain drives on existing create drives now keep a visually consistent mode
  - Chain geometry will be offset 2px to match the model in the case of base create cogwheels
  - Chain drives NO LONGER HAVE GAPS between segments (this took way too long, but it's beautiful now)
# 1.21.1-0.0.18
- Changes:
  - Added item requirement for printing headlamps
# 1.21.1-0.0.19
- Fixes:
  - Fixed crash related to chains
# 1.21.1-0.0.20
- Fixes:
  - Weathered girder vertical CT
  - Fixed small flanged cogwheel not getting ponder
# 1.21.1-0.0.21
- Changes:
  - Finished the flywheel bearing tooltip
# 1.21.1-0.0.22
- Changes:
  - Full spanish localization
# 1.21.1-0.0.23
- Changes:
  - Fixed chairs not rotating when disassembled
# 1.21.1-0.0.24
- Changes:
  - Allowed chain cogwheels (that aren't flanged) to connect like normal cogwheels
# 1.21.1-0.0.25
- Changes:
  - Ru_ru localisation update
# 1.21.1-0.0.26
- Changes:
  - Fixed crash chaz found related to nixie tubes
# 1.21.1-0.0.27
- Changes:
  - Allowed light force-on when interacting with an empty hand for all lights
# 1.21.1-0.0.28
- Changes:
  - Allowed chairs to have inverse corners (pretty much appearing like seats)
# 1.21.1-0.0.29
- Changes:
  - Fixed spanish translation being invalid
# 1.21.1-0.0.30
- Changes:
  - Turkish translation, thank you to @erdemarikaneskici
  - Fixed concurrent modification exception crash, thanks to @MarioSMB for the solution
# 1.21.1-0.0.31
- Changes:
  - Chain drives no longer drop chains when you are in creative mode
  - Chairs can now be wrenched to force an upwright back
  - Ru_Ru tweaks / fixes, thank you @VladisCrafter and @WatermelonGuy
# 1.21.1-0.0.32
- Changes:
  - Girder struts can be appropriately wrenched to break them
# 1.21.1-0.0.33jolly
- Changes:
  - Ru_Ru translation update, thank you @VladisCrafter and @WatermelonGuy
  - Turkish localization update, thank you @erdemarikaneskici
  - Fixed chain drives placed from schematics resulting in corrupted blocks, this will retroactively fix broken blocks
# 1.21.1-0.0.34
- Changes:
  - Fixed a corrupted state with the chair blocks
  - Nudged the chair armrest arms to be slightly inset to avoid z-fighting with certain blocks
# 1.21.1-0.0.35
- Changes:
  - Proper dist for mixins
  - Flipped 9 number texture in the atlas for nixie tubes and boards
# 1.21.1-0.0.36
- Changes:
  - Girder strut memory leak fix
# 1.21.1-0.0.37
- Changes:
  - Chain drive middle click
  - Reverted asset changes
# 1.21.1-0.0.38
- Changes:
  - Portuguese and Brazilian localization, thank you to @Aquila_Limonida
# 1.21.1-0.0.39
- Changes:
  - Added link to crowdin translations
  - Nixie tubes properly render the back of the text for international (unicode) characters
  - Item tooltips for the lights now notes they can be toggled on
  - Added reviewed machine translations for popular languages that are missing native translations
  - Fixed a crash with girder struts and cogwheel chain drives related to VS plotyards and large distances
  - Corrected UVS of tile slabs and tile stairs
# 1.21.1-0.0.40
- Changes:
  - Added resource reload detection for girder struts. Ideally should prevent wrong textures appearing on the model
# 1.21.1-0.0.41
- Changes (Credit to astral for locking in):
  - Fix chair corner states
  - Properly fix girder strut texture reloads
  - Added placement helper for nixie board and large nixie tube
  - Fixed lightbulb texture
  - Fix girder strut placement in water, and added sound
  - Fix text atlas having C and D swapped
# 1.21.1-0.0.42
- Changes:
  - Fixed LODs activating when a chain is part of a sublevel or ship in VS / sable