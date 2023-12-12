package jp.live.ugai.genomemask

import org.apache.jena.query.QueryExecutionFactory
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.riot.RDFDataMgr
import java.io.InputStream

/**
 * The main entry point of the application.
 *
 * @param args Command line arguments. If provided, they are treated as file names.
 */
fun main(args: Array<String>) {
    val make = Main()
    val fileNames =
        if (args.size > 0) {
            args
        } else {
            arrayOf(
                "Data/Admire_art1_scene1-101010.ttl",
                "Data/Clean_kitchentable1_scene7-222.ttl",
                "Data/Read_book1_scene6-555.ttl",
            )
        }
    for (fileName in fileNames) {
        val rates = fileName.replace(Regex(".*-"), "")
        when (rates) {
            "222.ttl" -> make.makeData(fileName, 2, 2, 2)
            "220.ttl" -> make.makeData(fileName, 2, 2, 0)
            "555.ttl" -> make.makeData(fileName, 5, 5, 5)
            "550.ttl" -> make.makeData(fileName, 5, 5, 0)
            "101010.ttl" -> make.makeData(fileName, 10, 10, 10)
            "10100.ttl" -> make.makeData(fileName, 10, 10, 0)
        }
        //       make.makeData(fileName, 2, 0, 0)
    }
}

/**
 * Main class responsible for creating data from files.
 */
class Main {
    /**
     * Creates data from a given file.
     *
     * @param fileName The name of the file to read data from.
     * @param placeRate The rate of the place.
     * @param actionRate The rate of the action.
     * @param objectRate The rate of the object.
     */
    fun makeData(
        fileName: String,
        placeRate: Int,
        actionRate: Int,
        objectRate: Int,
    ) {
        val model: Model = ModelFactory.createDefaultModel()

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

        var beginTime = 0.0
        var endTime = 0.0
        QueryExecutionFactory.create(queryString, model).use { qexec ->
            qexec.execSelect()!!.forEach {
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
        }
    }
}
