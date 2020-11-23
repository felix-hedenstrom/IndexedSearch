package test

import java.io.File
import java.io.BufferedReader

import scala.util.Try
import scala.io.Source
import scala.collection.mutable.{HashMap, HashSet}

object SimpleSearch extends App{

  import scala.io.StdIn.readLine

  sealed trait ReadFileError

  case object MissingPathArg extends ReadFileError
  case class NotDirectory(error: String) extends ReadFileError
  case class FileNotFound(t: Throwable) extends ReadFileError

  /**
   * Read all the files in a given directory
   */
  def readFiles(args: Array[String]): Either[ReadFileError, Array[File]] = {
    readDirectory(args)
      .map(dir => dir.listFiles.filter(_.isFile))
  }

  /**
   * Try to read a directory
   */
  def readDirectory(args: Array[String]): Either[ReadFileError, File] = {
    for {
      path <- args.headOption.toRight(MissingPathArg)
      file <- Try(new java.io.File(path))
        .fold(
          throwable => Left(FileNotFound(throwable)),
          file =>
            if (file.isDirectory) Right(file)
            else Left(NotDirectory(s"Path [$path] is not a directory"))
          )
    } yield file
  }

  /**
   * Start the CLI.
   */
  def cli_start(index: Index): Unit = {
    def get_user_input(): String = {
      print("search>")
      scala.io.StdIn.readLine.trim
    }
    // Every time a user input is required, step once through this internal function 
    def cli_step(user_input: String): Unit = {
      get_user_input() match {
        case "exit" => return 
        case "" => println("Please enter one or more search terms. Enter \"exit\" to exit.")
        case s => println(index.search(s.split("\\s+")))
      }

      cli_step()
    }
    println(s"${index.files.size} files read")
    println("Please enter one or more search terms. Enter \"exit\" to exit.")

    cli_step()
  }

  /**
   * 1. Check the given directory
   * 2. Create the index
   * 3. Start the CLI
   */
  SimpleSearch
    .readFiles(args)
    .fold(
      error => println("Could not finish: " + error),
      files => cli_start(new Index(files.map(new IndexableFile(_))))
    )
}

/**
 * This is important for testing purposes.
 */
trait Indexable{
  def ItemName: String
  def ItemContents: String
}

class IndexableFile(file: java.io.File) extends Indexable{
  def ItemName: String = file.getName
  def ItemContents: String = scala.io.Source.fromFile(file).mkString
}

class Index(indexable_items: Array[_ <: Indexable]){
  /**
   * Tokenize the string and try to isolate the word from any unneeded characters and casing
   *
   * Example:
   *  s = "end."
   *  returns "end"
   *
   *  s = "Steve?"
   *  returns "steve"
   */
  def tokenize(s: String): String = {
    """[:\-'"\(\)\[\]\*\.!\?]""".r.replaceAllIn(s, "").toLowerCase
  }

  val files = indexable_items.map(_.ItemName)
  /**
   * The index is a mapping of words or "tokens", which are segments of a text that have been stripped of 
   * extra data. See `tokenize`
   *
   * The index maps tokens to files that contain said tokens.
   *
   * Example:
   *
   * index("foo") == HashSet("file1.txt", "file3.txt")
   */
  val index: HashMap[String, HashSet[String]] = {
    var word_occurrences: HashMap[String, HashSet[String]] = HashMap.empty

    indexable_items.map(
      indexable_item => 
        for(indexable_item_element <- indexable_item.ItemContents.split("\\s+")){
          val tokenized_item = tokenize(indexable_item_element)
          word_occurrences(tokenized_item) = 
            word_occurrences.get(tokenized_item).getOrElse(HashSet.empty) + indexable_item.ItemName
        }
    )

    word_occurrences
  }

  /**
   * Use the index to score each file based on the user given words
   *
   * 1. Tokenize the words
   * 2. Calculate a score for each file
   * 3. Sort the file-scores
   * 4. Create a string representation of the top 10
   */
  def search(args: Array[String]): String = {
    def search_raw(searchwords: Array[String]): Array[(String, Integer)] = {
      val tokenized_words = searchwords.map(tokenize(_))

      files.map(
        file =>
          (file, file_score(file, tokenized_words)))
    }

    search_raw(args)
      .sortWith(_._2 > _._2)
      // We are only interested in the 10 first elements after sorting the list
      .take(10)
      .map(
      filescore =>  
        s"${filescore._1} : ${filescore._2}%" 
      )
      .mkString(" ")
  }

  /**
   * Files are scored by the number of tokens that are present in the file. If all words 
   * are present they recieve a score of 100, if none are present the recieve a score of 100
   *
   * Since we tokenize the words, false postives are possible. If we search for the exact word "[are]" we will match
   * "are" (without the brackets). This could be improved by allowing the user to specify if tokenized
   * search is prefferable, but that would require refactorization.
   */
  def file_score(file: String, search_tokens: Array[String]): Integer = {
    val nr_tokens_in_file = search_tokens.map(
      token =>
        if (index.get(token).getOrElse(HashSet.empty).contains(file)){
          1
        }else{
          0
        }
      ).sum 

    ((nr_tokens_in_file * 100f) / search_tokens.size).toInt
  }

}
