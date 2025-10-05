# Spring Task List CLI

A simple command-line task manager built with Spring Boot.  
Demonstrates **dependency injection** and **swappable persistence layers**:
- **In-memory storage** (default)
- **File-backed storage** using a `|`-delimited CSV file.

---

## Features
- Add, update, complete, delete tasks
- List tasks in a formatted table
- Switch between **in-memory** and **file** storage without changing any code
- Uses Spring’s `@ConditionalOnProperty` to select the repository implementation

---

## Build

Package the application into a runnable JAR:

```bash
mvn -DskipTests clean package
```

Jar will be produced
```bash
target/spring-tasklist-cli-0.0.1-SNAPSHOT.jar
```
## Run
Memory Repository (default)

Tasks exist only for the life of the process:
```bash
java -jar target/spring-tasklist-cli-0.0.1-SNAPSHOT.jar --storage.type=mem
```

If you omit --storage.type, the app will default to memory:
```bash
java -jar target/spring-tasklist-cli-0.0.1-SNAPSHOT.jar --storage.type=file
```


## File Repository

Tasks are stored in a |-delimited CSV file and persist across restarts.

```bash
java -jar target/spring-tasklist-cli-0.0.1-SNAPSHOT.jar   --storage.type=file  -Dtodo.file=/path/to/tasks.csv
```

If no -Dtodo.file is specified, the default is tasks.csv in the working directory.

The repo will create the file if it doesn’t exist.

File format:

id|taskName|description|completed
1|Buy groceries|Pick up milk and eggs|true
2|Clean desk|Clear papers and organize supplies|false

## Repository Implementations
# MemoryTaskRepository

    Stores tasks in a ConcurrentHashMap
    Uses an AtomicLong counter for IDs
    Tasks are lost when the app exits
    Useful for testing and demos

# FileTaskRepository

    Reads/writes tasks from a CSV file
    Appends on add; rewrites on update/delete
    Seeds the ID sequence from the max ID in file
    Ensures persistence across runs


## Switching Repositories

The choice of storage is made at runtime:

```bash
--storage.type=mem → uses MemoryTaskRepository

--storage.type=file → uses FileTaskRepository
```

This demonstrates inversion of control: the rest of the application does not change when swapping persistence layers.
