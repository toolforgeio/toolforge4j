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

import static java.util.Objects.requireNonNull;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.nio.charset.Charset;

public class OutputSink {
  public static InputSource fromString(String s) {
    return new InputSource(URI.create(s));
  }

  private final URI uri;

  public OutputSink(URI uri) {
    this.uri = requireNonNull(uri);
  }

  public OutputStream getOutputStream() throws IOException {
    return getScheme().getOutputStream(getUri());
  }

  public Writer getWriter(Charset charset) throws IOException {
    return new OutputStreamWriter(getOutputStream(), charset);
  }

  /**
   * @return the uri
   */
  private URI getUri() {
    return uri;
  }

  private Scheme getScheme() throws IOException {
    Scheme result;
    try {
      result = Scheme.fromUri(getUri());
    } catch (IllegalArgumentException e) {
      throw new IOException("unrecognized scheme " + getUri());
    }
    return result;
  }
}
