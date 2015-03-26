package io.shaka.jetty

import io.shaka.http.ContentType.{APPLICATION_JSON, TEXT_HTML, TEXT_PLAIN}
import io.shaka.http.Http.http
import io.shaka.http.HttpHeader.{CONTENT_TYPE, ACCEPT}
import io.shaka.http.Request.{POST, GET}
import io.shaka.http.Status
import io.shaka.http.Status.{ACCEPTED, OK}
import io.shaka.jetty.Handlers.Response
import io.shaka.jetty.Handlers.Response.okResponse
import org.eclipse.jetty.http.HttpStatus.ACCEPTED_202

class HandlerSpec extends JettySpec {

  jettyTest("Request has correct method") { jetty =>
    jetty.addHandler("/bob", (request) => {
      assert(request.method === "GET")
      okResponse
    })
    http(GET(s"http://localhost:${jetty.port}/bob/")) hasStatus OK
  }

  jettyTest("Request has correct url") { jetty =>
    jetty.addHandler("/bob", (request) => {
      assert(request.url === "/bob/")
      okResponse
    })
    http(GET(s"http://localhost:${jetty.port}/bob/")) hasStatus OK
  }

  jettyTest("Request has correct headers") { jetty =>
    jetty.addHandler("/bob", (request) => {
      assert(request.headers(ACCEPT.name) === List(TEXT_PLAIN.value, TEXT_HTML.value))
      okResponse
    })
    http(GET(s"http://localhost:${jetty.port}/bob/").header(ACCEPT, s"${TEXT_PLAIN.value}, ${TEXT_HTML.value}")) hasStatus OK
  }

  jettyTest("Request has correct entity") { jetty =>
    jetty.addHandler("/bob", (request) => {
      assert(request.entity.get === """{"hello":"world"}""".getBytes)
      okResponse
    })
    http(POST(s"http://localhost:${jetty.port}/bob/").entity("""{"hello":"world"}""")) hasStatus OK
  }

  jettyTest("Responds with the status ") { jetty =>
    jetty.addHandler("/bob", (request) => {
      Response(status = ACCEPTED_202)
    })
    http(GET(s"http://localhost:${jetty.port}/bob/")) hasStatus ACCEPTED
  }

  jettyTest("Responds with headers") { jetty =>
    jetty.addHandler("/bob", (request) => {
      Response(headers = Map(CONTENT_TYPE.name -> List(APPLICATION_JSON.value, TEXT_HTML.value)))
    })
    val response = http(GET(s"http://localhost:${jetty.port}/bob/")) hasStatus OK
    assert(response.header(CONTENT_TYPE) === Some(s"${APPLICATION_JSON.value},${TEXT_HTML.value}"))
  }

  jettyTest("Responds with entity") { jetty =>
    jetty.addHandler("/bob", (request) => {
      Response(entity = Some("<h1>Hello World</h1>".getBytes))
    })
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
