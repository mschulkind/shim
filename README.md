## Open mHealth Shim DSU

### Technical Requirements

This DSU is an extension of the [DSU Reference Implementation](https://github.com/openmhealth/developer). It is written in Java using Java 7. It runs inside a Java Servlet 3.0 container and MySQL or MongoDB may be used as its database.

For installing and running the DSU, see the [Getting Started Guide](https://github.com/openmhealth/developer/wiki/DSU-Reference-Implementation:-Getting-Started).


### About

This DSU extends the functionality of the DSU Reference Implementation to allow data from third-party data sources to be read in an Open mHealth conformant manner. 

A third-party data source is represented by a Shim that acts as an OAuth client and converts the third party data into data that is represented by a Concordia schema.

In order to run inside this application, Shims must be written to the Shim API.

1. The [Shim](https://github.com/openmhealth/shim/blob/master/dsu/src/org/openmhealth/shim/Shim.java) interface must be implemented by a concrete Java class.
2. The [ShimRegistry](https://github.com/openmhealth/shim/blob/master/dsu/src/org/openmhealth/shim/ShimRegistry.java) abstract class must be extended by a concrete Java class. 
3. In the deployment descriptor (web.xml), the Shim must be added as a ServletContextListener. This causes the Shim to be loaded into the DSU when on application startup.
 
For deployment,

1. OAuth client IDs and secrets should be read from configuration files in order for multiple Shim DSU instances to coexist. A given shim may also have other configurable properties that should be present in a configuration file.
2. Any required libraries must be deployed into the Servlet Container's WEB-INF/lib directory.
3. The Shim itself must also be present in the lib directory.

### Shims

* [2Net](https://github.com/openmhealth/omh-shim-2net)
* [BlueButton Plus](https://github.com/openmhealth/omh-shim-blue_button_plus)
* [FatSecret](https://github.com/openmhealth/omh-shim-fatsecret)
* [FitBit](https://github.com/openmhealth/omh-shim-fit_bit)
* [Moves](https://github.com/openmhealth/omh-shim-moves)
* [RunKeeper](https://github.com/openmhealth/omh-shim-run_keeper)
* [Withings](https://github.com/openmhealth/omh-shim-withings)

