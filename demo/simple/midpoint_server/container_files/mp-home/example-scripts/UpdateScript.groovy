import groovy.sql.Sql;
import groovy.sql.DataSet;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.common.security.SecurityUtil;
import java.security.MessageDigest;
import groovy.time.TimeCategory;
import org.apache.commons.codec.digest.UnixCrypt;
import org.apache.commons.lang3.RandomStringUtils;

log.info("Entering REZERWACJA "+action+" Script");

def sql = new Sql(connection);
def doCommit = false;
def preparedStatementPrefixAccounts = "UPDATE users SET ";
def preparedStatementAttributes = "";
def preparedStatementAttributesList = [];
def preparedStatementColumns = [];
def accountAttrNames = ["__NAME__","__PASSWORD__","__ENABLE__","fname","lname","email","organization","position","phone","timezone","language","homepageid","groups","resources"];

switch ( action ) {

    case "UPDATE":
    switch ( objectClass ) {
        case "__ACCOUNT__":

        for (attr in accountAttrNames) {
            if (attributes.get(attr) != null) {
                switch (attr) {
                    case "__NAME__":
                        // __NAME__ to kolumna "username"
                        preparedStatementAttributesList.add("username" + " = ?");
                        preparedStatementColumns.add(attributes.get(attr)?.find { true });
                        break;
                    case "__ENABLE__":
                        // enableattr: 1 - aktywny, 3 - zablokowany
                        def status = attributes.get(attr);
                        def enableattr = (attributes.get(attr)?.find { true }) ? 1 : 3;
                        preparedStatementAttributesList.add("status_id" + " = ?");
                        preparedStatementColumns.add(enableattr);
//log.error("REZERWACJA ENABLEATTR: "+enableattr+" , USERNAME: "+uid);

                        break;
                    case "__PASSWORD__":
                        // Odczytanie hasla w formie otwartego tekstu i zahaszowanie go SHA1 + SALT
			//def clearpass = midpoint.getPlaintextUserPassword(user).toString();
			if ( attributes?.get("__PASSWORD__")?.get(0)?.toString() != null ) {
			def SALT = org.apache.commons.lang.RandomStringUtils.randomAlphanumeric(8);
			def clearpass = SecurityUtil.decrypt(attributes?.get("__PASSWORD__")?.get(0))?.toString() + SALT;
			String.metaClass.toSHA1 = { salt = "" ->
			def messageDigest = MessageDigest.getInstance("SHA1")
			messageDigest.update(salt.getBytes())
			messageDigest.update(delegate.getBytes())
			new BigInteger(1, messageDigest.digest()).toString(16).padLeft(40, '0')
			}
			sha1pass = clearpass.toSHA1('');

//log.error("REZERWACJA SALT: "+SALT+" , PASSSHA1: "+sha1pass+" , USERNAME: "+uid);
                	sql.executeUpdate("UPDATE users SET password=?, salt=? WHERE user_id=?",[sha1pass,SALT,uid]);
                        //preparedStatementAttributesList.add("password" + " = ?");
                        //preparedStatementColumns.add(cryptedpass);
                        }
                        break;
        default:
	// wszystkie pozostałe atrybuty
        preparedStatementAttributesList.add(attr + " = ?");
        preparedStatementColumns.add(attributes.get(attr)?.find { true });
        }
        }
        }

        preparedStatementAttributes = preparedStatementAttributesList.join(',');

	if (preparedStatementAttributes != "") {
	    preparedStatementColumns.add(uid as String);
	    sql.executeUpdate(preparedStatementPrefixAccounts + preparedStatementAttributes + " WHERE user_id = ?", preparedStatementColumns);
	    doCommit = true;
	}

        //if (doCommit) {
	//	sql.commit();
	//}
        break;

        case "Group":

        break

        default:
        uid;
    }
    break

    case "ADD_ATTRIBUTE_VALUES":

        if(objectClass == "__ACCOUNT__")
        {
            for(String grupa : attributes.get("groups"))
            {
            def groupid = sql.firstRow("SELECT group_id as gid FROM groups WHERE name=?",[grupa as String])?.gid;
                def existingEntitlement = sql.rows("SELECT 1 FROM user_groups WHERE user_id=? AND group_id=?",[uid, groupid as Integer]);

                if(existingEntitlement.isEmpty())
                {
                    //log.error("Dodaję entitlement ${grupa} użytkownikowi ${uid}");
                    sql.executeInsert("INSERT INTO user_groups (user_id,group_id) values (?,?)",[uid,groupid as Integer]);
                }
                else
                {
                    log.info("Nie mam nic do roboty");
                    //log.error("NIE Dodaję entitlement ${grupa} użytkownikowi ${uid}");
                }
            }

            for(String zasob : attributes.get("resources"))
            {
            def resourceid = sql.firstRow("SELECT resource_id as rid FROM resources WHERE name=?",[zasob as String])?.rid;
                def existingEntitlement = sql.rows("SELECT 1 FROM user_resource_permissions WHERE user_id=? AND resource_id=?",[uid, resourceid as Integer]);

                if(existingEntitlement.isEmpty())
                {
                    //log.error("Dodaję entitlement ${zasob} użytkownikowi ${uid}");
                    sql.executeInsert("INSERT INTO user_resource_permissions (user_id,resource_id,permission_id,permission_type) values (?,?,1,0)",[uid,resourceid as Integer]);
                }
                else
                {
                    log.info("Nie mam nic do roboty");
                    //log.error("NIE Dodaję entitlement ${zasob} użytkownikowi ${uid}");
                }
            }
            

        }

	break

    case "REMOVE_ATTRIBUTE_VALUES":

        if(objectClass == "__ACCOUNT__")
        {
            for(String grupa : attributes.get("groups"))
            {
            def groupid = sql.firstRow("SELECT group_id as gid FROM groups WHERE name=?",[grupa as String])?.gid as Integer;
                def existingEntitlement = sql.rows("SELECT 1 FROM user_groups WHERE user_id=? AND group_id=?",[uid, groupid]);

                if(existingEntitlement.isEmpty())
                {
                    log.info("Nie mam nic do roboty");
                }
                else
                {
                    //log.error("Usuwam grupę ${grupa} z użytkownika ${uid}");
                    sql.execute("DELETE FROM user_groups WHERE group_id=? AND user_id=?",[groupid,uid]);
                }
            }

            for(String zasob : attributes.get("resources"))
            {
            def resourceid = sql.firstRow("SELECT resource_id as rid FROM resources WHERE name=?",[zasob as String])?.rid as Integer;
                def existingEntitlement = sql.rows("SELECT 1 FROM user_resource_permissions WHERE user_id=? AND resource_id=?",[uid, resourceid]);

                if(existingEntitlement.isEmpty())
                {
                    log.info("Nie mam nic do roboty");
                }
                else
                {
                    //log.error("Usuwam grupę ${grupa} z użytkownika ${uid}");
                    sql.execute("DELETE FROM user_resource_permissions WHERE resource_id=? AND user_id=?",[resourceid,uid]);
                }
            }
        }

        break

    default:
    uid
}

return uid;
