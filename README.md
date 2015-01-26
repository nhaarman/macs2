# Build and run:

    ./gradlew build
    java -jar build/libs/sokoban.jar screens/screen.125

# Run tests:

    ./gradlew test


# Troubleshooting

    Exception in thread "main" java.lang.UnsupportedClassVersionError: sokoban/Sokoban : Unsupported major.minor version 52.0

Sokoban has been built using Java8. Make sure you're using Java8 to run the program.