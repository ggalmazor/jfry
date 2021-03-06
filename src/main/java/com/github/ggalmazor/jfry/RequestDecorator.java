package com.github.ggalmazor.jfry;

import com.github.ggalmazor.jfry.Request;

import java.util.function.Function;

@FunctionalInterface
public interface RequestDecorator extends Function<Request, Request> {
  Request apply(Request request);
}
