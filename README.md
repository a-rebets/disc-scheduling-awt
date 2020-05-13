# Disk scheduling algorithms

## Cloning and running the project
To launch the latest release you can either first clone the repository

```sh
git clone https://github.com/pwr-os/disk-scheduling-team7.git
```
and navigate to the created folder after the clone is completed, or donwload the archive
file [`PROGRAM.jar`][jar downl] right away.

Then run the following command (a typical one):
```sh
java -jar PROGRAM.jar
```
## Basic structure
As was recommended in the task, the project is divided into 3 modules:
`InputModule`, `SimulationModule` and `OutputModule`.

Three input options are implemented:
  - preset (request sequence is stored in the code)
  - random (generated using the parameters requested from the user)
  - custom (the sequence itself is fully composed by the user)

All options **are error-protected** and work strictly in the "safe" range.

**GUI** is provided if the requests received from the input are within acceptable limits.

Source codes are contained in the `src/main` directory. Some simple documentation for the three modules mentioned above is given using the Java interfaces:

| File | Module |
| --- | --- |
| [InputModuleBackbone.java][inp back] | Input |
| [SimulationModuleBackbone.java][sim back] | Simulation |
| [OutputModuleBackbone.java][out back] | Output |
| [GraphBackbone.java][graph back] | Output*(additional) |

The main class which is chosen as "default" in the MANIFEST (receives the arguments from the console and integrates all modules) is `Main.java`

All the global constants, string messages and service/static methods are stored in `Misc.java` for the ease of access.

#### The program has been tested under various conditions and proved to be effective, modular and easy to maintain, while coping well with the main task - comparing the disk scheduling algorithms.

[inp back]: <https://github.com/pwr-os/disk-scheduling-team7/blob/master/src/main/InputModuleBackbone.java>
[sim back]: <https://github.com/pwr-os/disk-scheduling-team7/blob/master/src/main/SimulationModuleBackbone.java>
[out back]: <https://github.com/pwr-os/disk-scheduling-team7/blob/master/src/main/OutputModuleBackbone.java>
[graph back]: <https://github.com/pwr-os/disk-scheduling-team7/blob/master/src/main/GraphBackbone.java>
[jar downl]: <https://github.com/pwr-os/disk-scheduling-team7/raw/master/PROGRAM.jar>
