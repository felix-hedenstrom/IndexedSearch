package test
import org.scalatest.FunSuite
import scala.collection.mutable.HashSet

class IndexableTestItem(itemName: String, itemText: String) extends Indexable{
  def ItemName: String = itemName 
  def ItemContents: String = itemText 
}

class IndexTest extends FunSuite {
  
  val test_items = Array(
    new IndexableTestItem("file1.txt", "Hi! Am I talking to Steve?"), 
    new IndexableTestItem("file2.txt", "How long have we been watching this movie Steve? Are we close to the end."))

  val test_index = new Index(test_items)
  test("Index.index") {
    // The index should show that "steve" is present in both files
    assert(test_index.index("steve") == HashSet("file1.txt", "file2.txt"))
    // The index should give the files the expected score
    assert(
      test_index.search(Array("steve", "we")).toSet == Array(("file1.txt", 50), ("file2.txt", 100)).toSet)
  }

  test("Index.tokenize") {
    assert("end" == test_index.tokenize("end."))
    assert("steve" == test_index.tokenize("Steve?"))
  }

  test("Index.filescore") {

    // 100% score since all arguments are present in the file
    assert(100 == test_index.file_score("file1.txt", Array("steve")))
    // 0% score since none of the arguments are present
    assert(0 == test_index.file_score("file1.txt", Array("bob")))
    // 25% score since one of the names are present
    assert(25 == test_index.file_score("file1.txt", Array("steve", "bob", "alice", "john")))
  }
}
