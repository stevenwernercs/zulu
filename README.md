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
- LWJGL demo opens a gray window; no visual brain rendering is implemented yet.

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
- `src/main/resources/log4j.properties`: Logging configuration for log4j 1.x.

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

Notes:
- The jar produced by `package` is not “fat.” Running with `java -jar` will not work without the dependency classpath. Use the `exec-maven-plugin` commands above, or add a shading step (see Next Steps).

**Design Notes**

- **Update loop:** Each `Neuron` runs as a daemon thread, repeatedly aggregating dendritic inputs, potentially firing, and depositing transmitters into a shared fluid map keyed by coordinates.
- **Growth:** `Dendrite`/`Synapse` “wander” and grow within `CoordinateBounds`, using random deltas limited by a wander factor.
- **Messaging:** `Transmitters` carry a potential that decays to zero after a lifespan; dead messages are eventually removed.
- **Rendering:** The `Render` window currently acts as scaffolding; no brain rendering is wired yet.

**Known Issues / Rough Edges**

- **Logging:** Updated to SLF4J + Logback (console appender). Adjust `src/main/resources/logback.xml` to change levels.
- **Platform-specific natives:** `pom.xml` pulls LWJGL `natives-linux`. Adjust `<lwjgl.natives>` for macOS/Windows if needed.
  - Auto-selection added: Maven profiles set natives for Linux, Windows, or macOS at build time.

Fixes applied in this repo version:
- Added `Utils.getSecondOfMillis(long)` and removed the missing import issue.
- Corrected spelling: `potiential` → `potential` across packages, classes, and method names.
- Fixed math: `Coordinate` now sets `z` in constructor and uses x,y,z deltas for distance.
- Fixed logic: `Synapse.checkSurroundings` now counts non-zero potentials for `aliveNearby`.
- API casing: `Node.getFixedPoint()` uses conventional casing.
- **Logging:** Uses log4j 1.x, which is EOL. Consider updating to SLF4J + Logback or log4j2.
- **Platform-specific natives:** `pom.xml` pulls LWJGL `natives-linux`. Adjust `<lwjgl.natives>` for macOS/Windows if needed.

**Next Steps**

- **Make a fat jar:** Add the Maven Shade plugin to produce a runnable jar including LWJGL and natives.
- **Wire rendering:** Visualize the `Brain` state in `Render.loop()` instead of the static gray background.
- **Model refinements:** Tune growth heuristics, transmitter dynamics, and add simple learning signals.
- **Tests:** Add unit tests for coordinate math, growth boundaries, and transmitter decay.

**TODO**

- Rendering: draw connectors (lines) between soma–axon–synapses and soma–dendrites; scale point sizes by activity.
- Input: allow user to seed sensory neurons or pause/resume growth via keyboard.
- Performance: move rendering to VBOs; reduce per-frame allocations; consider fixed timestep.
- Packaging: add OS-specific launch scripts; add macOS codesigning notes if distributing.
- Observability: expose metrics for neuron counts, firing rates, transmitter volume.

**License**

- No explicit license file is present. If you intend to open source this, add a suitable `LICENSE`.
