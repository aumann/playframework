/*
 * Copyright (C) Lightbend Inc. <https://www.lightbend.com>
 */

package play.api.http

import java.io.File

import akka.util.ByteString
import org.specs2.mutable.Specification
import play.api.libs.Files.TemporaryFile
import play.api.mvc.Codec
import play.api.mvc.MultipartFormData
import play.api.mvc.MultipartFormData.FilePart

import play.api.libs.Files.SingletonTemporaryFileCreator._

class WriteableSpec extends Specification {
  "Writeable" in {
    "of multipart" should {
      "work for temporary files" in {
        val multipartFormData = createMultipartFormData[TemporaryFile](
          create(new File("src/test/resources/multipart-form-data-file.txt").toPath)
        )
        val contentType = Some("text/plain")
        val codec       = Codec.utf_8

        val writeable               = Writeable.writeableOf_MultipartFormData(codec, contentType)
        val transformed: ByteString = writeable.transform(multipartFormData)

        transformed.utf8String must contain("Content-Disposition: form-data; name=name")
        transformed.utf8String must contain(
          """Content-Disposition: form-data; name="thefile"; filename="something.text""""
        )
        transformed.utf8String must contain("Content-Type: text/plain")
        transformed.utf8String must contain("multipart-form-data-file")
      }

      "work composing with another writeable" in {
        val multipartFormData = createMultipartFormData[String]("file part value")
        val contentType       = Some("text/plain")
        val codec             = Codec.utf_8

        val writeable = Writeable.writeableOf_MultipartFormData(
          codec,
          Writeable[FilePart[String]]((f: FilePart[String]) => codec.encode(f.ref), contentType)
        )
        val transformed: ByteString = writeable.transform(multipartFormData)

        transformed.utf8String must contain("Content-Disposition: form-data; name=name")
        transformed.utf8String must contain(
          """Content-Disposition: form-data; name="thefile"; filename="something.text""""
        )
        transformed.utf8String must contain("Content-Type: text/plain")
        transformed.utf8String must contain("file part value")
      }

      "escape 'name' and 'filename' params" in {
        val multipartFormData =
          createMultipartFormData[String](
            "file part value",
            data => Some(ByteString.fromString(data)),
            dataPartKey = "ab\"cd\nef\rgh\"ij\rk\nl",
            filePartKey = "mn\"op\nqr\rst\"uv\rw\nx",
            filePartFilename = "fo\"o\no\rb\"a\ra\nar.p\"df"
          )
        val codec = Codec.utf_8

        val writeable               = Writeable.writeableOf_MultipartFormData[String](None)(codec)
        val transformed: ByteString = writeable.transform(multipartFormData)

        transformed.utf8String must contain("""Content-Disposition: form-data; name="ab%22cd%0Aef%0Dgh%22ij%0Dk%0Al"""")
        transformed.utf8String must contain(
          """Content-Disposition: form-data; name="mn%22op%0Aqr%0Dst%22uv%0Dw%0Ax"; filename="fo%22o%0Ao%0Db%22a%0Da%0Aar.p%22df""""
        )
        transformed.utf8String must contain("Content-Type: text/plain")
        transformed.utf8String must contain("file part value")
      }

      "use multipart/form-data content-type" in {
        val contentType = Some("text/plain")
        val codec       = Codec.utf_8
        val writeable = Writeable.writeableOf_MultipartFormData(
          codec,
          Writeable[FilePart[String]]((f: FilePart[String]) => codec.encode(f.ref), contentType)
        )

        writeable.contentType must beSome(startWith("multipart/form-data; boundary="))
      }
    }

    "of urlEncodedForm" should {
      "encode keys and values" in {
        val codec                   = Codec.utf_8
        val writeable               = Writeable.writeableOf_urlEncodedForm(codec)
        val transformed: ByteString = writeable.transform(Map("foo$bar" -> Seq("ba$z")))

        transformed.utf8String must contain("foo%24bar=ba%24z")
      }
    }
  }

<<<<<<< HEAD
  def createMultipartFormData[A](ref: A): MultipartFormData[A] = {
=======
  def createMultipartFormData[A](
      ref: A,
      refToBytes: A => Option[ByteString] = (a: A) => None,
      dataPartKey: String = "name",
      filePartKey: String = "thefile",
      filePartFilename: String = "something.text"
  ): MultipartFormData[A] = {
>>>>>>> 8e787dd65c (Escape Content-Disposition params according to WHATWG HTML living standard)
    MultipartFormData[A](
      dataParts = Map(
        dataPartKey -> Seq("value")
      ),
      files = Seq(
        FilePart[A](
          key = filePartKey,
          filename = filePartFilename,
          contentType = Some("text/plain"),
          ref = ref
        )
      ),
      badParts = Seq.empty
    )
  }
}
