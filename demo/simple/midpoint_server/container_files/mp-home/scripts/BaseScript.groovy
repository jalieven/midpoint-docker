import org.identityconnectors.framework.common.objects.ObjectClass

class BaseScript extends Script {

    public static final String ENTITLEMENT_NAME = "Entitlement"

    public static final ObjectClass ENTITLEMENT = new ObjectClass(BaseScript.ENTITLEMENT_NAME)

    public static final String ORGANIZATION_NAME = "Organization"

    public static final ObjectClass ORGANIZATION = new ObjectClass(BaseScript.ORGANIZATION_NAME)

    @Override
    Object run() {
        return null
    }
}