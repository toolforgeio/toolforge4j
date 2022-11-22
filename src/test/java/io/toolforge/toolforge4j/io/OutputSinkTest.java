/*-
 * =================================LICENSE_START==================================
 * toolforge4j
 * ====================================SECTION=====================================
 * Copyright (C) 2022 ToolForge
 * ====================================SECTION=====================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ==================================LICENSE_END===================================
 */
package io.toolforge.toolforge4j.io;

import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import org.junit.Test;
import com.google.common.io.ByteStreams;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

public class OutputSinkTest {
  private static final String FILE_SCHEME_PREFIX = "file:";

  @Test
  public void fileTest() throws IOException {
    String expected = "Hello, world!";

    String observed;
    File file = File.createTempFile("test.", ".txt");
    try {
      URI uri = file.toURI();

      assertThat(uri.toString(), startsWith(FILE_SCHEME_PREFIX));

      OutputSink unit = new OutputSink(uri);
      try (OutputStream out = unit.getOutputStream()) {
        out.write(expected.getBytes(StandardCharsets.UTF_8));
      }

      try (InputStream in = new FileInputStream(file)) {
        observed = new String(ByteStreams.toByteArray(in), StandardCharsets.UTF_8);
      }
    } finally {
      file.delete();
    }

    assertThat(observed, is(expected));
  }

  @Test
  public void httpTest() throws Exception {
    String expected = "Hello, world!";

    String observed;
    try (MockWebServer server = new MockWebServer()) {
      server.enqueue(new MockResponse().setResponseCode(HttpURLConnection.HTTP_NO_CONTENT));
      server.start();

      URI uri = URI.create(
          format("http://%s:%s/o/abcdefgh/12345678", server.getHostName(), server.getPort()));

      OutputSink unit = new OutputSink(uri);
      try (OutputStream out = unit.getOutputStream()) {
        out.write(expected.getBytes(StandardCharsets.UTF_8));
      }

      observed = server.takeRequest().getBody().readUtf8();
    }

    assertThat(observed, is(expected));
  }

  @Test
  public void fromStringTest() throws IOException {
    String expected = "Hello, world!";

    String observed;
    File file = File.createTempFile("test.", ".txt");
    try {
      OutputSink unit = OutputSink.fromString(file.toURI().toString());
      try (OutputStream out = unit.getOutputStream()) {
        out.write(expected.getBytes(StandardCharsets.UTF_8));
      }

      try (InputStream in = new FileInputStream(file)) {
        observed = new String(ByteStreams.toByteArray(in), StandardCharsets.UTF_8);
      }
    } finally {
      file.delete();
    }

    assertThat(observed, is(expected));
  }

  @Test
  public void getReaderTest() throws IOException {
    String expected = "Hello, world!";

    String observed;
    File file = File.createTempFile("test.", ".txt");
    try {
      OutputSink unit = OutputSink.fromString(file.toURI().toString());
      try (Writer w = unit.getWriter(StandardCharsets.UTF_8)) {
        w.write(expected);
      }

      try (InputStream in = new FileInputStream(file)) {
        observed = new String(ByteStreams.toByteArray(in), StandardCharsets.UTF_8);
      }
    } finally {
      file.delete();
    }

    assertThat(observed, is(expected));
  }
}
