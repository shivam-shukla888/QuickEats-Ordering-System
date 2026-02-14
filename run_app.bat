@echo off
echo Starting QuickEats... > startup.log
echo JAVA_HOME=%JAVA_HOME% >> startup.log
echo Building project... >> startup.log
call mvn clean package -DskipTests >> startup.log 2>&1
echo Running application... >> startup.log
java -jar target/quickeats-0.0.1-SNAPSHOT.jar >> startup.log 2>&1
