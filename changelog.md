# 1.21.1-0.0.9

- Changes:
    - Allowed shift placing a brass lamp to force a direction
    - Added flywheel kinetic storage factor configuration
    - Added chain drive cost factor configuration
    - Added feature flags to all recipes, now they will disable if the relevant block (s) are disabled
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

# 1.21.1-0.0.43

- Changes:
    - Proper polish translations, thank you Imperator Pablo
    - Smoother looking tile slab and tile stair textures thank you to IcaroJam

# 1.21.1-0.0.44

- Changes:
    - Fixed girder struts in schematics
    - Fixed coghweel chain drives in schematics

# 1.21.1-1.0.0

- Changes:
    - Belts can be right-clicked with glow ink sacs to make them glow

# 1.21.1-2.1.5

- Changes:
    - Fixed residual chain creation missing certain integrity checks

# 1.21.1-2.1.6-beta

- First public beta release of Bits n Bobs 2.0.0
- Changes since to 2.1.4:
    - Trusses now have proper dyed and visual behavior when encasing pipes
    - Fixed encasing blocks breaking the chain they are a part of

# 1.21.1-2.1.7-beta

- Fixed chipped incompatibility (And possibly other mods), where chains would not render with flywheel enabled.

# 1.21.1-2.1.8-beta

- Fixed bug in reload listeners

# 1.21.1-2.1.9-beta

- Attempted to fix lerping issue on carriage contraptions
- Added recipe for flywheel bearing

# 1.21.1-2.1.10-beta

- Properly linked pipe config to dyeing action so it can actually be disabled.
- Changed all toLowerCase () calls to toLowerCase (Locale.ROOT) to avoid issues with Turkish locale.
- Cleanup missing assets in log

# 1.21.1-2.1.11-beta

- Improved feature flag group behaviour
- Improved chain config behaviour

# 1.21.1-2.1.12-beta

- Fixed ponder foreign label issue
- Fixed bad ponder baseplate size
- Fixed being unable to watch all new ponders in chain
- Fixed chain interaction being possible through blocks on aeronautics contraptions
- Fixed dyed pipes and other fluid components not saving to schematics or being printed
- Fixed chain ponder baseplate size
- Added sable tags

# 1.21.1-2.1.13-beta

- Misc changes and improvements to dyeable behaviours to enable Bits 'n' Dyes
- Fix fluid tanks not saving dye colour to schematics

# 1.21.1-2.1.14-beta

- Dedicated server related fixes

# 1.21.1-2.1.15-beta

- Add config options for max cogwheel chain size and node count

# 1.21.1-2.2.0

- 2.0.0 stable release!
- Added cogwheel materials, allowing you to change the wood type of cogwheels
- Updated the model for flanged cogwheels to allow them to have cogwheel materials
- Added flanged cogwheel connectivity, flanged cogwheels can connect to other cogwheels but not other flanged cogwheels.
- Added cogwheel material compat to:
  Create: Connected, Create: Slice 'n' Dice, Create: Hypertubes, Create: Additional Logistics, Create: Encased
- Added a config option to suppress the wooden cogwheels in Create: Encased
- Fixed excessively large headlamp shape with sable
- Fixed an issue where dyed nixie blocks would not drop anything
- Fixed flanged cogwheels turning into normal cogwheels when being unencased
- Fixed brass lamp model
- Fixed weathered metal bracket not having a recipe
- Added weathered metal girder to #railways:semaphore_poles tags

# 1.21.1-2.2.1

- Added waterlog functionality to industrial grating
- Fixed encased flanged cogwheels not working for belt drives
- Untag as beta

# 1.21.1-2.2.2

- Added proper incompatibility warning for incompatible TMFG version
- Fixed mixin issue when loading without an optional compat
- Potentially fixed cogwheel material and fluid dying mechanics not working

# 1.21.1-2.2.3

- Internal refactors to dye system
- Fixed item models of DnDecor not also being corrected

# 1.21.1-2.2.4

- Further internal refactors and improvements to dye system

# 1.21.1-2.2.5

- Reduced neoforge version requirement

# 1.21.1-2.2.6

- Fix cogwheel rendering with flywheel off

# 1.21.1-2.2.7

- Redo flanged cogwheel encased rendering

# 1.21.1-2.2.8

- Changed flanged cogwheel model from 12 sides to 8 sides
- Fixed incompatibility with Create: Petrol's Parts differential
- Further pulled back neo requirement, im hoping there was a reason it wasnt fully back

# 1.21.1-2.2.9

- Fix encasing chain cogwheels deleting the chain due to a missing `!`

# 1.21.1-2.3.0

- Cogwheel chain model no longer twists!
- Fixed chain cogwheels not properly propagating kinetics in a specific case
- Added missing null guard when transferring chain controller dealing with a controller in a broken state.
- Fixed #164 where certain creative tabs would cause crashes with item suppression
- Block vertical chain carriages instead of crashing

# 1.21.1-2.3.1

- Fixed item suppression causing creative tabs to not link to item groups

# 1.21.1-2.3.2

- (#167) Fixed chain drive behaviour overriding gears n kinetics behaviour
- Added missing chain failed addition lang for using a chain on other types of cogwheel
- Restored a deprecated old signature of `CogwheelChainRenderGeometryBuilder#renderChainSlowerButWithoutGaps` to avoid
  crash with bits n tracks

# 1.21.1-2.3.3

- Stop controlling belt shift click

# 1.21.1-2.3.4

- Increased available bits for headlamp colours