import groovy.sql.Sql;

import org.forgerock.openicf.connectors.scriptedsql.ScriptedSQLConfiguration
import org.forgerock.openicf.misc.scriptedcommon.OperationType
import org.identityconnectors.common.logging.Log

import java.sql.Connection

def log = log as Log
def operation = operation as OperationType
def connection = connection as Connection
def configuration = configuration as ScriptedSQLConfiguration

log.info("Entering " + operation + " Script")

log.info("Using driver: {0} version: {1}",
        connection.getMetaData().getDriverName(),
        connection.getMetaData().getDriverVersion())

Sql sql = new Sql(connection)

sql.eachRow("SELECT 1", {})


