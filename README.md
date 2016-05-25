Mod UHC
==========

Mod based on the work from Azenet. For Forge **1.9.4**.

Use & commands
==========

Start new UHC :

```
/uhcgame start
```

Pour sauter d'un épisode :
```
/uhcgame shift
```

Pour créer un nouveau point de spawn :
```
/uhcgame addgamespawn
```

Pour modifier le spawn du monde :
```
/uhcgame setspawn
```

Pour modifier la taille de la map :
```
/uhcgame setsize
```

Pour génerer les murs :
```
/uhcgame generatewalls
```

Pour créer les teams :
```
/uhcgame addteam
```

Dev
=======
Step 1: setup & decompile Minecraft source (go grab a coffee).
```
Windows: "gradlew setupDecompWorkspace"
Linux/Mac OS: "./gradlew setupDecompWorkspace"
```

For eclipse, run "gradlew eclipse" (./gradlew eclipse if you are on Mac/Linux) and switch your workspace to /eclipse/.

If you preffer to use IntelliJ, steps are a little different.
1. Open IDEA, and import project.
2. Select your build.gradle file and have it import.
3. Once it's finished you must close IntelliJ and run the following command:

"gradlew genIntellijRuns" (./gradlew genIntellijRuns if you are on Mac/Linux)

License
=======

For MinecraftForge and Paulscode refer to the license in the Forge MDK.

GPLv3. Refer to LICENSE.
