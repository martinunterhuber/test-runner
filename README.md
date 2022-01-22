# Test Runner

## How To Run

First compile the project which should be tested (currently Maven and Gradle are supported)

* Maven: ``mvn compile test-compile dependency:copy-dependencies``
* Gradle: ``gradle assemble compileTestJava``

Then run:

``gradle doCustomTest --directory="{ Project Root Directory }" --packageName="{ Package Name }"``