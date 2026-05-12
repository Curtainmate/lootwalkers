# Lootwalkers Prototype

A tiny native Android fitness RPG prototype that uses real phone steps to move through a dungeon.

## What it does

- Requests the Android activity recognition permission when needed.
- Reads `TYPE_STEP_COUNTER`, the phone's cumulative step sensor.
- Shows daily steps.
- Lets the player start Goblin Cave I.
- Uses steps for room travel and automatic sword attacks.
- Saves active dungeon progress locally.
- Opens a loot chest after the boss and upgrades starter gear.

## Build

For the local prototype APK, use the checked-in helper script:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\build-debug-apk.ps1
```

It creates a signed debug APK at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

The script pins the install compatibility metadata to `minSdk 26` and `targetSdk 35`.

Open this folder in Android Studio and build the `app` module.

From a terminal with Android Gradle tooling available:

```powershell
gradle assembleDebug
```

The Gradle debug APK will be created at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Notes for the future RPG version

This first version keeps everything deliberately simple. The next sensible additions are:

- a real step history store,
- XP gained from daily steps,
- quests or streaks,
- a character/progression screen,
- background-friendly sync strategy.
