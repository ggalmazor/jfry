package com.github.ggalmazor.jfry;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import org.apache.commons.io.IOUtils;
import org.assertj.core.api.StrictAssertions;
import org.junit.After;
import org.junit.Test;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class JettyServerTest {
  private static final int PORT = 9999;
  private static final String BASE_URL = "http://localhost:" + PORT;
  private JFry jfry;

  private void startJFry(Route route) {
    JettyServer server = JettyServer.of(PORT);
    jfry = JFry.of(server)
        .register(route);

    jfry.start();
  }

  @After
  public void tearDown() throws Exception {
    jfry.stop();
  }

  @Test
  public void starts_a_Jetty_server_and_uses_it_to_serve_requests() throws Exception {
    startJFry(Route.get("/foo", request -> request.buildResponse().ok("bar")));

    HttpResponse<String> response = Unirest.get(BASE_URL + "/foo").asString();
    StrictAssertions.assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getCode());
    StrictAssertions.assertThat(response.getBody()).isEqualTo("bar");
  }

  @Test
  public void decodes_query_string_params() throws Exception {
    startJFry(Route.get("/foo", request -> request.buildResponse().ok(request.param("bar"))));

    HttpResponse<String> response = Unirest.get(BASE_URL + "/foo?bar=123").asString();
    StrictAssertions.assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getCode());
    StrictAssertions.assertThat(response.getBody()).isEqualTo("123");
  }

  @Test
  public void encodes_response_body_as_binary_data_if_contains_byte_array() throws Exception {
    byte[] expectedBytes = Files.readAllBytes(Paths.get(JettyServerTest.class.getResource("/image.jpeg").toURI()));
    startJFry(Route.get("/foo", request -> request.buildResponse().ok(expectedBytes)));
    HttpResponse<InputStream> response = Unirest.get(BASE_URL + "/foo").asBinary();
    byte[] actualBytes = IOUtils.toByteArray(response.getBody());
    assertThat(actualBytes).isEqualTo(expectedBytes);
  }

  @Test
  public void encodes_response_body_as_binary_data_if_contains_InputStream() throws Exception {
    InputStream is = JettyServerTest.class.getResourceAsStream("/image.jpeg");
    startJFry(Route.get("/foo", request -> request.buildResponse().ok(is)));
    HttpResponse<InputStream> response = Unirest.get(BASE_URL + "/foo").asBinary();
    byte[] actualBytes = IOUtils.toByteArray(response.getBody());
    byte[] expectedBytes = Files.readAllBytes(Paths.get(JettyServerTest.class.getResource("/image.jpeg").toURI()));
    assertThat(actualBytes).isEqualTo(expectedBytes);
  }

  @Test
  public void supports_PATCH_method() throws Exception {
    startJFry(Route.patch("/foo", request -> request.buildResponse().noContent()));
    HttpResponse<InputStream> response = Unirest.patch(BASE_URL + "/foo").asBinary();
    StrictAssertions.assertThat(response.getStatus()).isEqualTo(204);
  }
}
