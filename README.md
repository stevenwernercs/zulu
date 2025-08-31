**Zulu**

- **Description:** Toy brain/neuronal growth sandbox plus an LWJGL window demo. The core model simulates neurons (dendrites, soma, axon, synapses) that grow within bounds and exchange neurotransmitters; a simple LWJGL/GLFW window is included for visualization scaffolding.
- **Languages:** Java 8+ (tested with JDK 11)
- **Build Tool:** Maven

**Intent**

- **Goal:** Explore a toy, self-organizing brain where neurons grow connections within spatial bounds and exchange neurotransmitters. Emphasis is on structure (dendrites, soma, axon, synapses) and emergent connectivity via simple local rules, not biological fidelity.
- **Core mechanics:**
  - Each neuron has a fixed soma point and a growing endpoint for each process (dendrite/synapse/axon) that “wanders” within `CoordinateBounds`.
  - Dendrites accumulate nearby neurotransmitters into an electric potential; the soma thresholds that potential to emit action potentials; axons/synapses deliver new transmitters.
  - Transmitters have lifetimes and decay; local transmitter presence influences growth wander.

**Current Behavior**

- Builds a small network (sensory, inter-neuron, motor), starts one thread per neuron.
- Each neuron loop: sample nearby transmitters at dendrites → sum potential → threshold at soma → propagate via axon → emit transmitters at synapses.
- Brain maintains a coordinate-indexed fluid of transmitters with decay; prints a textual “plot” and motor neuron output to the console.
- LWJGL 3D visualizer renders the live brain as colored points in 3D (soma, dendrites, axon tips, synapses, transmitters) and updates continuously.

**Project Layout**

- `render.Render`: Minimal LWJGL window; prints LWJGL version and opens a resizable gray window. Entry via `render.EngineTest`.
- `com.trifidearth.zulu.brain.Brain`: Creates a small network, starts neuron threads, and logs a textual “plot” of activity. Also contains a `main` for console output.
- `com.trifidearth.zulu.neuron.*`: Neuron model
  - `Neuron`: Owns `Soma`, `Axon`, `Dendrite`s, `Synapse`s; runs an update loop.
  - `Soma`: Integrates dendritic input (ElectricPotential) and may emit an `ActionPotential` when above threshold.
  - `Dendrite`: Samples nearby transmitters and contributes to soma potential; wanders/grows within bounds.
  - `Axon`: Propagates action potentials with a delay proportional to length; grows once from the soma.
  - `Synapse`: Emits random transmitters on action potentials; adapts its “wander” based on surroundings.
- `com.trifidearth.zulu.message.*`: Messages passed between nodes
  - `ElectricPotential`, `ActionPotential`.
  - `transmitter.*`: Neurotransmitters (`Dopamine`, `Gaba`, etc.) grouped in `Transmitters` with simple lifetime/decay.
- `com.trifidearth.zulu.coordinate.*`: Coordinates, pairs, and bounded growth helpers.
- `src/main/resources/logback.xml`: Logging configuration (SLF4J + Logback).

**Requirements**

- **JDK:** 11+ recommended
- **OS:** Linux is the default target (LWJGL natives configured as `natives-linux`). For macOS/Windows you’ll need to change the classifier in `pom.xml`.
- **Graphics:** An OpenGL-capable environment for the LWJGL window. On WSL2 you need a running X server/Wayland bridge and proper `DISPLAY`.

**Build**

- Using system Maven:
  - `mvn -DskipTests package`
- Without Maven installed (download to project-local `.tools/`):
  - `bash -c 'set -e; V=3.9.6; D=.tools; U=https://archive.apache.org/dist/maven/maven-3/$V/binaries/apache-maven-$V-bin.tar.gz; mkdir -p $D && cd $D && curl -fsSLO $U && tar -xzf apache-maven-$V-bin.tar.gz; ../.tools/apache-maven-$V/bin/mvn -Dmaven.repo.local=.m2 -DskipTests package'`

Artifacts:
- Jar: `target/zulu-1.0-SNAPSHOT.jar`
- Fat jar: `target/zulu-1.0-SNAPSHOT-all.jar` (runnable)

**Run**

- LWJGL window demo:
  - With system Maven: `mvn -q org.codehaus.mojo:exec-maven-plugin:3.1.0:java -Dexec.mainClass=render.EngineTest`
  - With local maven (downloaded above): `./.tools/apache-maven-3.9.6/bin/mvn -q -Dmaven.repo.local=.m2 org.codehaus.mojo:exec-maven-plugin:3.1.0:java -Dexec.mainClass=render.EngineTest`
- Console brain simulation (text output):
  - `mvn -q org.codehaus.mojo:exec-maven-plugin:3.1.0:java -Dexec.mainClass=com.trifidearth.zulu.brain.Brain`

- Fat jar (auto-selects host LWJGL natives via Maven OS profiles at build time):
  - `java -jar target/zulu-1.0-SNAPSHOT-all.jar`
  - Linux/WSL2: ensure an OpenGL-capable display (X11/Wayland) is available.
  - WSL: Joystick device warnings are suppressed; ensure an X server or Wayland bridge is running.
  - If the window appears black: this is expected initially; nodes are small points. The border box is now brighter; activity appears as colored points over time.
  - Cross-platform: the fat jar bundles core LWJGL natives for Linux/Windows/macOS so you can build once and run on any OS.

Run scripts (auto-build if jar missing):
- Linux: `scripts/run-linux.sh`
- WSL: `scripts/run-wsl.sh` (sets DISPLAY if unset; requires X/Wayland on Windows host)
- macOS: `scripts/run-mac.sh`
- Windows: `scripts/run-windows.bat`

Notes:
- A runnable fat jar (`-all.jar`) is produced via the Maven Shade plugin.

**Design Notes**

- **Update loop:** Each `Neuron` runs as a daemon thread, repeatedly aggregating dendritic inputs, potentially firing, and depositing transmitters into a shared fluid map keyed by coordinates.
- **Growth:** `Dendrite`/`Synapse` “wander” and grow within `CoordinateBounds`, using random deltas limited by a wander factor.
- **Messaging:** `Transmitters` carry a potential that decays to zero after a lifespan; dead messages are eventually removed.
- **Rendering:** 3D view with perspective projection and a simple camera. Draws colored points for node/transmitter types; updates every frame.

**Controls**

- Mouse: Right-drag to rotate the camera (orbit yaw/pitch).
- Scroll: Zoom in/out.
- Keyboard: `ESC` to quit; `R` to reset camera.

Log window
- A separate window titled “Brain Console” streams logs and Brain.print output so you can observe activity without an IDE console.

**Known Issues / Rough Edges**

- OpenGL/display required: ensure a working display (X11/Wayland/macOS/Windows). On WSL2, set `DISPLAY` and run an X server or Wayland bridge.
- Biological model is intentionally simplified; growth/firing rules are heuristic.

Fixes applied in this repo version:
- Added `Utils.getSecondOfMillis(long)` and removed the missing import issue.
- Corrected spelling: `potiential` → `potential` across packages, classes, and method names.
- Fixed math: `Coordinate` now sets `z` in constructor and uses x,y,z deltas for distance.
- Fixed logic: `Synapse.checkSurroundings` now counts non-zero potentials for `aliveNearby`.
- API casing: `Node.getFixedPoint()` uses conventional casing.
- Logging migrated to SLF4J + Logback with `logback.xml`.
- Auto OS natives selection via Maven profiles (Linux/Windows/macOS).

**Next Steps**

- Completed:
  - Make a fat jar (shaded `-all.jar`).
  - Wire rendering (window draws live brain nodes and transmitters).
  - Tests (coordinate math, bounds growth, transmitter decay).
  - Auto OS natives selection; logging migrated to SLF4J + Logback.

- Newly implemented:
  - Initial model refinements: bounded and smoother dendrite/synapse “wander” heuristics reacting to nearby activity.

- Still open:
  - Deeper model refinements (learning signals, better transmitter dynamics).
  - Richer rendering (connectors, activity indicators).

**TODO**

- Rendering: draw connectors (lines) between soma–axon–synapses and soma–dendrites; scale point sizes by activity.
- Input: allow user to seed sensory neurons or pause/resume growth via keyboard.
- Performance: move rendering to VBOs; reduce per-frame allocations; consider fixed timestep.
- Packaging: add OS-specific launch scripts; add macOS codesigning notes if distributing.
- Observability: expose metrics for neuron counts, firing rates, transmitter volume.

**License**

- No explicit license file is present. If you intend to open source this, add a suitable `LICENSE`.
