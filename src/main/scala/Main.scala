import cats.effect.{ExitCode, IO, IOApp, Resource}
import cats.syntax.all.*
import files.FileService
import web.WebService
import openai.GptService
import org.jsoup.Jsoup
import fs2.io.file.Path
import unindent.*

object Main extends IOApp:

  val services: Resource[IO, (FileService, WebService, GptService)] =
    (
      FileService.resource,
      WebService.resource,
      GptService.resource(System.getenv("OPENAI_SECRET_KEY")),
    ).tupled

  override def run(args: List[String]): IO[ExitCode] =
    services.use { (files, web, gpt) =>
      args match
        case "summarize" :: url :: Nil =>
          for
            content <- web.text(url)
            _       <- IO.println(s"Original:\n\n${content}\n\n")
            summary <- gpt.summarize(content)
            _       <- IO.println(s"Summary:\n\n${summary}\n\n")
          yield ExitCode.Success

        case "rewrite" :: url :: Nil =>
          for
            content <- web.text(url)
            _       <- IO.println(s"Original:\n\n${content}\n\n")
            summary <- gpt.rewrite(content)
            _       <- IO.println(s"Rewritten:\n\n${summary}\n\n")
          yield ExitCode.Success

        case "translate" :: filename :: languages =>
          for
            content <- files.text(Path(filename))
            _       <- IO.println(s"Original:\n\n${content}\n\n")
            translations <- languages.parTraverse(gpt.translate(content, _))
            _       <- languages.zip(translations).traverse((lang, text) => IO.println(s"Translation to ${lang}:\n\n${text}\n\n")).void
          yield ExitCode.Success

        case _ =>
          IO.println("Command not recognised. See the source for usage!").as(ExitCode.Error)
    }
