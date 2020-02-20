import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import java.sql.Timestamp
import java.util.Calendar
import org.forgerock.openicf.connectors.scriptedsql.ScriptedSQLConfiguration
import org.forgerock.openicf.misc.scriptedcommon.OperationType
import org.identityconnectors.common.logging.Log
import org.identityconnectors.framework.common.objects.*
import org.identityconnectors.framework.common.exceptions.ConnectorException


def configuration = configuration as ScriptedSQLConfiguration
def operation = operation as OperationType
def objectClass = objectClass as ObjectClass
def log = log as Log

log.info("Entering {0} Script for objectClass {1}", operation, objectClass)

def sql = new Sql(connection)


//def now = new Timestamp(Calendar.getInstance().getTimeInMillis()) //first.values()[1]
//log.info("NOW IS {0}", now.toString())
//
//def huh = sql.firstRow("SELECT now()::timestamp, LOCALTIMESTAMP")
//log.info("DB SAYS {0}", huh)
//

switch (operation) {
    case OperationType.SYNC:
        def token = token as String
        def handler = handler as SyncResultsHandler
        def options = options as OperationOptions
        log.info("With options {0}, token {1} and handler {2}", options, token, handler)
        if (token != null) {
            log.info("WHYYYY {0}", token)
            handleSync(sql, token, handler)
        }
        break
    case OperationType.GET_LATEST_SYNC_TOKEN:
        return handleGetLatestSyncToken(sql)
    default:
        return
}

void handleSync(Sql sql, String token, SyncResultsHandler handler) {
    switch (objectClass) {
        case ObjectClass.ACCOUNT:
            handleAccountSync(sql, token, handler)
            break
        case BaseScript.ENTITLEMENT:
            handleEntitlementSync(sql, token, handler)
            break
        default:
            throw new ConnectorException("Unknown object class " + objectClass)
    }
}

void handleAccountSync(Sql sql, String token, SyncResultsHandler handler) {
    Closure closure = { row ->
        log.info("#### Account Row from DB: {0} ####", row)
        ConnectorObjectBuilder connObjBuilder = new ConnectorObjectBuilder();
        connObjBuilder.addAttribute(new Uid(row.account_id))
        connObjBuilder.addAttribute(new Name(row.rijksregisternummer))
        connObjBuilder.addAttribute(AttributeBuilder.build('__ENABLE__', row.enabled))
        connObjBuilder.addAttribute(AttributeBuilder.build('firstname', row.given_name))
        connObjBuilder.addAttribute(AttributeBuilder.build('lastname', row.surname))
        connObjBuilder.addAttribute(AttributeBuilder.build('rijksregisternummer', row.rijksregisternummer))
        connObjBuilder.addAttribute(AttributeBuilder.build('entitlements', row.entitlements.split(";")))
        SyncDeltaBuilder synDeltaBuilder = new SyncDeltaBuilder();
        synDeltaBuilder.setToken(new SyncToken(row.last_modified_date.toString()));
        synDeltaBuilder.setObject(connObjBuilder.build());
        if (row.deleted) {
            synDeltaBuilder.setDeltaType(SyncDeltaType.DELETE);
        } else {
            synDeltaBuilder.setDeltaType(SyncDeltaType.CREATE_OR_UPDATE);
        }
        SyncDelta syncDelta = synDeltaBuilder.build()
        log.info("#### SyncDelta {0}", syncDelta)
        handler.handle(syncDelta);
    }
    String select = "SELECT acc.*, usr.*, string_agg(ent.entitlement_id, ';') as entitlements FROM webidm_accounts acc LEFT JOIN webidm_entitlements ent ON ent.account_id = acc.account_id LEFT JOIN webidm_users usr ON usr.rijksregisternummer = acc.user_rijksregisternummer ";
    String where = "WHERE ent.deleted = false AND acc.last_modified_date > TO_TIMESTAMP('" + token + "', 'YYYY-MM-DD HH24:MI:SS.US') ";
    String group = "GROUP BY acc.account_id, usr.rijksregisternummer";
    String sqlQuery = select + where + group;
    log.info('########QUERY### {0}', sqlQuery)
    sql.eachRow([:], sqlQuery, closure)
}

void handleEntitlementSync(Sql sql, String token, SyncResultsHandler handler) {
    Closure closure = { row ->
        log.info("#### Entitlements Row from DB: {0} ####", row)
        ConnectorObjectBuilder connObjBuilder = new ConnectorObjectBuilder();
        connObjBuilder.addAttribute(new Uid(row.entitlement_id))
        connObjBuilder.addAttribute(new Name(row.entitlement_id))
        connObjBuilder.addAttribute(AttributeBuilder.build('accountId', row.account_id))
        connObjBuilder.addAttribute(AttributeBuilder.build('privileges', row.privileges.split(";")))
        connObjBuilder.addAttribute(AttributeBuilder.build('organisatiecode', row.organisation_code))
        connObjBuilder.addAttribute(AttributeBuilder.build('organisatiecodedetail', row.organisation_code_detail))
        connObjBuilder.addAttribute(AttributeBuilder.build('departement', row.department))
        connObjBuilder.addAttribute(AttributeBuilder.build('dienst', row.service))
        connObjBuilder.addAttribute(AttributeBuilder.build('functie', row.function))
        connObjBuilder.addAttribute(AttributeBuilder.build('email', row.email))
        connObjBuilder.addAttribute(AttributeBuilder.build('personeelsnummer', row.employee_number))
        connObjBuilder.addAttribute(AttributeBuilder.build('fax', row.fax))
        connObjBuilder.addAttribute(AttributeBuilder.build('gsm', row.mobile))
        connObjBuilder.addAttribute(AttributeBuilder.build('telefoonnr', row.telephone))
        SyncDeltaBuilder synDeltaBuilder = new SyncDeltaBuilder();
        synDeltaBuilder.setToken(new SyncToken(row.last_modified_date.toString()));
        synDeltaBuilder.setObject(connObjBuilder.build());
        if (row.deleted) {
            synDeltaBuilder.setDeltaType(SyncDeltaType.DELETE);
        } else {
            synDeltaBuilder.setDeltaType(SyncDeltaType.CREATE_OR_UPDATE);
        }
        SyncDelta syncDelta = synDeltaBuilder.build()
        log.info("#### SyncDelta {0}", syncDelta)
        handler.handle(syncDelta);
    }
    String select = "SELECT ent.*, string_agg(priv.name, ';' ORDER BY priv.name) as privileges FROM webidm_entitlements ent LEFT JOIN webidm_entitlement_privilege ent_priv ON ent_priv.entitlement_entitlement_id = ent.entitlement_id LEFT JOIN webidm_privileges priv ON priv.name = ent_priv.privilege_name ";
    String where = "WHERE ent.last_modified_date > TO_TIMESTAMP('" + token + "', 'YYYY-MM-DD HH24:MI:SS.US') ";
    String group = "GROUP BY ent.entitlement_id";
    String sqlQuery = select + where + group;
    log.info('########QUERY### {0}', sqlQuery)
    sql.eachRow([:], sqlQuery, closure)
}

Object handleGetLatestSyncToken(Sql sql) {
    switch (objectClass) {
        case ObjectClass.ACCOUNT:
            return handleSyncToken(sql, 'webidm_accounts')
        case BaseScript.ENTITLEMENT:
            return handleSyncToken(sql, 'webidm_entitlements')
        default:
            throw new ConnectorException("Unknown object class " + objectClass)
    }
}

Object handleSyncToken(Sql sql, String table) {
    def first = sql.firstRow("SELECT MAX(last_modified_date) AS max_modification, LOCALTIMESTAMP AS now FROM " + table)
    log.info("VVVVVV {0}", first.values())
    if (first.values() != null &&  first.values()[0] != null) {
        def maxLastModification = first.values()[0]
//        log.info("#### values {0} {1}", maxLastModification, first)
        return maxLastModification.toString()
    } else {
        def now = first.values()[1]
        log.info("NOW IS {0}", now.toString())
        return now.toString()
    }
}
