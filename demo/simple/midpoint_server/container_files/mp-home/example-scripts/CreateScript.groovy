import groovy.sql.Sql;
import groovy.sql.DataSet;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.common.security.SecurityUtil;
import org.apache.commons.lang3.RandomStringUtils;
import java.security.*
import groovy.time.TimeCategory;
import static java.util.UUID.randomUUID;

log.info("Entering "+action+" Script");

def sql = new Sql(connection);

String newUid; //Create must return UID.


switch ( objectClass ) {
    case "__ACCOUNT__":
    //Generowanie SALTa
    def SALT = org.apache.commons.lang.RandomStringUtils.randomAlphanumeric(8);
    // Hashowanie hasla SHA1 + SALT
    def clearpass = SecurityUtil.decrypt(attributes?.get("__PASSWORD__")?.get(0))?.toString() + SALT;
    String.metaClass.toSHA1 = { salt = "" ->
    def messageDigest = MessageDigest.getInstance("SHA1")
      messageDigest.update(salt.getBytes())
      messageDigest.update(delegate.getBytes())
      new BigInteger(1, messageDigest.digest()).toString(16).padLeft(40, '0')
      }
    sha1pass = clearpass.toSHA1('');
    // bieżąca data
    curdate = new Date().format('yyyy-MM-dd HH:mm:ss');

    // tworzenie nowego konta

    def keys = sql.executeInsert("INSERT INTO users (fname,lname,username,email,password,salt,organization,position,phone,timezone,language,homepageid,status_id,date_created) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
        [   attributes?.get("fname")?.get(0),
            attributes?.get("lname")?.get(0),
            id,
            attributes?.get("email")?.get(0),
            sha1pass,
            SALT,
            attributes?.get("organization")?.get(0),
            attributes?.get("position")?.get(0),
            attributes?.get("phone")?.get(0),
            attributes?.get("timezone")?.get(0),
            attributes?.get("language")?.get(0),
            attributes?.get("homepageid")?.get(0),
            attributes?.get("__ENABLE__")?.get(0) ? 1 : 3,
	    curdate
        ]);
	newUid = sql.firstRow("select user_id from users where username=?",[id]).user_id as Integer;
	//dodanie uprawnień do domyślnej sali konf.
//	sql.executeInsert("INSERT INTO user_resource_permissions values (?,1,1,0)",[newUid]);

    break

    case "Group":

    break

}


return newUid;
