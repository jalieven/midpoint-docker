import org.identityconnectors.common.logging.Log
import org.identityconnectors.framework.common.objects.AttributeInfo
import org.identityconnectors.framework.common.objects.AttributeInfoBuilder
import org.identityconnectors.framework.common.objects.ObjectClass
import org.identityconnectors.framework.common.objects.ObjectClassInfoBuilder
import org.identityconnectors.framework.common.objects.OperationalAttributeInfos

def log = log as Log

log.ok("Schema script execution started.");

Set<AttributeInfo.Flags> flags = new HashSet<AttributeInfo.Flags>();
flags.add(AttributeInfo.Flags.MULTIVALUED);

ObjectClassInfoBuilder account = new ObjectClassInfoBuilder();
account.setType(ObjectClass.ACCOUNT_NAME);
account.addAttributeInfo(AttributeInfoBuilder.build("fname", String.class));
account.addAttributeInfo(AttributeInfoBuilder.build("lname", String.class));
account.addAttributeInfo(AttributeInfoBuilder.build("email", String.class));
account.addAttributeInfo(AttributeInfoBuilder.build("organization", String.class));
account.addAttributeInfo(AttributeInfoBuilder.build("position", String.class));
account.addAttributeInfo(AttributeInfoBuilder.build("phone", String.class));
account.addAttributeInfo(AttributeInfoBuilder.build("timezone", String.class));
account.addAttributeInfo(AttributeInfoBuilder.build("language", String.class));
account.addAttributeInfo(AttributeInfoBuilder.build("homepageid", Integer.class));
account.addAttributeInfo(AttributeInfoBuilder.build("resources", String.class, flags));
account.addAttributeInfo(AttributeInfoBuilder.build("groups", String.class, flags));
account.addAttributeInfo(OperationalAttributeInfos.ENABLE);
account.addAttributeInfo(OperationalAttributeInfos.PASSWORD);
account.addAttributeInfo(OperationalAttributeInfos.LOCK_OUT); //locked FIXME

ObjectClassInfoBuilder resource = new ObjectClassInfoBuilder();
resource.setType("Resource");
resource.addAttributeInfo(AttributeInfoBuilder.build("resourcemembers", String.class, flags));

ObjectClassInfoBuilder group = new ObjectClassInfoBuilder();
group.setType("Group");
group.addAttributeInfo(AttributeInfoBuilder.build("members", String.class, flags));

builder.defineObjectClass(account.build());
builder.defineObjectClass(resource.build());
builder.defineObjectClass(group.build());

log.ok("Schema script execution finished.");
