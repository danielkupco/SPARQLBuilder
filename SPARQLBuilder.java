package rs.ftn.pma.tourismobile.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for building SPARQL queries.
 * Created by Daniel Kupčo on 07.06.2016.
 */
public class SPARQLBuilder {

    private SPARQLSelectPart query;

    public SPARQLSelectPart startQuery() {
        query = new SPARQLSelectPart();
        return query;
    }

    public String getQuery() {
        return query.build();
    }

    public String prettify() {
        StringBuilder prettified = new StringBuilder(getQuery());

        // keywords
        List<String> keywords = new ArrayList<>();
        keywords.add("SELECT");
        keywords.add("FROM");
        keywords.add("WHERE");
        keywords.add("FILTER");
        keywords.add("GROUP BY");
        keywords.add("ORDER BY");
        keywords.add("LIMIT");
        keywords.add("OFFSET");

        for(String keyword : keywords) {
            breakLineOnKeyword(prettified, keyword);
        }

        // separators and indentation
        String indentation = "";
        int firstOpenCurly = prettified.indexOf("{");
        // must be updated because query length is getting bigger
        int lastClosedCurly = prettified.lastIndexOf("}") + 1;
        boolean afterTriplet = false;

        for (int i = firstOpenCurly; i < lastClosedCurly; i++) {
            char c = prettified.charAt(i);
            if(c == '{') {
                prettified.replace(i - 1, i, "\n" + indentation); // insert new line and indentation
                lastClosedCurly += indentation.length() + 1; // update last closed curly bracket index
                i += indentation.length(); // skip indentation, new line is already skipped with i++ in the loop
                indentation += "  "; // increase indentation
                if(prettified.indexOf("{ ?", i) == i) { // if there is triplet break it to the next line
                    prettified.replace(i + 1, i + 2, "\n" + indentation);
                    lastClosedCurly += indentation.length() + 1;
                    i += indentation.length();
                }
            }
            else if(c == '}') {
                if(indentation.length() > 1) {
                    indentation = indentation.substring(0, indentation.length() - 2); // first decrease the indentation
                }
                prettified.replace(i - 1, i, "\n" + indentation); // same process as above...
                lastClosedCurly += indentation.length() + 1;
                i += indentation.length();
                if(prettified.indexOf("} ?", i) == i) {
                    prettified.replace(i + 1, i + 2, "\n" + indentation);
                    lastClosedCurly += indentation.length() + 1;
                    i += indentation.length();
                }
            }

            // for every keyword in the process (while we have valid indentation in each moment)
            for(String keyword : keywords) {
                int index = prettified.indexOf(keyword, i); // get the keyword index form current position
                if(i == index) { // if we are at the keyword
                    prettified.insert(i, indentation); // indent it
                    lastClosedCurly += indentation.length();
                    i += indentation.length() + keyword.length(); // skip the indentation and the keyword
                }
            }

            // separators
            if (c == ';') {
                if (!afterTriplet) { // if after triplet indent a bit more
                    afterTriplet = true;
                    indentation += "  ";
                }
                prettified.replace(i + 1, i + 2, "\n" + indentation);
                lastClosedCurly += indentation.length() + 1;
            }
            else if (c == '.') {
                if (afterTriplet) { // if another condition and we are in triplet decrease indentation
                    afterTriplet = false;
                    indentation = indentation.substring(0, indentation.length() - 2);
                }
                prettified.replace(i + 1, i + 2, "\n" + indentation);
                lastClosedCurly += indentation.length() + 1;
            }
        }

        // in the end clear empty lines
        clearEmptyLines(prettified);

        return prettified.toString();
    }

    private void breakLineOnKeyword(StringBuilder stringBuilder, String keyword) {
        int index = stringBuilder.indexOf(keyword);
        while(index > -1) {
            stringBuilder.insert(index, "\n");
            index = stringBuilder.indexOf(keyword, index + 2);
        }
    }

    private void clearEmptyLines(StringBuilder stringBuilder) {
        int firstNewLine = stringBuilder.indexOf("\n");
        int secondNewLine = stringBuilder.indexOf("\n", firstNewLine + 1);
        while(secondNewLine > -1 && secondNewLine < stringBuilder.length()) {
            // if there is empty line remove it
            // first new line index automatically takes the value of the second
            if(stringBuilder.substring(firstNewLine, secondNewLine).trim().isEmpty()) {
                stringBuilder.replace(firstNewLine, secondNewLine, "");
            }
            // otherwise go to the next new line character
            else{
                firstNewLine = secondNewLine;
            }
            secondNewLine = stringBuilder.indexOf("\n", firstNewLine + 1);
        }

    }

    public static abstract class SPARQLPart {

        protected SPARQLPart creator;

        protected StringBuilder queryPart;

        public SPARQLPart(SPARQLPart creator) {
            this.creator = creator;
            queryPart = new StringBuilder();
        }

        public SPARQLPart(SPARQLPart creator, String initialString) {
            this.creator = creator;
            queryPart = new StringBuilder(initialString);
        }

        public char lastChar(StringBuilder stringBuilder) {
            int i = 1;
            int length = stringBuilder.length();
            char last = ' ';
            while(last == ' ') {
                last = stringBuilder.charAt(length - i);
                i++;
            }
            return last;
        }

        public SPARQLPart appendString(String string) {
            queryPart.append(string);
            return this;
        }

        public SPARQLPart appendStringLine(String string) {
            queryPart.append("\n").append(string);
            return this;
        }

        public SPARQLPart endPart() {
            creator.appendString(queryPart.toString());
            return creator;
        }

    }

    public static class SPARQLSelectPart extends SPARQLPart {

        public SPARQLSelectPart() {
            super(null);
        }

        public SPARQLSelectPart(SPARQLPart creator) {
            super(creator);
        }

        public SPARQLSelectPart(SPARQLPart creator, String initialString) {
            super(creator, initialString);
        }

        public SPARQLWherePart endSubquery() {
            queryPart.append(" }");
            return (SPARQLWherePart) super.endPart();
        }

        public SPARQLSelectPart select() {
            queryPart.append("SELECT");
            return this;
        }

        public SPARQLSelectPart selectDistinct() {
            queryPart.append("SELECT DISTINCT");
            return this;
        }

        public SPARQLSelectPart variables(String... variables) {
            if(variables.length == 0) {
                queryPart.append(" *");
            }
            else {
                for(String variable : variables) {
                    queryPart.append(String.format(" ?%s", variable));
                }
            }
            return this;
        }

        public SPARQLSelectPart var(String name) {
            queryPart.append(String.format(" (?%s) ", name));
            return this;
        }

        public SPARQLSelectPart varAs(String name, String as) {
            queryPart.append(String.format(" (?%s as ?%s) ", name, as));
            return this;
        }

        public SPARQLSelectPart aggregateVarAs(String aggregateFunction, String name, String as) {
            queryPart.append(String.format(" (%s(?%s) as ?%s) ", aggregateFunction, name, as));
            return this;
        }

        public SPARQLSelectPart from(String url) {
            queryPart.append(String.format(" FROM <%s>", url));
            return this;
        }

        public SPARQLWherePart startWhere() {
            SPARQLWherePart wherePart = new SPARQLWherePart(this);
            return wherePart;
        }

        public SPARQLSelectPart orderBy(String... variables) {
            if(variables.length > 0) {
                queryPart.append(" ORDER BY");
                for(String variable : variables) {
                    queryPart.append(String.format(" ?%s", variable));
                }
            }
            return this;
        }

        public SPARQLSelectPart groupBy(String... variables) {
            if(variables.length > 0) {
                queryPart.append(" GROUP BY");
                for(String variable : variables) {
                    queryPart.append(String.format(" ?%s", variable));
                }
            }
            return this;
        }

        public SPARQLSelectPart limit(int number) {
            queryPart.append(String.format(" LIMIT %d", number));
            return this;
        }

        public SPARQLSelectPart offset(int number) {
            queryPart.append(String.format(" OFFSET %d", number));
            return this;
        }

        public String build() {
            return queryPart.toString();
        }

    }

    public static class SPARQLWherePart extends SPARQLPart {

        public SPARQLWherePart(SPARQLSelectPart creator) {
            super(creator, " WHERE {");
        }

        public SPARQLSelectPart endWhere() {
            queryPart.append(" . }");
            return (SPARQLSelectPart) super.endPart();
        }

        public SPARQLSelectPart startSubquery() {
            return new SPARQLSelectPart(this, " { ");
        }

        public SPARQLWherePart triplet(String subject, String predicate, String object) {
            return triplet(subject, predicate, object, false);
        }

        public SPARQLWherePart triplet(String subject, String predicate, String object, boolean isObjectVariable) {
            char lastChar = lastChar(queryPart);
            if(!(lastChar == '{' || lastChar == '.' || lastChar == '}')) {
                queryPart.append(" .");
            }
            final String format = isObjectVariable ? " ?%s %s ?%s" : " ?%s %s %s";
            queryPart.append(String.format(format, subject, predicate, object));
            return this;
        }

        public SPARQLWherePart property(String property) {
            queryPart.append(String.format(" ; %s", property));
            return this;
        }

        public SPARQLWherePart propertyChoice(String... properties) {
            queryPart.append(" ; ");
            for(String property : properties) {
                queryPart.append(String.format("%s|", property));
            }
            queryPart.deleteCharAt(queryPart.length() - 1);
            return this;
        }

        public SPARQLWherePart as(String variable) {
            queryPart.append(String.format(" ?%s", variable));
            return this;
        }

        public SPARQLWherePart and() {
            queryPart.append(" .");
            return this;
        }

        public SPARQLFilterPart startFilter() {
            SPARQLFilterPart filterPart = new SPARQLFilterPart(this);
            return filterPart;
        }

    }

    public static class SPARQLFilterPart extends SPARQLPart {

        public SPARQLFilterPart(SPARQLWherePart creator) {
            super(creator, " . FILTER(");
        }

        public SPARQLFilterPart var(String name) {
            queryPart.append(String.format("?%s", name));
            return this;
        }

        public SPARQLFilterPart varFunction(String function, String name) {
            queryPart.append(String.format("%s(?%s)", function, name));
            return this;
        }

        public SPARQLFilterPart eq(String value) {
            queryPart.append(String.format("=%s", value));
            return this;
        }

        public SPARQLFilterPart eqAsString(String value) {
            queryPart.append(String.format("=\"%s\"", value));
            return this;
        }

        public SPARQLFilterPart eqAsType(String value, String type) {
            queryPart.append(String.format("=%s^^%s", value, type));
            return this;
        }

        public SPARQLFilterPart and() {
            queryPart.append(" && ");
            return this;
        }

        public SPARQLFilterPart or() {
            queryPart.append(" || ");
            return this;
        }

        public SPARQLWherePart endFilter() {
            queryPart.append(")");
            return (SPARQLWherePart) super.endPart();
        }

    }

}
