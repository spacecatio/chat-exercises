import cats.effect.{ExitCode, IO, IOApp, Resource}
import cats.syntax.all.*
import files.FileService
import web.WebService
import openai.GptService
import org.jsoup.Jsoup
import fs2.io.file.Path
import unindent.*

// Replace the body of Main.run as follows:
// - Accept the URL of a web page as a command line parameter
// - Use WebService to extract the text of the page
// - Use GptService to summarize the text
// - More exercises to follow!

object Main extends IOApp:
  override def run(args: List[String]): IO[ExitCode] =
    IO.pure(ExitCode.Success)
