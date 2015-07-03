package io.shaka.jetty

import io.shaka.http.ContentType.{APPLICATION_JSON, TEXT_HTML, TEXT_PLAIN}
import io.shaka.http.Http.http
import io.shaka.http.HttpHeader.{ACCEPT, CONTENT_TYPE}
import io.shaka.http.Request.{GET, POST}
import io.shaka.http.Response.respond
import io.shaka.http.Status.{ACCEPTED, OK}
import io.shaka.http.{Method, Response, Status}

class HandlerSpec extends JettySpec {

  jettyTest("Request has correct method") { jetty =>
    jetty.addHandler("/bob", (request) => {
      assert(request.method === Method.GET)
      Response.ok
    })
    http(GET(s"http://localhost:${jetty.port}/bob/")) hasStatus OK
  }

  jettyTest("Request has correct url") { jetty =>
    jetty.addHandler("/bob", (request) => {
      assert(request.url === "/bob/")
      Response.ok
    })
    http(GET(s"http://localhost:${jetty.port}/bob/")) hasStatus OK
  }

  jettyTest("Request has correct headers") { jetty =>
    jetty.addHandler("/bob", (request) => {
      assert(request.headers(ACCEPT) === List(TEXT_PLAIN.value, TEXT_HTML.value))
      Response.ok
    })
    http(GET(s"http://localhost:${jetty.port}/bob/").header(ACCEPT, s"${TEXT_PLAIN.value}, ${TEXT_HTML.value}")) hasStatus OK
  }

  jettyTest("Request has correct entity") { jetty =>
    jetty.addHandler("/bob", (request) => {
      assert(request.entityAsString === """{"hello":"world"}""")
      Response.ok
    })
    http(POST(s"http://localhost:${jetty.port}/bob/").entity( """{"hello":"world"}""")) hasStatus OK
  }

  jettyTest("Responds with the status ") { jetty =>
    jetty.addHandler("/bob", (request) => {
      Response(status = ACCEPTED)
    })
    http(GET(s"http://localhost:${jetty.port}/bob/")) hasStatus ACCEPTED
  }

  jettyTest("Responds with headers") { jetty =>
    jetty.addHandler("/bob", (request) => {
      val contentType = Response.ok.contentType(APPLICATION_JSON).contentType(TEXT_HTML)
      contentType

    })
    val response = http(GET(s"http://localhost:${jetty.port}/bob/")) hasStatus OK
    assert(response.header(CONTENT_TYPE) === Some(s"${TEXT_HTML.value},${APPLICATION_JSON.value}"))
  }

  jettyTest("Responds with entity") { jetty =>
    jetty.addHandler("/bob", (request) => {
      respond("<h1>Hello World</h1>")
    })
    val response = http(GET(s"http://localhost:${jetty.port}/bob/")) hasStatus OK
    assert(response.entityAsString === "<h1>Hello World</h1>")
  }

  jettyTest("Intercepts all requests handler") { jetty =>
    jetty.addHandler("/bob", (request) => {
      respond("<h1>Hello World</h1>")
    })
    jetty.addIntercept((requestResponse) => {
      requestResponse
        .contentType(TEXT_HTML)
        .status(OK)
        .entity("<h1>Bye World</h1>")
        .markHandled()
    }
    )
    val response = http(GET(s"http://localhost:${jetty.port}/bob/")) hasStatus OK
    assert(response.entityAsString === "<h1>Bye World</h1>")
  }

  jettyTest("Will pass through to nextHandler when not set to handled") { jetty =>
    jetty.addHandler("/bob", (request) => {
      respond("<h1>Hello World</h1>")
    })
    jetty.addIntercept((jettyResponseRequest) => ())
    val response = http(GET(s"http://localhost:${jetty.port}/bob/")) hasStatus OK
    assert(response.entityAsString === "<h1>Hello World</h1>")
  }

  implicit class ResponseTestPimps(response: io.shaka.http.Response) {
    def hasStatus(status: Status): io.shaka.http.Response = withClue(response) {
      assert(response.status === status)
      response
    }
  }

}
