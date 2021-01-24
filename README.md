# jmh-xap
Temporary repo for JMH tests

# Configuration options

Refer to: `bin/start-env.sh`

- `GS_HOME` - path to product directory
- `GS_LOOKUP_LOCATORS` - host ip address of manager 

# How to run?
Refer to: `bin` directory

1. ./start-managers.sh &
2. ./start-gscs.sh
3. ./start-partitions.sh
4. mvn compile
5. mvn test
