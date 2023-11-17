package jp.live.ugai.genomemask

import org.apache.jena.query.QueryExecutionFactory
import org.apache.jena.query.ResultSet
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.riot.RDFDataMgr
import java.io.InputStream

fun main(args: Array<String>) {
    val make = Main()
    val fileNames = if (args.size > 0) {
        args
    } else {
        arrayOf(
            "Data/Admire_art1_scene1-101010.ttl",
            "Data/Clean_kitchentable1_scene7-222.ttl",
            "Data/Read_book1_scene6-555.ttl"
        )
    }
    for (fileName in fileNames) {
        val rates = fileName.replace(Regex(".*-"), "")
//        println(rates)
        when (rates) {
            "222.ttl" -> {
                make.makeData(fileName, 2, 2, 2)
            }

            "220.ttl" -> {
                make.makeData(fileName, 2, 2, 0)
            }

            "555.ttl" -> {
                make.makeData(fileName, 5, 5, 5)
            }

            "550.ttl" -> {
                make.makeData(fileName, 5, 5, 0)
            }

            "101010.ttl" -> {
                make.makeData(fileName, 10, 10, 10)
            }

            "10100.ttl" -> {
                make.makeData(fileName, 10, 10, 0)
            }
        }
        //       make.makeData(fileName, 2, 0, 0)
    }
}

class Main {

    fun makeData(fileName: String, placeRate: Int, actionRate: Int, objectRate: Int) {
        val model: Model = ModelFactory.createDefaultModel()

// use the RDFDataMgr to find the input file
        val inputStream: InputStream = RDFDataMgr.open("file:$fileName")
        model.read(inputStream, null, "TURTLE")
        inputStream.close()

        val queryString = """
PREFIX ex: <http://kgrc4si.home.kg/virtualhome2kg/instance/>
PREFIX : <http://kgrc4si.home.kg/virtualhome2kg/ontology/>
PREFIX vh2kg: <http://kgrc4si.home.kg/virtualhome2kg/ontology/>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX time: <http://www.w3.org/2006/time#>
select DISTINCT * where {
    ?ee :hasEvent ?event .
    ?event :action ?action .
    OPTIONAL {
    ?event vh2kg:place ?place .
    }
    ?event vh2kg:time ?time .
    ?time time:numericDuration ?dtime .
    ?event vh2kg:eventNumber ?number .
    OPTIONAL {
    ?event :mainObject ?mainObject .
    } 
    OPTIONAL {
    ?event :targetObject ?targetObject .
    } 
#    ?event ?p ?object .
}order by (?number)
    """

        val dQueries: MutableList<String> = mutableListOf()
        val iQueries: MutableList<String> = mutableListOf()
        var beginTime = 0.0
        var endTime = 0.0
        QueryExecutionFactory.create(queryString, model).use { qexec ->
            var results: ResultSet? = qexec.execSelect()
//        results = ResultSetFactory.copyResults(results)
//        println(results)
            results!!.forEach {
                val event = it["ee"].toString().replace("http://kgrc4si.home.kg/virtualhome2kg/instance/", "")
                val number = it["number"].asLiteral().int
                val action =
                    it["action"].toString().replace("http://kgrc4si.home.kg/virtualhome2kg/ontology/action/", "")

                endTime += it["dtime"].asLiteral().double
                print("$placeRate,$actionRate,$objectRate,$event,$number,$beginTime,$endTime,$action,")
                beginTime += it["dtime"].asLiteral().double
                val mainObject = it["mainObject"]
                if (mainObject != null) {
                    print("${mainObject.toString().replace("http://kgrc4si.home.kg/virtualhome2kg/instance/", "")}")
                }
                print(",")
                val targetObject = it["targetObject"]
                if (targetObject != null) {
                    print("${targetObject.toString().replace("http://kgrc4si.home.kg/virtualhome2kg/instance/", "")}")
                }
                println()
            }

            // Create an UpdateRequest

            // Create a Dataset and add the Model to it
//        return results // Passes the result set out of the try-resources
        }
    }
}
