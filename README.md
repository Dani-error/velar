<picture>
  <source media="(prefers-color-scheme: dark)" srcset="https://raw.githubusercontent.com/Dani-error/velar/refs/heads/master/.github/assets/logo-dark.svg">
  <source media="(prefers-color-scheme: light)" srcset="https://raw.githubusercontent.com/Dani-error/velar/refs/heads/master/.github/assets/logo-light.svg">
  <img alt="Fallback logo image" src="https://raw.githubusercontent.com/Dani-error/velar/refs/heads/master/.github/assets/logo-dark.svg">
</picture>

> Library ported and based from [@juliarn/npc-lib](https://github.com/juliarn/npc-lib) to Kotlin.

<br/>

## Features

- **Bukkit & Forks** (including Folia) supported via **ProtocolLib** or **PacketEvents*
- **Skin** (Static and Dynamic loading)
- **Attributes** (Status, Pose, Skin Layers)
- **Equipment** (Main & Off-Hand, Armor)
- **Interaction** (Interact & Attack)
- **Action Controller** (Automatic Looking at Player, Player Imitation & Spawning etc.)
- **Kotlin DSL & Type-safe**

---

## Usage

<details>
  <summary><strong>Maven</strong></summary>

  ```xml
  <repositories>
    <repository>
      <id>jitpack.io</id>
      <url>https://jitpack.io</url>
    </repository>
  </repositories>

  <dependency>
    <groupId>dev.dani.velar</groupId>
    <artifactId>MODULE</artifactId>
    <version>1.0.0-SNAPSHOT</version>
  </dependency>
  ```

  _Replace `MODULE` with one of the modules listed below._
</details>

<details>
  <summary><strong>Gradle (Kotlin DSL)</strong></summary>

  ```kotlin
  repositories {
      maven("https://jitpack.io")
  }

  dependencies {
      implementation("dev.dani.velar:<module>:1.0.0-SNAPSHOT")
  }
  ```

  _Replace `<module>` with one of the modules listed below._
</details>

<details>
  <summary><strong>Gradle (Groovy DSL)</strong></summary>

  ```groovy
  repositories {
      maven { url 'https://jitpack.io' }
  }

  dependencies {
      implementation 'dev.dani.velar:<module>:1.0.0-SNAPSHOT'
  }
  ```

  _Replace `<module>` with one of the modules listed below._
</details>
<br/>
<details>
  <summary><strong>Additional Repositories</strong></summary>

  You may need to add the following if you rely on transitive dependencies:
  - `https://repo.papermc.io/repository/maven-public/` (PaperLib)
  - `https://repository.derklaro.dev/releases/` (ProtocolLib via Derklaro’s repo; can also use JitPack)
  - `https://repo.codemc.io/repository/maven-releases/` (PacketEvents)
  - `https://s01.oss.sonatype.org/content/repositories/snapshots/` (for snapshot-only dependencies)
</details>

<details>
  <summary><strong>Shading</strong></summary>

  To avoid conflicts when multiple plugins ship the same dependencies, shade/relocate these packages:
  - `net.kyori`
  - `io.papermc.lib`
  - `io.leangen.geantyref`
  - `io.github.retrooper`
  - `com.github.retrooper`
  - `com.github.juliarn.npclib`
</details>


---

## Modules

| Module Name | Artifact ID | Description                                                                                           |
|-------------|-------------|-------------------------------------------------------------------------------------------------------|
| API         | `api`       | Core Velar API (no platform-specific code). |
| Common      | `common`    | Abstract API implementations for building new platforms.                     |
| Bukkit      | `bukkit`    | Full Bukkit (and forks) implementation—includes API & Common.    |

---

## Documentation

Full API reference and usage examples are available on the [docs site](https://github.com/Dani-error/velar/wiki).

## Contributing

Contributions are welcome! To get started:

1. Fork this repository.
2. Create a feature branch:
   ```bash
   git checkout -b feature/<your-feature>
   ```
3. Make your changes in Kotlin (follow existing style).
4. Commit and push to your fork, then open a Pull Request against `main`.

Please include a short description of your changes. For larger features, open an issue first to discuss.

---

## License

This project is MIT-licensed. See [LICENSE](./LICENSE) for details.  
(The original `@juliarn/npc-lib` is also MIT-licensed, and this port preserves that license.)  
