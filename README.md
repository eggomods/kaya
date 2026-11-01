#   Kaya

Kaya (shortened form of 'kakayahan', which is the tagalog translation for 'capability'/'ability') is a library that allows attaching "powers" to entities via data packs. This is a rewrite of the [Apoli](https://github.com/apace100/apoli) project by Apace.


##  Features
- Vanilla-esque context system which allows for certain types to interconnect with each other.
- Data-driven powers which gives an entity a certain "ability" when granted. These provide the context needed by other types to function.
- Actions and conditions that uses the context system to affect and check the state of the objects of a world respectively.
- Value providers of various types that uses the context system to provide a dynamic value, like providing the attribute value of an entity for a field.


##  Usage

### For players/mod pack authors

You don't have to install this library as most if not all dependents will include or require it, but this library will work fine as a standalone mod.


###  For developers

<ul>
<li>
<details>
<summary><b>Using Modrinth Maven</b></summary>

```groovy
repositories {
    exclusiveContent {
        forRepository {
            maven {
                name = "Modrinth"
                url = "https://api.modrinth.com/maven"
            }
        }
        filter {
            includeGroup "maven.modrinth"
        }
    }
    maven {
        name = "QuiltMC"
        url = "https://maven.quiltmc.org/repository/release"
    }
    maven {
        name = "Xander Maven"
        url = "https://maven.isxander.dev/releases"
    }
    maven {
        name = "Terraformers"
        url = "https://maven.terraformersmc.com/"
    }
}

dependencies {
    //  Remove 'include' if you don't want to JiJ the library
    include(modImplementation("maven.modrinth:kaya:<version>"))
}
```

</details>
</li>
<li>
<details>
<summary><b>Using local maven / as a local dependency</b></summary>

You can clone this repository and run `./gradlew publishToMavenLocal`. It will publish the artifacts to the local maven and under the `io.github.eggohito` group:

```groovy
repositories {
    mavenLocal()
    maven {
        name = "QuiltMC"
        url = "https://maven.quiltmc.org/repository/release"
    }
    maven {
        name = "Xander Maven"
        url = "https://maven.isxander.dev/releases"
    }
    maven {
        name = "Terraformers"
        url = "https://maven.terraformersmc.com/"
    }
}

dependencies {
    //  Remove 'include' if you don't want to JiJ the library
    include(modImplementation("io.github.eggohito:kaya:<version>"))
}
```

</details>
</li>
</ul>


##  Contributing

This project uses JDK 25 and Fabric Loom 0.18-SNAPSHOT. You can generate artifacts using `./gradlew build` and run the client or server with `./gradlew runClient` or `./gradlew runServer` for testing. The client and server will use the `run/client` and `run/server` directories respectively.
