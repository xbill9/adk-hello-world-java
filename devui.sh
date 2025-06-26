. $HOME/adk-hello-world-java/set_env.sh


echo `pwd`
echo running Dev UI
echo http://127.0.0.1:8080

mvn exec:java \
    -Dexec.mainClass="com.google.adk.web.AdkWebServer" \
    -Dexec.args="--adk.agents.source-dir=src/main/java" \
    -Dexec.classpathScope="compile"


