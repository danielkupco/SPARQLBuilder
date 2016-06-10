// Simple query example

// plain Java string
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

// sparql builder example
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

// More complicated example with subquery and aggregate functions

// plain java string
String example2a = "SELECT (?Destination)  (?name)  (?wikiPageID)  (?image_uri)  (?comment)  (AVG(?latitude) as ?latitude)  (AVG(?longitude) as ?longitude)" +
          " FROM <http://dbpedia.org>" +
          " WHERE" +
          " {" +
          " {" +
          " SELECT *" +
          " WHERE" +
          " {" +
          " ?Destination a dbo:City ;" +
          " rdfs:label ?name ;" +
          " rdfs:comment ?comment ." +
          " FILTER(lang(?comment)=\"en\" && lang(?name)=\"en\") ." +
          " }" +
          " }" +
          " ?Destination dbo:wikiPageID ?wikiPageID ;" +
          " dbo:thumbnail ?image_uri ;" +
          " geo:lat|dbp:latD ?latitude ;" +
          " geo:long|dbp:longD ?longitude ." +
          " }" +
          " GROUP BY ?Destination ?name ?wikiPageID ?image_uri ?comment" +
          " ORDER BY ?name" +
          " LIMIT 10" +
          " OFFSET 0";

// sparql builder example
// notice that these variables are just some plain constants, this way we avoid copy/paste errors
String example2b = sparqlBuilder.startQuery()
          .select()
          .var(VARIABLE).var(Destination.NAME_FIELD).var(Destination.WIKI_PAGE_ID_FIELD)
          .var(Destination.IMAGE_URI_FIELD).var(Destination.COMMENT_FIELD)
          .aggregateVarAs("AVG", Destination.LATITUDE_FIELD, Destination.LATITUDE_FIELD)
          .aggregateVarAs("AVG", Destination.LONGITUDE_FIELD, Destination.LONGITUDE_FIELD)
          .from("http://dbpedia.org")
          .startWhere()
              .startSubquery()
              .select()
              .variables() // all
              .startWhere()
                  .triplet(VARIABLE, "a", "dbo:City", false)
                  .property("rdfs:label").as(Destination.NAME_FIELD)
                  .property("rdfs:comment").as(Destination.COMMENT_FIELD)
                  .startFilter()
                      .varFunction("lang", Destination.COMMENT_FIELD).eqAsString("en").and()
                      .varFunction("lang", Destination.NAME_FIELD).eqAsString("en")
                  .endFilter()
              .endWhere()
              .endSubquery()
              // must continue with triplet
              .triplet(VARIABLE, "dbo:wikiPageID", Destination.WIKI_PAGE_ID_FIELD, true)
              .property("dbo:thumbnail").as(Destination.IMAGE_URI_FIELD)
              .propertyChoice("geo:lat", "dbp:latD").as(Destination.LATITUDE_FIELD)
              .propertyChoice("geo:long", "dbp:longD").as(Destination.LONGITUDE_FIELD)
          .endWhere()
          .groupBy(VARIABLE, Destination.NAME_FIELD, Destination.WIKI_PAGE_ID_FIELD,
                  Destination.IMAGE_URI_FIELD, Destination.COMMENT_FIELD)
          .orderBy(Destination.NAME_FIELD)
          // we can make pagination with limit and offset
          .limit(queryLimit)
          .offset(queryLimit * page)
          .build();
