import groovy.sql.Sql;
import groovy.sql.DataSet;

log.info("Entering "+action+" Script");

def sql = new Sql(connection);
def result = []
def where = "";

if (query != null){
    //Need to handle the __UID__ in queries
    if (query.get("left").equalsIgnoreCase("__UID__") && objectClass.equalsIgnoreCase("__ACCOUNT__")) query.put("left","u.user_id");
    if (query.get("left").equalsIgnoreCase("__NAME__") && objectClass.equalsIgnoreCase("__ACCOUNT__")) query.put("left","u.username");

    if (query.get("left").equalsIgnoreCase("__UID__") && objectClass.equalsIgnoreCase("Resource")) query.put("left","r.resource_id");
    if (query.get("left").equalsIgnoreCase("__NAME__") && objectClass.equalsIgnoreCase("Resource")) query.put("left","r.name");

    if (query.get("left").equalsIgnoreCase("__UID__") && objectClass.equalsIgnoreCase("Group")) query.put("left","g.group_id");
    if (query.get("left").equalsIgnoreCase("__NAME__") && objectClass.equalsIgnoreCase("Group")) query.put("left","g.name");

    // We can use Groovy template engine to generate our custom SQL queries
    def engine = new groovy.text.SimpleTemplateEngine();

    def whereTemplates = [
        CONTAINS:' WHERE $left ${not ? "NOT " : ""}LIKE "%$right%"',
        ENDSWITH:' WHERE $left ${not ? "NOT " : ""}LIKE "%$right"',
        STARTSWITH:' WHERE $left ${not ? "NOT " : ""}LIKE "$right%"',
        EQUALS:' WHERE $left ${not ? "<>" : "="} \'$right\'',
        GREATERTHAN:' WHERE $left ${not ? "<=" : ">"} "$right"',
        GREATERTHANOREQUAL:' WHERE $left ${not ? "<" : ">="} "$right"',
        LESSTHAN:' WHERE $left ${not ? ">=" : "<"} "$right"',
        LESSTHANOREQUAL:' WHERE $left ${not ? ">" : "<="} "$right"'
    ]

    def wt = whereTemplates.get(query.get("operation"));
    def binding = [left:query.get("left"),right:query.get("right"),not:query.get("not")];
    def template = engine.createTemplate(wt).make(binding);
    where = template.toString();
    log.ok("Search WHERE clause is: "+ where)
}

switch ( objectClass ) {
    case "__ACCOUNT__":
	//zasoby
	def zasobyquery = "select r.name from resources r join user_resource_permissions ur on ur.resource_id = r.resource_id join users u on u.user_id = ur.user_id" + where;
	def zasobyresults = sql.rows(zasobyquery);
	def zasobylist = zasobyresults.name as List;

	//grupy
	def groupsquery = "select g.name from groups g join user_groups ug on ug.group_id = g.group_id join users u on u.user_id = ug.user_id" + where;
	def groupsresults = sql.rows(groupsquery);
	def groupslist = groupsresults.name as List;

	// konta
        sql.eachRow("select u.user_id, u.fname, u.lname, u.username, u.email, u.organization, u.position, u.phone, u.timezone, u.language, u.homepageid, case u.status_id when 1 then 1 else 0 end as status from users u" + where) { row ->
        result.add([
        __UID__:row.user_id,
        __NAME__:row.username,
        __ENABLE__:row.status.asBoolean(),
        fname:row.fname,
        lname:row.lname,
        email:row.email,
        organization:row.organization,
        position:row.position,
        phone:row.phone,
        timezone:row.timezone,
        language:row.language,
        homepageid:row.homepageid,
        groups:groupslist,
        resources:zasobylist
        ]);
        }
    break

    case "Resource":
//	log.error("REZ: Listuję ZASOBY");
	//lista użytkowników mających dostęp do zasobu
	def membersquery = "select u.username from users u join user_resource_permissions rp on u.user_id = rp.user_id join resources r on r.resource_id = rp.resource_id" + where;
        def membersresults = sql.rows(membersquery);
        def rmembers = membersresults.username as List
        	// grupy
        sql.eachRow("select r.resource_id,r.name from resources r" + where,
        {result.add([__UID__:it.resource_id, __NAME__:it.name,resourcemembers:rmembers])} );

    break

    case "Group":
	//lista członków grupy
//	log.error("REZ: Listuję GRUPĘ");
//	def groupmembersquery = "select u.username from users u join user_groups g on u.user_id = g.user_id" + where;
//        def groupmembersresults = sql.rows(groupmembersquery);
//        def groupmembers = groupmembersresults.username as List
        def groupmembers = [];
        	// grupy
        sql.eachRow("select g.group_id, g.name from groups g" + where,
        {result.add([__UID__:it.group_id, __NAME__:it.name,members:groupmembers])} );

    break


    default:
    result;
}

return result;
