import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import org.forgerock.openicf.connectors.scriptedsql.ScriptedSQLConfiguration
import org.forgerock.openicf.misc.scriptedcommon.ICFObjectBuilder
import org.forgerock.openicf.misc.scriptedcommon.MapFilterVisitor
import org.forgerock.openicf.misc.scriptedcommon.OperationType
import org.identityconnectors.common.logging.Log
import org.identityconnectors.framework.common.exceptions.ConnectorException
import org.identityconnectors.framework.common.objects.ObjectClass
import org.identityconnectors.framework.common.objects.OperationOptions
import org.identityconnectors.framework.common.objects.ResultsHandler
import org.identityconnectors.framework.common.objects.SearchResult
import org.identityconnectors.framework.common.objects.filter.Filter

import java.sql.Connection

def log = log as Log
def operation = operation as OperationType
def options = options as OperationOptions
def objectClass = objectClass as ObjectClass
def configuration = configuration as ScriptedSQLConfiguration
def filter = filter as Filter
def connection = connection as Connection
def query = query as Closure
def handler = handler as ResultsHandler

/**
    The handling code of this thing: https://github.com/OpenRock/OpenICF-groovy-connector/blob/master/src/main/groovy/org/forgerock/openicf/misc/scriptedcommon/ScriptedConnectorBase.groovy
    more specifically the executeQuery method which provides 3 arguments to this script:
        - Filter: org.identityconnectors.framework.common.objects.filter.Filter
        - Query:
        - Handler: Closure<Boolean>
    Then there are auxiliary arguments provided to this script:
        - connection: handler to the SQL connection
        - objectClass: a String describing the Object class (__ACCOUNT__ / __GROUP__ / other)
        - log: a handler to the Log facility
        - options: a handler to the OperationOptions Map
        - query: a handler to the Query Map
            The Query map describes the filter used.

                 query = [ operation: "CONTAINS", left: attribute, right: "value", not: true/false ]
                 query = [ operation: "ENDSWITH", left: attribute, right: "value", not: true/false ]
                 query = [ operation: "STARTSWITH", left: attribute, right: "value", not: true/false ]
                 query = [ operation: "EQUALS", left: attribute, right: "value", not: true/false ]
                 query = [ operation: "GREATERTHAN", left: attribute, right: "value", not: true/false ]
                 query = [ operation: "GREATERTHANOREQUAL", left: attribute, right: "value", not: true/false ]
                 query = [ operation: "LESSTHAN", left: attribute, right: "value", not: true/false ]
                 query = [ operation: "LESSTHANOREQUAL", left: attribute, right: "value", not: true/false ]
                 query = null : then we assume we fetch everything

                 AND and OR filter just embed a left/right couple of queries.
                 query = [ operation: "AND", left: query1, right: query2 ]
                 query = [ operation: "OR", left: query1, right: query2 ]

    Apparently you have 2 options to provide the actual data from the DB:
        - Returning one of the following:
            * org.identityconnectors.framework.common.objects.SearchResult
            * String (which will be packaged into aforementioned SearchResult
        - Calling the Handler with one of the following:
            * ICFObjectBuilder.co call that wraps a closure
                (see https://github.com/OpenRock/OpenICF-groovy-connector/blob/master/src/main/groovy/org/forgerock/openicf/misc/scriptedcommon/ICFObjectBuilder.groovy)
            * Just a plain Closure
    What to return:
         Returns: A list of Maps. Each map describing one row.
         !!!! Each Map must contain a '__UID__' and '__NAME__' attribute.
         This is required to build a ConnectorObject.
*/

log.info("#### Entering {0} Script for {1} #####", operation, objectClass)

def sql = new Sql(connection)

switch (objectClass) {
    case ObjectClass.ACCOUNT:
        handleAccount(sql)
        break
    case BaseScript.ENTITLEMENT:
        handleEntitlement(sql)
        break
    default:
        throw new ConnectorException("Unknown object class " + objectClass)
}

// =================================================================================

void handleAccount(Sql sql) {
    Closure closure = { row ->
        log.info("#### Account Row from DB: {0} ####", row)
        handler(ICFObjectBuilder.co {
            uid row.account_id as String
            id row.rijksregisternummer
            attribute '__UID__', row.account_id
            attribute '__ENABLE__', row.enabled
            attribute '__NAME__', row.rijksregisternummer
            attribute 'firstname', row.given_name
            attribute 'lastname', row.surname
            attribute 'rijksregisternummer', row.rijksregisternummer
            attribute 'entitlements', row.entitlements.split(";")
        })
    }
    Map params = [:]
    String customWhere = "acc.deleted = false AND ent.deleted = false";
    String where = buildWhereClause(filter, params, 'account_id', 'rijksregisternummer', customWhere)
    String select = "SELECT acc.*, usr.*, string_agg(ent.entitlement_id, ';') as entitlements FROM webidm_accounts acc LEFT JOIN webidm_entitlements ent ON ent.account_id = acc.account_id LEFT JOIN webidm_users usr ON usr.rijksregisternummer = acc.user_rijksregisternummer ";
    String group = " GROUP BY acc.account_id, usr.rijksregisternummer";
    String sqlQuery = select + where + group;
    log.info('########QUERY### {0}', sqlQuery)
    sql.eachRow(params, sqlQuery, closure);
}

void handleEntitlement(Sql sql) {
    Closure closure = { row ->
        log.info("#### Entitlement Row from DB: {0} ####", row)
        handler(ICFObjectBuilder.co {
            uid row.entitlement_id as String
            id row.entitlement_id
            attribute '__NAME__', row.entitlement_id
            attribute 'accountId', row.account_id
            attribute 'privileges', row.privileges.split(";")
            attribute 'organisatiecode', row.organisation_code
            attribute 'organisatiecodedetail', row.organisation_code_detail
            attribute 'departement', row.department
            attribute 'dienst', row.service
            attribute 'functie', row.function
            attribute 'email', row.email
            attribute 'personeelsnummer', row.employee_number
            attribute 'fax', row.fax
            attribute 'gsm', row.mobile
            attribute 'telefoonnr', row.telephone
        })
    }

    Map params = [:]
    String customWhere = "ent.deleted = false";
    String where = buildWhereClause(filter, params, 'entitlement_id', 'entitlement_id', customWhere)
    String select = "SELECT ent.*, string_agg(priv.name, ';' ORDER BY priv.name) as privileges FROM webidm_entitlements ent LEFT JOIN webidm_entitlement_privilege ent_priv ON ent_priv.entitlement_entitlement_id = ent.entitlement_id LEFT JOIN webidm_privileges priv ON priv.name = ent_priv.privilege_name ";
    String group = " GROUP BY ent.entitlement_id";
    String sqlQuery = select + where + group;
    log.info('########QUERY### {0}', sqlQuery)
    sql.eachRow(params, sqlQuery, closure)
}

String buildWhereClause(Filter filter, Map sqlParams, String uidColumn, String nameColumn, String customWhere) {
    if (filter == null) {
        log.info("#### Returning empty where clause #####")
        return "WHERE " + customWhere
    }

    Map query = filter.accept(MapFilterVisitor.INSTANCE, null)

    log.info("#### Building where clause, query {0}, uidcolumn: {1}, nameColumn: {2} ####", query, uidColumn, nameColumn)

    String columnName = uidColumn.replaceFirst("[\\w]+\\.", "")

    String left = query.get("left")
    if (Uid.NAME.equals(left)) {
        left = uidColumn
    } else if (Name.NAME.equals(left)) {
        left = nameColumn
    }

    String right = query.get("right")

    String operation = query.get("operation")
    switch (operation) {
        case "CONTAINS":
            right = '%' + right + '%'
            break;
        case "ENDSWITH":
            right = '%' + right
            break;
        case "STARTSWITH":
            right = right + '%'
            break;
    }

    sqlParams.put(columnName, right)
    right = ":" + columnName

    def engine = new groovy.text.SimpleTemplateEngine()

    def whereTemplates = [
            CONTAINS          : 'WHERE $left ${not ? "not " : ""}like $right AND $custom',
            ENDSWITH          : 'WHERE $left ${not ? "not " : ""}like $right AND $custom',
            STARTSWITH        : 'WHERE $left ${not ? "not " : ""}like $right AND $custom',
            EQUALS            : 'WHERE $left ${not ? "<>" : "="} $right AND $custom',
            GREATERTHAN       : 'WHERE $left ${not ? "<=" : ">"} $right AND $custom',
            GREATERTHANOREQUAL: 'WHERE $left ${not ? "<" : ">="} $right AND $custom',
            LESSTHAN          : 'WHERE $left ${not ? ">=" : "<"} $right AND $custom',
            LESSTHANOREQUAL   : 'WHERE $left ${not ? ">" : "<="} $right AND $custom'
    ]

    def wt = whereTemplates.get(operation)
    def binding = [left: left, right: right, not: query.get("not"), custom: customWhere]
    def template = engine.createTemplate(wt).make(binding)
    def where = template.toString()

    log.info("#### Where clause: {0}, with parameters {1} ####", where, sqlParams)
    if (where.isEmpty()) {
        return 'WHERE ' + customWhere
    } else {
        return where
    }


}

