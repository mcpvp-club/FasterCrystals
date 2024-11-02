# FasterCrystals
This plugin allows high ping players to crystal quickly by processing the left/right clicks *as the packets come in*.
This means that the server does not have to rely on the client's attack packet to destroy a crystal, and also does not have to rely on the client's block interact packet to place a crystal.
This is the main reason why high ping players cannot crystal quickly normally; the crystals take too long to get destroyed/placed on their clients.

The plugin will only function on Paper (or its compatible forks) and Folia servers.

## Building
`./gradlew :core:build`

The output file will be located in `./core/build/libs`.
