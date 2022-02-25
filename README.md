# Test Runner ![ci](https://github.com/martinunterhuber/test-runner/actions/workflows/build.yml/badge.svg)

TestRunner is a Java Application/GithubAction for running Junit test. It will only execute tests for "risky" classes. 

"Risky" classes are determined based on:

* **Software Metrics (CK)**: Computes the relative value of each metric compared to the maximum value inside the project. The relative values for each class are summed up.
* **Issues (PMD)**: Finds issues in the project and rates them 1 (least important) - 5 (most important). Sums up the ratings of all issues in a class.
* **Bugs (SpotBugs)**: Finds (potential) bugs in the project and rates them 1 (least important) - 20 (most important). Sums up the ratings of all bugs in a class.
* **History (Git)**:
  * Files often changed together share their risk
  * Newer files have a higher risk
  * Recently changed files have a higher risk
  * Files which are changed often have a higher risk
  * Files with a higher number of contributors have a higher risk

## How To Run

First compile the project which should be tested (currently Maven and Gradle are supported)

* Maven: ``mvn compile test-compile dependency:copy-dependencies``
* Gradle: ``gradle assemble compileTestJava``

Then run the following (replace the curly braces and their content):

``./gradlew doCustomTest --directory="{ Project Root Directory }" --packageName="{ Package Name }"``

## Use as Github Action

Inside your action first compile your project ([How to Run](#user-content-how-to-run))

Then use TestRunner like this:

```
- name: Run Tests
  uses: martinunterhuber/test-runner@master
  with:
    project-root: /home/runner/work/test-project/test-project/main/
    package-name: at.unterhuber.test
    github-token: ${{ secrets.GITHUB_TOKEN }}
```

You can find the full example [here](https://github.com/martinunterhuber/test-project/blob/master/.github/workflows/build.yml).

### Inputs

| **Input**        | **Required** | **Default**                | **Description**                                                                                                                                      |
|------------------|--------------|----------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------|
| ``project-root`` | Required     | N/A                        | The path to the root of your project inside the Github runner. Github's default for this is ``/home/runner/work/{ Project Name }/{ Project Name }/`` |
| ``package-name`` | Required     | N/A                        | The name of the package of your application                                                                                                          |
| ``github-token`` | Required     | N/A                        | `${{ secrets.GITHUB_TOKEN }}` can be used to access the Github Token in an action.                                                                   |
| ``script``       | Optional     | ``./gradlew doCustomTest`` | The script to run.                                                                                                                                   |

## Configuration

By default, TestRunner will use [this](https://github.com/martinunterhuber/test-runner/blob/master/test.properties) configuration. The configuration can be customized by adding a file called `test.properties` inside the root directory of the project. It should contain the following properties:


* `weight_{metric}`: The weight of a metric. This can be any positive double value.
* `filePropagationFactor`: Determines how much risk classes, which are often changed together, share. This must be value in the range [0, 1].
* `{metric/issue/bug}Threshold`: All classes with a value higher than the threshold will be considered risky. This can be any positive integer value.
