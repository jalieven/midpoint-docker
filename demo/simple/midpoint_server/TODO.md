# Todo's

## Entitlements to roles

-   Map basic WebIDM role to LDAP
-   Split up the entitlements name to (meta) role
-   Assign the entitlement to the effective account via accountId
-   Map the organisation info in entitlement to Org structure (assignmentTargetSearch?)
-   Privileges in entitlement to effective role via mapping somewhere (dynamic and GUI editable)
-   Contact info stays on entitlement (find a way to map it to LDAP)
-   Map everything (orgs, roles, contactinfo) into LDAP structure



- When entitlements are created it has privileges (meta-roles),
    those meta-roles could be pre-populated and could have inducements to technical roles
    which is a mapping that can be done in the GUI?
    
- Multiple accounts in one resource: can we correlate this to one user in midpoint?


    Voorheen:
    
    http://lists.evolveum.com/pipermail/midpoint/2016-May/001921.html
    
    Yes, midPoint is capable to have multiple accounts linked to the same
    user. Even multiple accounts (better: projections) on the same resource.
    But if the various accounts are on the same resource, schema handling
    configuration must define intents for them, and synchronization
    configuration must define conditions which would then identify the
    intent and the correlation rule.
    
    http://lists.evolveum.com/pipermail/midpoint/2017-December/004272.html
    
    Yes, kind/intent is fixed. And, strictly speaking it has to remain fixed 
    because resource+kind+intent triple determines object type, which 
    determines the schema (and some policies). That is how midPoint is 
    implemented now.
    
    But, despite that, there are places where we do not need to be that 
    strict. Theoretically intent could be determined dynamically at some 
    places in the future. But there may be limitations (e.g. resource wizard 
    will not work properly). And this is going to make the code a bit more 
    complex than it is now. Therefore we will do that only if there is 
    funding for development of this feature and funding for maintenance as 
    well (i.e. platform subscription is needed).
    
    On doing this with multiple intents and sync them to the correct schema:
        
        http://lists.evolveum.com/pipermail/midpoint/2017-February/003316.html
        (dit is enkel als we voor elke account kunnen nagaan in welk web-idm-context/intent die hoort)
        
        Nu momenteel in OpenAM web idm implementatie (waar zit die intent distinctie? attribute name? en blijft die fixed?):
        
            Map<String, String> accountAttributes = getAccountAttributes(account.getAttributes());
                  provisioningService.createAccount(new ValidCreateAccount(
                      account.getAccountId(),
                      accountAttributes.get("Voornaam"),
                      accountAttributes.get("Naam"),
                      accountAttributes.get("RRN"),
                      account.isEnabled(),
                      account.isLocked(),
                      getRealm()));
        
    MAAR: https://wiki.evolveum.com/display/midPoint/Multiaccounts+HOWTO
            => does not work as advertised
            => en heeft UI limitations
    
    MSS: https://wiki.evolveum.com/display/midPoint/Persona+Configuration
            => ook met zijn limitaties maar vereist pre-manipulatie van de resource

- Uitzoeken hoe je een posix user insert (projection aan een user)

    Extra account met verschillende intent geeft een nieuwe projection?

- En systeemgebruikers die niet inbound van webIdm komen

- 