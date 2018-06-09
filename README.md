# AWS p2 Maven Plugin (aws-p2-maven-plugin)

[![Build Status](https://travis-ci.org/avojak/aws-p2-maven-plugin.svg?branch=master)](https://travis-ci.org/avojak/aws-p2-maven-plugin) 
[![Coverage Status](https://coveralls.io/repos/github/avojak/aws-p2-maven-plugin/badge.svg?branch=master)](https://coveralls.io/github/avojak/aws-p2-maven-plugin?branch=master) 
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/223a3bc2eac54dca90a3f96c8b853cf4)](https://www.codacy.com/app/avojak/aws-p2-maven-plugin?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=avojak/aws-p2-maven-plugin&amp;utm_campaign=Badge_Grade) 
[![Known Vulnerabilities](https://snyk.io/test/github/avojak/aws-p2-maven-plugin/badge.svg)](https://snyk.io/test/github/avojak/aws-p2-maven-plugin)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
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
$ mvn clean install
```

## Usage

Add the plugin to the `build` section of the `pom.xml`:

```xml
<plugin>
    <groupId>com.avojak.mojo</groupId>
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

The plugin will run automatically during the `deploy` phase.

Additionally, you may add a `pluginGroup` entry to the Maven settings file (User: `${user.home}/.m2/settings.xml`, Global: `${maven.home}/conf/settings.xml`), which will allow the use of `aws-p2:*` command line goals interactively in all projects:
```xml
<pluginGroups>
    <pluginGroup>com.avojak.mojo</pluginGroup>
</pluginGroups>
```

For example, `$ mvn aws-p2:deploy -Daws-p2.bucket=p2.example.com`.

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
| generateLandingPage | `boolean` | No | 1.0 | Whether or not to generate an HTML landing page. This page will be displayed if a user attempts to access the update site via a web browser. No external sources are used in this page, so CORS configuration is not necessary. An example landing page can be found here: [p2.avojak.com](http://p2.avojak.com/snapshot/example-eclipse-plugin.site/1.0.0-SNAPSHOT/)<br>**Default value is:** `true`<br>**User property is:** `aws-p2.generateLandingPage` |
| skip | `boolean` | No | 1.0 | Set to `true` to skip plugin execution.<br>**Default value is:** `false`<br>**User property is:** `aws-p2.skip` |
| targetSiteDirectory | `String` | No | 1.0 | The directory within the bucket to place the update site.<br>**Default value is:** `${project.name}/${project.version}`<br>**User property is:** `aws-p2.targetSiteDirectory` |

### Authentication

The AWS p2 Maven Plugin uses the Default Credential Provider Chain for authentication. See 
[Working with AWS Credentials](http://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/credentials.html) for details 
on all supported methods of providing credentials.

## S3 Bucket Configuration

In order to host a static p2 update site on an S3 bucket, you will need to ensure several bucket settings:

1. In the bucket properties tab, "Static website hosting" must be enabled
2. In the bucket permissions tab, "Public access" to list bucket contents must be enabled

It is up to the consumer to configure the AWS account used for deployments, although we recommend creating an account whose sole responsibility is deploying to this bucket. Only the minimum required permissions should be granted to this user.

If versioning is enabled on the bucket, not all versions are deleted during an overwrite. When a new site is uploaded, only current content is deleted, and delete markers are not touched.

## Built With

* [Maven](https://maven.apache.org/) - Dependency Management
* [AWS SDK for Java](https://aws.amazon.com/sdk-for-java/) - Used to interact with AWS S3 buckets
* [Travis CI](https://travis-ci.org) - Continuous Integration and Deployment
* [JaCoCo](http://www.eclemma.org/jacoco/) - Code Coverage

## Versioning

We use [SemVer](http://semver.org/) for versioning. For the versions available, see the [tags on this repository](https://github.com/avojak/aws-p2-maven-plugin/tags). 

## License

This project is licensed under the Apache License, Version 2.0 - see the [LICENSE](LICENSE) file for details
