package openai

import cats.effect.*
import cats.effect.std.*
import cats.syntax.all.*
import sttp.client4.*
import sttp.client4.httpclient.cats.HttpClientCatsBackend
import sttp.openai.OpenAI
import sttp.openai.OpenAIExceptions.OpenAIException
import sttp.openai.requests.completions.chat.ChatRequestBody.{ChatBody, ChatCompletionModel}
import sttp.openai.requests.completions.chat.ChatRequestResponseData.ChatResponse
import sttp.openai.requests.completions.chat.{Message, Role}
import unindent.*

import javax.crypto.SecretKey

class GptService(backend: Backend[IO], secretKey: String):
  private val openAI = OpenAI(secretKey)

  def send(prompt: List[String]): IO[String] =
    val messages = prompt.map(content => Message(Role.User, content))
    openAI
      .createChatCompletion(ChatBody(model = ChatCompletionModel.GPT35Turbo, messages = messages))
      .send(backend)
      .map(_.body)
      .rethrow[ChatResponse]
      .map(_.choices.head.message.content)

  def summarize(content: String): IO[String] =
    send(List("Please summarize the following text:", content.takeWords(500)))

  def rewrite(content: String): IO[String] =
    send(List("Please rewrite the following text for readability:", content.takeWords(500)))

  def translate(code: String, lang: String): IO[String] =
    send(List(s"Here is a snippet of program code. Please translate it to ${lang}:", code))

object GptService:
  def resource(secretKey: String): Resource[IO, GptService] =
    HttpClientCatsBackend
      .resource[IO]()
      .map(backend => new GptService(backend, secretKey))

extension (string: String)
  def takeWords(num: Int): String =
    val truncated = string.split(" ").take(num).mkString(" ")
    if truncated.length < string.length then truncated + "..." else truncated
