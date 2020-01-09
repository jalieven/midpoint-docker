import groovy.sql.Sql;
import groovy.sql.DataSet;

log.info("Entering "+action+" Script");
def sql = new Sql(connection);

sql.eachRow("select * from users", { println it.user_id} );
