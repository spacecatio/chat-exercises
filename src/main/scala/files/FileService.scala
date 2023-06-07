package files

import cats.effect.{ExitCode, IO, IOApp, Resource}
import cats.syntax.all.*
import fs2.io.file.{Files, Path}

class FileService:
  def text(path: Path): IO[String] =
    Files[IO].readUtf8(path).compile.fold("")(_ + _)

  def lines(path: Path): IO[List[String]] =
    Files[IO].readUtf8Lines(path).compile.toList

  def list(path: Path): IO[List[Path]] =
    Files[IO].list(path).compile.toList

object FileService:
  def resource: Resource[IO, FileService] =
    Resource.make(FileService().pure[IO])(_ => ().pure[IO])