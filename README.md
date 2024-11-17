# staple
Space Traders Automated PLanning Engine

SWENG 894 Capstone Project

This is a quarkus project that is compiled into a native executable.

### Compiling the Project

Ensure that you have GraalVM and VisualStudio downloaded (follow this documentation: https://quarkus.io/guides/building-native-image)

Run the `java/buildExecutable.sh` script

The executable will be called StapleApplication-runner.exe and will be located in the `java/build` directory.

### Running the project

Create a `.env` file in the `java` folder. Include your API token in it in this form:

QUARKUS_DATASOURCE_SPACETRADERS_API_KEY=yourTokenHere

Run the `runExecutable.sh` script, it will automatically set the environment variables and then run the executable.

### Running the Test Driver

Running the application with the test drivers results in the app not reaching out to the space traders API, and will use local variants of each rest client.

To run using the test driver, run this command from the `java` directory:

quarkus dev -Dquarkus.profile=test-driver
