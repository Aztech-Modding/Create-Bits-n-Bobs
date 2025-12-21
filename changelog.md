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
# 1.20.1-0.0.18
- Created backport to 1.20.1
- Note that this version is slightly behind the 1.21.1 version, it will updated over time
# 1.20.1-0.0.23
- Fixed recipes x_X
- Edits carried from 0.0.19-0.0.22:
  - Full spanish localization
  - Finished the flywheel bearing tooltip
  - Fixed crash related to chains
  - Weathered girder vertical CT
  - Fixed small flanged cogwheel not getting ponder
  - Fixed chairs not rotating when disassembled
# 1.21.1-0.0.24
- Changes:
    - Allowed chain cogwheels (that aren't flanged) to connect like normal cogwheels
# 1.21.1-0.0.25
- Changes:
    - Ru_ru localisation update
# 1.21.1-0.0.26
- Changes:
    - Fixed crash shalz found related to nixie tubes
# 1.20.1-0.0.26
- Changes carried from 0.0.24-0.0.26:
  - Allowed chain cogwheels (that aren't flanged) to connect like normal cogwheels
  - Ru_ru localisation update
  - Fixed crash shalz found related to nixie tubes
# 1.21.1-0.0.27
- Changes:
  - Allowed light force-on when interacting with an empty hand for all lights
# 1.21.1-0.0.28
- Changes:
  - Allowed chairs to have inverse corners (pretty much appearing like seats)
# 1.21.1-0.0.29
- Changes:
  - Fixed spanish translation being invalid
# 1.20.1-0.0.29
- Changes carried from 0.0.27-0.0.29:
  - Fixed spanish translation being invalid
  - Allowed chairs to have inverse corners (pretty much appearing like seats)
  - Allowed light force-on when interacting with an empty hand for all lights
# 1.20.1-0.0.29b
- Fixed nametags on nixie tubes and boards
# 1.20.1-0.0.30
- Changes:
  - Turkish translation, thank you to @erdemarikaneskici
  - Fixed concurrent modification exception crash, thanks to @MarioSMB for the solution
  - Fixed glass tag in lightbulb recipe
# 1.20.1-0.0.30b
- Changes:
  - Fix concurrent modification exception crash properly