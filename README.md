**Zulu**

- **Description:** Toy brain/neuronal growth sandbox plus an LWJGL window demo. The core model simulates neurons (dendrites, soma, axon, synapses) that grow within bounds and exchange neurotransmitters; a simple LWJGL/GLFW window is included for visualization scaffolding.
- **Languages:** Java 8+ (tested with JDK 11)
- **Build Tool:** Maven

**Project Layout**

- `render.Render`: Minimal LWJGL window; prints LWJGL version and opens a resizable gray window. Entry via `render.EngineTest`.
- `com.trifidearth.zulu.brain.Brain`: Creates a small network, starts neuron threads, and logs a textual “plot” of activity. Also contains a `main` for console output.
- `com.trifidearth.zulu.neuron.*`: Neuron model
  - `Neuron`: Owns `Soma`, `Axon`, `Dendrite`s, `Synapse`s; runs an update loop.
  - `Soma`: Integrates dendritic input (ElectricPotential) and may emit an `ActionPotiential` when above threshold.
  - `Dendrite`: Samples nearby transmitters and contributes to soma potential; wanders/grows within bounds.
  - `Axon`: Propagates action potentials with a delay proportional to length; grows once from the soma.
  - `Synapse`: Emits random transmitters on action potentials; adapts its “wander” based on surroundings.
- `com.trifidearth.zulu.message.*`: Messages passed between nodes
  - `ElectricPotiential`, `ActionPotiential` (note: spelling preserved from the original code).
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

**Run**

- LWJGL window demo:
  - With system Maven: `mvn -q org.codehaus.mojo:exec-maven-plugin:3.1.0:java -Dexec.mainClass=render.EngineTest`
  - With local maven (downloaded above): `./.tools/apache-maven-3.9.6/bin/mvn -q -Dmaven.repo.local=.m2 org.codehaus.mojo:exec-maven-plugin:3.1.0:java -Dexec.mainClass=render.EngineTest`
- Console brain simulation (text output):
  - `mvn -q org.codehaus.mojo:exec-maven-plugin:3.1.0:java -Dexec.mainClass=com.trifidearth.zulu.brain.Brain`

Notes:
- The jar produced by `package` is not “fat.” Running with `java -jar` will not work without the dependency classpath. Use the `exec-maven-plugin` commands above, or add a shading step (see Next Steps).

**Design Notes**

- **Update loop:** Each `Neuron` runs as a daemon thread, repeatedly aggregating dendritic inputs, potentially firing, and depositing transmitters into a shared fluid map keyed by coordinates.
- **Growth:** `Dendrite`/`Synapse` “wander” and grow within `CoordinateBounds`, using random deltas limited by a wander factor.
- **Messaging:** `Transmitters` carry a potential that decays to zero after a lifespan; dead messages are eventually removed.
- **Rendering:** The `Render` window currently acts as scaffolding; no brain rendering is wired yet.

**Known Issues / Rough Edges**

- **Missing utility:** `Transmitter` imports `com.trifidearth.zulu.utils.Utils` for time formatting; this class isn’t present. The code compiles when the package isn’t built, but adding the brain module to a runnable artifact will require a tiny `Utils` with `getSecondOfMillis(long)`.
- **Spelling:** “Potiential” is consistently misspelled across classes; left as-is to preserve history. Consider renaming for clarity.
- **Physics/math quirks:**
  - `Coordinate.computeDistanceTo` uses `z` twice (likely a typo; should include `y`).
  - `Synapse.checkSurroundings` sets `aliveNearby` using `countZeroPotentials()` instead of non-zero, probably incorrect.
  - `Node.getfixedPoint()` method name casing is inconsistent with Java conventions.
- **Logging:** Uses log4j 1.x, which is EOL. Consider updating to SLF4J + Logback or log4j2.
- **Platform-specific natives:** `pom.xml` pulls LWJGL `natives-linux`. Adjust `<lwjgl.natives>` for macOS/Windows if needed.

**Next Steps**

- **Make a fat jar:** Add the Maven Shade plugin to produce a runnable jar including LWJGL and natives.
- **Fix math/typos:** Correct distance math, naming, and `Synapse` logic; add small `Utils` helper.
- **Wire rendering:** Visualize the `Brain` state in `Render.loop()` instead of the static gray background.
- **Tests:** Add unit tests for coordinate math, growth boundaries, and transmitter decay.

**License**

- No explicit license file is present. If you intend to open source this, add a suitable `LICENSE`.

