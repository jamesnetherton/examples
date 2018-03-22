### Deploy

```
mvn clean package
cp target/jaxrs-resource-inject.war $WILDFLY_HOME/standalone/deployments
```

### Test

```
curl -v localhost:8080/jaxrs-resource-inject/rest
```
