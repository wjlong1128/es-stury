```shell
# 先运行一个容器
docker container cp esnew:/usr/share/elasticsearch/config .
docker run -d --name esnew -v D:/softwares/docker/es/data:/usr/share/elasticsearch/data  -v  D:/softwares/docker/es/plugins:/usr/share/elasticsearch/plugins -v D:/softwares/docker/es/config:/usr/share/elasticsearch/config -e "ES_JAVA_OPTS=-Xms512m -Xmx512m" -e "discovery.type=single-node"  --privileged  -p  9201:9200 -p 9301:9300 elasticsearch:8.6.2
docker exec -it esnew bash
# 重置密码
elasticsearch-reset-password -u elastic
```

配置文件或者
```java
 @Bean
    public ElasticsearchClient esRestClient() {
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
        // Create the low-level client
        RestClient restClient = RestClient.builder(new HttpHost(hostname, port)).setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)).build();
        // Create the transport with a Jackson mapper
        ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        // And create the API client
        return new ElasticsearchClient(transport);
    }
```

```yaml
spring:
  elasticsearch:
    username: elastic
    password: 7OilUkQfZTu+KNdxrOtj
    uris:
      - http://localhost:9201
```