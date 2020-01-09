import groovy.sql.Sql;
import groovy.sql.DataSet;
import org.identityconnectors.framework.common.exceptions.UnknownUidException

log.info("Entering "+action+" Script");
def sql = new Sql(connection);

assert uid != null

switch ( objectClass ) {
    case "__ACCOUNT__":
    sql.execute("DELETE FROM user_groups WHERE user_id=?",[uid as Integer])
    sql.execute("DELETE FROM user_resource_permissions WHERE user_id=?",[uid as Integer])
    sql.execute("DELETE FROM user_preferences WHERE user_id=?",[uid as Integer])
    sql.execute("DELETE FROM user_email_preferences WHERE user_id=?",[uid as Integer])
    sql.execute("DELETE FROM users WHERE user_id=?",[uid as Integer])
//    count = sql.updateCount;
    //sql.commit();
    break

    default:
    uid;
}

//if (count != 1) {
//    throw new UnknownUidException("Couldn't found and delete object $objectClass with uid $uid")
//}
