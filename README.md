# AWS P2 Maven Plugin (aws-p2-maven-plugin)

[![Build Status](https://travis-ci.org/avojak/aws-p2-maven-plugin.svg?branch=master)](https://travis-ci.org/avojak/aws-p2-maven-plugin) 
[![Coverage Status](https://coveralls.io/repos/github/avojak/aws-p2-maven-plugin/badge.svg?branch=initial-impl)](https://coveralls.io/github/avojak/aws-p2-maven-plugin?branch=initial-impl) 
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](https://opensource.org/licenses/EPL-1.0) 
![Version](https://img.shields.io/badge/version-1.0--SNAPSHOT-yellow.svg)

A Maven plugin for deploying a [p2](https://www.eclipse.org/equinox/p2/) update site to an [AWS S3](https://aws.amazon.com/s3/) bucket.

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes.

### Prerequisites

Java 1.7+

Maven 3.1.0+

### Installation

To build the plugin locally, simply run the following:

```
mvn clean install
```

## Usage

Add the plugin to the `build` section of the `pom.xml`:

```xml
<plugin>
    <groupId>com.thedesertmonk.mojo</groupId>
    <artifactId>aws-p2-maven-plugin</artifactId>
    <version>1.0-SNAPSHOT</version>
    <configuration>
        <bucket>p2.example.com</bucket>
    </configuration>
    <executions>
        <execution>
            <goals>
                <goal>deploy</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

### Goals

The following goals are available:

| Goal | Description |
|:---|:---|
| deploy | Deploys the p2 update site |
| help | Display help information on the aws-p2-maven-plugin |

### Configuration

The following parameters can be set in the configuration:

| Name | Type | Required | Since | Description |
|:---|:---|:---|:---|:---|
| bucket | `String` | Yes | 1.0 | The name of the S3 bucket where the update site is hosted.<br>**User property is:** `aws-p2.bucket` |
| dedicatedBuckets | `boolean` | No | 1.0 | Whether or not dedicated (i.e. separate) buckets are used for snapshot and release deployments. When set to `false`, the update site will automatically be placed within a `snapshot` or `release` directory at the top level for snapshot and release deployments respectively.<br>**Default value is:** `false`<br>**User property is:** `aws-p2.dedicatedBuckets` |
| deploySnapshots | `boolean` | No | 1.0 | Whether or not to deploy snapshot versions.<br>**Default value is:** `true`<br>**User property is:** `aws-p2.deploySnapshots` |
| skip | `boolean` | No | 1.0 | Set to `true` to skip plugin execution.<br>**Default value is:** `false`<br>**User property is:** `aws-p2.skip` |
| targetSiteDirectory | `String` | No | 1.0 | The directory within the bucket to place the update site.<br>**Default value is:** `${project.name}/${project.version}`<br>**User property is:** `aws-p2.targetSiteDirectory` |

## Built With

* [Maven](https://maven.apache.org/) - Dependency Management
* [AWS SDK for Java](https://aws.amazon.com/sdk-for-java/) - Used to interact with AWS S3 buckets

## Versioning

We use [SemVer](http://semver.org/) for versioning. For the versions available, see the [tags on this repository](https://github.com/avojak/aws-p2-maven-plugin/tags). 

## License

This project is licensed under the Apache License, Version 2.0 - see the [LICENSE.md](LICENSE.md) file for details