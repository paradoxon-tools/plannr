# Local Build and Deployment Instructions

These instructions outline the steps required to manage your project's artifacts and interact with Docker, especially for locally hosted projects.  

**Please note the majority of these steps are necessary only when your Build Server is unreachable**.    

If you are publishing locally, most of these variables can be left empty.

## Artifactory Artifact Publishing

#### Maven Local

With the **publishToMavenLocal** gradle task you are able to test artifacts produced locally by the project in other local projects.  
This is particularly useful when evaluating changes without pushing them to the remote repository.  
To do that you have to do the following:
1. run **./gradlew build**
2. run **./gradlew publishToMavenLocal**
3. Take note of the version configured in **gradle.properties**
4. Configure mavenLocal as a source to the build:

> **old projects:**  
> - add 'mavenLocal()' to the repositories.gradle  

> **new projects:**  
> - Either add **includeMavenLocal=true** to your global gradle config in **~/.gradle/gradle.properties**  
> - Or add a **repositories.properties** file to the root of the project with **includeMavenLocal=true** as its content 

To push project artifacts to our Artifactory, use the Gradle **publish** task. This process ensures that our project's build artifacts are stored and versioned appropriately.
This should not be done manually. The possibility is only presented to circumvent problems with our build server.

## Docker and AWS Configuration
#### Required Software
- Docker [Install Instructions](https://www.docker.com/products/docker-desktop/)
- Python
- AWS CLI - Install using the guide: [AWS CLI Installation](https://docs.aws.amazon.com/cli/v1/userguide/install-windows.html#awscli-install-windows-path)

#### AWS Configuration
- Use the **aws configure** command and provide the secrets when prompted.
- Acquire a one-time AWS ECR password by running **aws ecr get-login-password --region eu-central-1** in your console.

## Gradle Configuration
Variables to be configured in ~/.gradle/gradle.properties:
- **dericonArtifactoryUser**
- **dericonArtifactoryPassword**
- **dericonDockerRegistryUrl**
- **dericonDockerRepository**
- **dericonDockerRegistryUsername**
- **dericonDockerRegistryPassword** (The password generated from the ECR login step)

These variables are essential in allowing your build to interact with Docker registry and Artifactory.

After configuring these you will be able to execute the ```pushImage```task which publishes the Dockerfile to ECR.