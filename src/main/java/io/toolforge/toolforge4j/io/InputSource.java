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
import java.io.InputStream;
import java.io.Reader;
import java.net.URI;
import java.nio.charset.Charset;
import com.sigpwned.chardet4j.Chardet;

public class InputSource {
  public static InputSource fromString(String s) {
    return new InputSource(URI.create(s));
  }

  private final URI uri;

  public InputSource(URI uri) {
    this.uri = requireNonNull(uri);
  }

  public InputStream getInputStream() throws IOException {
    return getScheme().getInputStream(uri);
  }

  public Reader getReader(Charset defaultCharset) throws IOException {
    return Chardet.decode(getInputStream(), defaultCharset);
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
