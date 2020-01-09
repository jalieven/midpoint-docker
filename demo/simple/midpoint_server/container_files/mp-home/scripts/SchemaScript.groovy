import org.forgerock.openicf.connectors.scriptedsql.ScriptedSQLConfiguration
import org.forgerock.openicf.misc.scriptedcommon.ICFObjectBuilder
import org.forgerock.openicf.misc.scriptedcommon.OperationType
import org.identityconnectors.common.logging.Log
import org.identityconnectors.framework.common.objects.ObjectClass
import org.identityconnectors.framework.common.objects.OperationOptionInfoBuilder
import org.identityconnectors.framework.spi.operations.SearchOp
import org.identityconnectors.framework.common.objects.OperationalAttributeInfos

import static org.identityconnectors.framework.common.objects.AttributeInfo.Flags.MULTIVALUED

def log = log as Log
def operation = operation as OperationType
def builder = builder as ICFObjectBuilder
def configuration = configuration as ScriptedSQLConfiguration

log.info("#### Entering {0} Script", operation)

builder.schema({
    objectClass {
        type ObjectClass.ACCOUNT_NAME
        attributes {
            firstname()
            lastname()
            rijksregisternummer()
            entitlements String.class, MULTIVALUED
            OperationalAttributeInfos.ENABLE
            OperationalAttributeInfos.LOCK_OUT
        }
    }
    objectClass {
        type BaseScript.ENTITLEMENT_NAME
        attributes {
            accountId()
            privileges String.class, MULTIVALUED
            OperationalAttributeInfos.ENABLE
        }
    }
//    objectClass {
//        type BaseScript.ORGANIZATION
//        attributes {
//            name()
//            description()
//        }
//    }

    defineOperationOption OperationOptionInfoBuilder.buildPagedResultsOffset(), SearchOp
    defineOperationOption OperationOptionInfoBuilder.buildPageSize(), SearchOp
})