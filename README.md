# OwnTheSkies - Advanced Mace PvP Combat Mod

A Fabric mod for Minecraft 1.21.9+ that implements advanced mace PvP techniques, including weapon swapping, armor swapping, and automated aerial combat sequences based on competitive mace combat strategies.

## Features

### Core Mace PvP Techniques

#### 1. **Auto-Attack (Never Miss Your Swing!)** 🆕
Automatically swings your mace when falling to ensure you never miss the critical moment and take unnecessary fall damage.

- **Automatic swing detection** - Triggers when you're falling with mace equipped
- **Smart targeting** - Attacks nearby enemies within 4 blocks
- **Fall damage prevention** - Swings automatically when close to ground (within 2 blocks)
- **One attack per fall** - Only attacks once to prevent spam
- **50ms cooldown** - Prevents accidental double attacks

#### 2. **Sword/Axe Swapping** (Nearly Doubles Damage!)
The most fundamental technique in mace PvP - automatically swaps from your sword/axe to mace **right before** landing the hit to transfer damage and attack speed bonuses.

- Hold **Netherite Axe** or **Netherite Sword** while falling
- Mod automatically switches to **Mace** at the optimal moment (within 2-3 blocks of target)
- Can increase damage from 3.5 hearts to 6+ hearts
- **Netherite Axe is statistically preferred** for maximum damage boost

#### 2. **Elytra to Chestplate Swapping**
Critical technique for aerial mace combat - the mace cannot deal falling damage while gliding with elytra!

- Automatically detects when you're falling with elytra equipped
- Swaps to chestplate mid-fall to apply **full accumulated fall damage**
- Swaps back to elytra after landing for continued mobility
- Note: Manual left-click may be required for armor swap in some cases

#### 3. **Auto-Switching System**
- **Falling (0.5+ blocks/tick)**: Switches to sword/axe for weapon swap technique
- **Fast Falling (2.5+ blocks/tick)**: Direct mace switch for spam clicking
- **After Landing**: Automatically switches back to mobility items
- **Ascending/Gliding**: Ensures mobility items are ready

### Loadout Profiles

Four specialized profiles optimized for different mace PvP scenarios:

#### 1. **Density (Aerial)** - Standard aerial combat
- **Focus**: Maximum aerial damage with Density enchantment
- **Weapons**: Mace + Netherite Axe/Sword swap
- **Mobility**: Wind Charge, Ender Pearl, Firework Rocket
- **Armor**: Elytra ↔ Netherite Chestplate swapping
- **Techniques**: Weapon swapping ✓ | Armor swapping ✓

#### 2. **Breach (Ground)** - Ground combat specialist
- **Focus**: Ignores 60% of enemy armor with Breach enchantment
- **Weapons**: Breach Mace + Netherite Axe for maximum damage
- **Mobility**: Wind Charge, Ender Pearl
- **Armor**: Netherite Chestplate only
- **Techniques**: Weapon swapping ✓ | Best with Strength Potion
- **Note**: Can deal over half enemy health even through Blast Protection

#### 3. **Shield Counter** - Anti-shield specialist
- **Focus**: Stun slam technique against shield users
- **Weapons**: Netherite Axe (shield stun) → Mace
- **Mobility**: Wind Charge, Ender Pearl, Firework Rocket
- **Armor**: Elytra ↔ Chestplate swapping
- **Technique**: Axe stuns shield mid-air, then mace strike (~4.5 hearts damage)

#### 4. **Hybrid** - Balanced for all situations
- **Focus**: Versatile loadout for any combat scenario
- **Weapons**: Mace + Axe/Sword swap options
- **Mobility**: Full mobility kit
- **Armor**: Elytra ↔ Chestplate swapping
- **Techniques**: All techniques enabled

### Automated Elytra Launch

Press **R** to execute a fully automated launch sequence:

1. **Look Up** (~85°) - Smooth camera tilt
2. **Jump** - Automatic jump
3. **Activate Elytra** - Start gliding
4. **Fire Rockets** - Uses up to 3 firework rockets
5. **Look Down** (~85°) - Aims for strike
6. **Auto-Switch** - Weapon swapper takes over for the kill

## Controls

| Key | Action | Description |
|-----|--------|-------------|
| `V` | Toggle Auto-Switch | Enable/disable automatic item switching |
| `]` | Next Profile | Cycle to next loadout profile |
| `[` | Previous Profile | Cycle to previous loadout profile |
| `R` | Elytra Launch | Execute automated launch sequence |
| `B` | Toggle Weapon Swap | Enable/disable sword/axe swapping technique |
| `N` | Toggle Armor Swap | Enable/disable elytra/chestplate swapping |
| `M` | Toggle Auto-Attack | Enable/disable automatic swinging to prevent fall damage |

*Keys can be rebound in Minecraft's Controls settings under "Own The Skies" category*

## How to Use

### Setup Your Hotbar
For optimal performance, have these items in your hotbar:

**Essential:**
- Mace (with Density 5 or Breach 4)
- Netherite Axe (preferred) or Netherite Sword
- Elytra (equipped in chest slot)
- Netherite Chestplate

**Mobility:**
- Wind Charges
- Firework Rockets
- Ender Pearls

**Optional:**
- Shield (for defense)
- Strength Potions (for Breach mace builds)

### Basic Combat Flow

1. **Launch**: Press `R` for automated launch or use wind charges/ender pearls manually
2. **Ascend**: Hold sword/axe while rising (weapon swap technique)
3. **Fall**: Mod automatically switches to mace at optimal moment
4. **Strike**: Land the hit for massive damage
5. **Repeat**: Mod switches back to mobility items automatically

### Advanced Techniques

#### Weapon Swapping (Manual Trigger)
- Hold Netherite Axe while falling
- Attack just before landing - mod switches to mace automatically
- Nearly doubles your damage output

#### Ender Pearl Catch
- Throw an Ender Pearl upward
- Throw a Wind Charge to catch the pearl and boost yourself
- Quick vertical ascent for mace strike

#### Backstab
- Boost up behind shielded opponent
- Attack from behind where shield doesn't protect
- Highly effective against defensive players

#### Spam Clicking Mode
- When falling extremely fast (2.5+ blocks/tick), spam click
- Registers damage per block even with low swing animation
- Useful when timing single hit is difficult

## Technical Details

### Damage Mechanics
- **Min Fall Speed for Auto-Switch**: 0.5 blocks/tick
- **Optimal Fall Speed**: 1.5 blocks/tick
- **Fast Fall (Spam Mode)**: 2.5+ blocks/tick
- **Switch Cooldown**: 100ms to prevent rapid toggling
- **Weapon Swap Timing**: 50ms before impact

### Enchantments Guide
- **Density 5**: More damage per block fallen (aerial combat)
- **Breach 4**: Ignores 60% of armor (ground combat with strength potion)
- **Wind Burst 3**: Launches you upward on successful hit (mobility)

### Combat Tips
- **Practice weapon swapping** - It's the most important technique
- Enable F3+B for hitboxes to improve accuracy
- Carry a shield to block enemy mace attacks
- Use Ender Pearls to escape when low on health
- Ender Pearl targeting: Aim for **lower half** of enemy body (legs/feet)

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/)
2. Install [Fabric API](https://modrinth.com/mod/fabric-api)
3. Place the OwnTheSkies mod JAR in your `.minecraft/mods` folder
4. Launch Minecraft 1.21.9+

## Building from Source

```bash
# Build the mod
./gradlew build

# Output JAR will be in build/libs/
```

## Compatibility

- **Minecraft Version**: 1.21.9+
- **Mod Loader**: Fabric
- **Required Dependencies**: Fabric API
- **Client-Side Only**: Yes

## Credits

Based on competitive mace PvP techniques and strategies. Created for players who want to master aerial mace combat!

---

**Remember: Practice makes perfect, and consistent use of weapon swapping is essential for competitive mace PvP!**

