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

import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Optional;

/**
 * Does the heavy lifting for mapping URIs to byte streams.
 * 
 * @see InputStream
 * @see OutputStream
 */
/* default */ enum Scheme {
  FILE {
    @Override
    public InputStream getInputStream(URI uri) throws IOException {
      return Files.newInputStream(Paths.get(uri));
    }

    @Override
    public OutputStream getOutputStream(URI uri) throws IOException {
      return Files.newOutputStream(Paths.get(uri), StandardOpenOption.WRITE,
          StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
  },
  HTTP {
    @Override
    public InputStream getInputStream(URI uri) throws IOException {
      InputStream result = null;

      HttpURLConnection cn = (HttpURLConnection) uri.toURL().openConnection();
      try {
        cn.setRequestMethod("GET");
        cn.setDoInput(true);
        cn.setDoOutput(false);
        cn.connect();

        final InputStream input = cn.getInputStream();

        result = new FilterInputStream(input) {
          @Override
          public void close() throws IOException {
            try {
              super.close();
            } finally {
              cn.disconnect();
            }
          }
        };
      } finally {
        if (result == null)
          cn.disconnect();
      }

      return result;
    }

    @Override
    public OutputStream getOutputStream(URI uri) throws IOException {
      OutputStream result = null;

      HttpURLConnection cn = (HttpURLConnection) uri.toURL().openConnection();
      try {
        cn.setRequestMethod("PUT");
        cn.setDoInput(true);
        cn.setDoOutput(true);
        cn.connect();

        final OutputStream output = cn.getOutputStream();

        result = new FilterOutputStream(output) {
          @Override
          public void close() throws IOException {
            try {
              super.close();
            } finally {
              try {
                cn.getInputStream().close();
              } finally {
                cn.disconnect();
              }
            }
          }
        };
      } finally {
        if (result == null)
          cn.disconnect();
      }

      return result;
    }
  };

  private static final String FILE_SCHEME = "file";

  private static final String HTTP_SCHEME = "http";

  private static final String HTTPS_SCHEME = "https";

  public static Scheme fromUri(URI uri) {
    if (uri == null)
      throw new NullPointerException();
    Scheme result;
    switch (Optional.ofNullable(uri.getScheme()).orElse(FILE_SCHEME).toLowerCase()) {
      case FILE_SCHEME:
        result = Scheme.FILE;
        break;
      case HTTP_SCHEME:
      case HTTPS_SCHEME:
        result = Scheme.HTTP;
        break;
      default:
        throw new IllegalArgumentException("unrecognized scheme " + uri);
    };
    return result;
  }

  public abstract InputStream getInputStream(URI uri) throws IOException;

  public abstract OutputStream getOutputStream(URI uri) throws IOException;
}
