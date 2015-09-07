# JFry [![Build Status](https://travis-ci.org/ggalmazor/jfry.svg?branch=master)](https://travis-ci.org/ggalmazor/jfry)

[![Join the chat at https://gitter.im/ggalmazor/jfry](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/ggalmazor/jfry?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
Java Fry is a web library inspired by Sinatra

## Getting started

This will improve with time. You just create a new instance with a server adapter and a port number. 

Then you can use JFry's DSL to register new Routes with their Handlers:
 
```java
JFry.of(new JettyAdapter(), 8080)
  .options("/foo", req -> req.buildResponse().ok("bar"))
  .get("/foo", req -> req.buildResponse().ok("bar"))
  .head("/foo", req -> req.buildResponse().ok("bar"))
  .post("/foo", req -> req.buildResponse().ok("bar"))
  .put("/foo", req -> req.buildResponse().ok("bar"))
  .delete("/foo", req -> req.buildResponse().ok("bar"))
  .trace("/foo", req -> req.buildResponse().ok("bar"))
  .connect("/foo", req -> req.buildResponse().ok("bar"))
  .start();
```

Now you can browse to [http://localhost:8080/foo](http://localhost:8080/foo)

## Why is JFry different?

JFry is strongly based on concepts related to Functional Programming: immutable objects, first-class functions, etc.

Probably the main difference between JFry and almost any other Java web library is that Route Handlers are defined as ```Request -> Response``` functions. This is a key design decision in order to benefit from functions' composition and decoration capabilities.
 
JFry doesn't make any assumption on the kind of use that you're going to give it. Therefore, the core artifact doesn't provide any templating engine, authentication method, json transformer, nothing, nada. Instead, it gives you an easy way to implement your own decorators to handle template rendering, input/output deserialization/serialization, authentication, session handling, etc.

# Decorators and function chains

A function decorator is a function that extends the functionality of other compatible functions. JFry defines for your convenience some [Functional Interfaces](https://github.com/ggalmazor/jfry/tree/master/core/src/main/java/org/jfry/decorators) to make easier for you the creation of decorators Requests and Responses. 

If you are using a simple ```Request -> Response``` Handler like so:
 
```java
jfry.get("/doge", request -> request.buildResponse.ok("wow"))
```

And you need to allow access only to authenticated users, you could do something like:
 
```java
// Providing that you have some authentication object like this one:

class Authenticator {
  public Handler authenticate(Handler handler) {
    return request -> {
      return request.mapHeader("X-Auth-Token", JWT::validate).orElse(false) 
               ? handler.apply(request) 
               : request.buildResponse().unathorized();
    }
  }
}

// Then you can do:

jfry.get("/doge", authenticator.authenticate(request -> request.buildResponse.ok("wow"))));
``` 

This mode of operation is based on a decorator that returns a new function that complies with a [Handler Functional Interface](https://github.com/ggalmazor/jfry/blob/master/core/src/main/java/org/jfry/Handler.java) and can be used as a Handler itself. This way, your initial Handler doesn't have to know anything about authentication and can be "clean" of dependencies and lines of code that don't have anything to do with your real business logic.

Sometimes, it's enough to manipulate the Request or the Response method. This is very useful when you need to deal with json content. Let's say that you need to receive a json object from your ursers and then return some other json to them. You could do:
 
```
// Providing that you have some json serialization/deserialization engine like this:

class JsonEngine {
  public RequestDecorator deserialize() {
    return request -> request.mapBody(Json::deserialize)
                             .map(request::withBody)
                             .orElse(request);
  }
  
  public ResponseDecorator serialize() {
    return response -> response.mapBody(Json::serialize)
                               .map(response::withBody)
                               .orElse(response);
  }
}

// Then you can do:

jfry.get("/doge", json.deserialize()
                      .andThen(request -> request.buildResponse.ok(request.<Doge>getBody()))
                      .andThen(json.serialize()));
```

RequestDecorator and ResponseDecorator are Functiontal Interfaces declared in [org.jfry.decorators package](https://github.com/ggalmazor/jfry/tree/master/core/src/main/java/org/jfry/decorators).

In this case, we're taking advantage of the implicit type casts that occur inside Request and Response objects when you call to mapBody() or getBody(). You need to remember that this operations are unsafe. JFry will always think that you know what you're doing :)
 
## Use the tests

You can learn a lot about JFry browsing through its tests