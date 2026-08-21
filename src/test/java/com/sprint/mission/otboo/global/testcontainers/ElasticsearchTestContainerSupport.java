package com.sprint.mission.otboo.global.testcontainers;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * ES 통합 테스트용 컨테이너.
 *
 * <p>{@code RedisTestContainerSupport}와 달리 인터페이스가 아닌 추상 클래스다.
 * ES 테스트는 Spring 컨텍스트를 띄우므로 컨테이너 주소를 프로퍼티로 주입해야 하는데, {@code @DynamicPropertySource}는 static 메서드에만
 * 붙일 수 있어 인터페이스에 둘 수 없다.
 *
 * <p>Nori 플러그인이 필요해 {@code docker/elasticsearch} 이미지를 쓴다.
 * 테스트 전에 아래를 한 번 실행해야 한다.
 * <pre>docker build -t otboo-es:9.4.2 docker/elasticsearch</pre>
 */
public abstract class ElasticsearchTestContainerSupport {

  private static final DockerImageName IMAGE = DockerImageName
      .parse("otboo-es:9.4.2")
      .asCompatibleSubstituteFor("docker.elastic.co/elasticsearch/elasticsearch");
  
  protected static final ElasticsearchContainer ES_CONTAINER = createStartedContainer();

  private static ElasticsearchContainer createStartedContainer() {
    ElasticsearchContainer container = new ElasticsearchContainer(IMAGE)
        .withEnv("xpack.security.enabled", "false")
        .withEnv("ES_JAVA_OPTS", "-Xms512m -Xmx512m");
    container.start();
    return container;
  }

  @DynamicPropertySource
  static void registerProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.elasticsearch.uris", ES_CONTAINER::getHttpHostAddress);
  }
}
