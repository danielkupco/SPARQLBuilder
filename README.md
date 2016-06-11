# SPARQLBuilder
Simple Java utility class for building SPARQL queries.

---

#### Description
This class should make writing SPARQL queries easier, more readable and less prone to errors.
Also there is prettify feature that prints SPARQL query in much more readable format so it is easier to spot eventual errors.

#### Usage example

Using plain Java string:
```java
String example1a = "SELECT DISTINCT * FROM <http://dbpedia.org>\n" +
          "WHERE  { \n" +
          "  ?destination a dbo:Park ;\n" +
          "    dbp:name ?name ;\n" +
          "    geo:lat ?lat ;\n" +
          "    geo:long ?long ;\n" +
          "    dbo:thumbnail ?thumbnail ;\n" +
          "    dbp:website ?website ;\n" +
          "    rdfs:comment ?comment ;\n" +
          "    dbo:abstract ?abstract .\n" +
          "  FILTER( lang(?comment)=\"en\" && lang(?abstract)=\"en\") .\n" +
          "}\n" +
          "ORDER BY ?name\n" +
          "LIMIT 10";
```

Using SPARQLBuilder:
```java
SPARQLBuilder sparqlBuilder = new SPARQLBuilder();
String example1b = sparqlBuilder.startQuery()
          .selectDistinct()
          .from("http://dbpedia.org")
          .startWhere()
              .triplet("destination", "a", "dbo:Park")
              .property("dbp:name").as("name")
              .property("geo:lat").as("lat")
              .property("geo:long").as("long")
              .property("dbo:thumbnail").as("thumbnail")
              .property("foaf:isPrimaryTopicOf").as("wikiLink")
              .property("rdfs:comment").as("comment")
              .property("dbo:abstract").as("abstract")
              .startFilter()
                  .varFunction("lang", "comment").eq("en").and()
                  .varFunction("lang", "abstract").eq("en")
              .endFilter()
          .endWhere()
          .orderBy("name")
          .limit(10)
          .build();
```
You can even prettify your query like this:
```java
String prettyQuery = sparqlBuilder.prettify();
```
