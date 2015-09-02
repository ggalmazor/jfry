package org.jfry;

import javaslang.control.Option;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

class Request {
  private final HttpMethod method;
  private final String path;
  private final Map<String, String> headers;
  private final Map<String, String> query;
  private final Map<String, String> params;
  private final Supplier<String> bodySupplier;

  Request(HttpMethod method, String path, Map<String, String> headers, Map<String, String> query, Map<String, String> params, Supplier<String> bodySupplier) {
    this.method = method;
    this.path = path;
    this.params = params;
    this.headers = headers;
    this.bodySupplier = bodySupplier;
    this.query = query;
  }

  public static Request of(HttpMethod method, String path, Map<String, String> headers, Map<String, String> query, Supplier<String> bodySupplier) {
    return new Request(method, path, headers, query, new HashMap<>(), new MemoizingSupplier<>(bodySupplier));
  }

  Response buildResponse() {
    return Response.from(this);
  }

  String param(String name) {
    return Option
        .of(params.get(name))
        .orElseThrow(() -> new UnknownRequestParamException(name));
  }

  String getPath() {
    return path;
  }

  public HttpMethod getMethod() {
    return method;
  }

  Request withParams(Map<String, String> params) {
    return new Request(method, path, headers, query, params, bodySupplier);
  }

  public Map<String, String> getQuery() {
    return query;
  }

  /*
   * Copied from https://github.com/google/guava/blob/2b9d1761e6f913cbe044f1b80033e555e1500539/guava/src/com/google/common/base/Suppliers.java#L114
   */
  private static class MemoizingSupplier<T> implements Supplier<T> {

    private final Supplier<T> delegate;
    transient volatile private boolean initialized = false;
    transient private T value;

    private MemoizingSupplier(Supplier<T> delegate) {
      this.delegate = delegate;
    }

    @Override
    public T get() {
      if (value == null)
        synchronized (this) {
          if (!initialized) {
            T t = delegate.get();
            value = t;
            initialized = true;
            return t;
          }
        }
      return value;
    }

    @Override
    public String toString() {
      return "Suppliers.memoize(" + delegate + ")";
    }
  }

}
