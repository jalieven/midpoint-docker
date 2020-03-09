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

        String entitlementsQuery = "SELECT s_ent.entitlementid FROM source_entitlements s_ent WHERE s_ent.accountid = '" + row.accountid + "' AND s_ent.deleted IS NULL"
        List<GroovyRowResult> entitlementsResults = sql.rows(entitlementsQuery);
        List entitlementIds = entitlementsResults.entitlementid as List;
        log.info("#### Found Entitlements: {0} for Account: {1}", entitlementIds, row.accountid)

        ConnectorObjectBuilder connObjBuilder = new ConnectorObjectBuilder();
        connObjBuilder.setObjectClass(ObjectClass.ACCOUNT)
        connObjBuilder.addAttribute(new Uid(row.accountid))
        connObjBuilder.addAttribute(new Name(row.rijksregisternummer))
        connObjBuilder.addAttribute(AttributeBuilder.build('username', row.username))
        connObjBuilder.addAttribute(AttributeBuilder.build('firstname', row.firstname))
        connObjBuilder.addAttribute(AttributeBuilder.build('lastname', row.lastname))
        connObjBuilder.addAttribute(AttributeBuilder.build('rijksregisternummer', row.rijksregisternummer))
        connObjBuilder.addAttribute(AttributeBuilder.build('entitlements', entitlementIds))
        SyncDeltaBuilder synDeltaBuilder = new SyncDeltaBuilder();
        synDeltaBuilder.setToken(new SyncToken(row.lastmodification.toString()));
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
    log.info("SELECT * FROM source_accounts WHERE lastmodification > TO_TIMESTAMP('" + token + "', 'YYYY-MM-DD HH24:MI:SS.US')")
    sql.eachRow([:], "SELECT * FROM source_accounts WHERE lastmodification > TO_TIMESTAMP('" + token + "', 'YYYY-MM-DD HH24:MI:SS.US')", closure)
}

void handleEntitlementSync(Sql sql, String token, SyncResultsHandler handler) {
    Closure closure = { row ->
        log.info("#### Entitlements Row from DB: {0} ####", row)
        ConnectorObjectBuilder connObjBuilder = new ConnectorObjectBuilder();
        connObjBuilder.setObjectClass(BaseScript.ENTITLEMENT)
        connObjBuilder.addAttribute(new Uid(row.entitlementid))
        connObjBuilder.addAttribute(new Name(row.entitlementid))
        connObjBuilder.addAttribute(AttributeBuilder.build('__ENABLE__', !row.disabled))
        connObjBuilder.addAttribute(AttributeBuilder.build('accountId', row.accountid))
        connObjBuilder.addAttribute(AttributeBuilder.build('privileges', row.privileges.split(";")))
        connObjBuilder.addAttribute(AttributeBuilder.build('organisatiecode', row.organisatiecode))
        connObjBuilder.addAttribute(AttributeBuilder.build('departement', row.departement))
        connObjBuilder.addAttribute(AttributeBuilder.build('dienst', row.dienst))
        connObjBuilder.addAttribute(AttributeBuilder.build('functie', row.functie))
        connObjBuilder.addAttribute(AttributeBuilder.build('email', row.email))
        connObjBuilder.addAttribute(AttributeBuilder.build('personeelsnummer', row.personeelsnummer))
        connObjBuilder.addAttribute(AttributeBuilder.build('fax', row.fax))
        connObjBuilder.addAttribute(AttributeBuilder.build('gsm', row.gsm))
        connObjBuilder.addAttribute(AttributeBuilder.build('telefoonnr', row.telefoonnr))
        SyncDeltaBuilder synDeltaBuilder = new SyncDeltaBuilder();
        synDeltaBuilder.setToken(new SyncToken(row.lastmodification.toString()));
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
    log.info("SELECT * FROM source_entitlements WHERE lastmodification > TO_TIMESTAMP('" + token + "', 'YYYY-MM-DD HH24:MI:SS.US')")
    sql.eachRow([:], "SELECT * FROM source_entitlements WHERE lastmodification > TO_TIMESTAMP('" + token + "', 'YYYY-MM-DD HH24:MI:SS.US')", closure)
}

Object handleGetLatestSyncToken(Sql sql) {
    switch (objectClass) {
        case ObjectClass.ACCOUNT:
            return handleSyncToken(sql, 'source_accounts')
        case BaseScript.ENTITLEMENT:
            return handleSyncToken(sql, 'source_entitlements')
        default:
            throw new ConnectorException("Unknown object class " + objectClass)
    }
}

Object handleSyncToken(Sql sql, String table) {
    def first = sql.firstRow("SELECT MAX(lastmodification) AS max_modification, now() AT TIME ZONE 'UTC' AS now FROM " + table)
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
