# Todo's

## Entitlements to roles

-   Map basic WebIDM role to LDAP
-   Split up the entitlements name to (meta) role
-   Assign the entitlement to the effective account via accountId
-   Map the organisation info in entitlement to Org structure (assignmentTargetSearch?)
-   Privileges in entitlement to effective role via mapping somewhere (dynamic and GUI editable)
-   Contact info stays on entitlement (find a way to map it to LDAP)
-   Map everything (orgs, roles, contactinfo) into LDAP structure


- Eerst uitzoeken waarom:
    - JiraUser assignment -> meta niet de group in LDAP aanmaakt (maar wel als je dat via de GUI doet)
        moet de assignment in de andere richting liggen mss meta -> JiraUser??? => NEEN de gui doet iets speciaals dat wij niet doen
            doet roleMembershipRef iets extra naast assigment van JirUser -> meta role???   NOPE!
            doet activation iets extra ?  NOPE!
        
        Hmm: Blijkt dat wanneer je de link in de GUI maakt de JiraUser group direct in LDAP komt. Maar als je hem in XML doet dan niet maar wel nadat je de JiraUser reconciled.
            => mss toch eens die recurring task om op openDJ resource de rollen te syncen aanzetten zodat die direct worden aangemaakt (shadow) => NOPE! die task doet niet uit JiraUser wordt niet initieel in LDAP gepompt
            
            
    - Unassign van jirauser doet de ref niet verdwijnen
    - Daarna terug opbouwen naar een hierarchy
    - Terug RRN introduceren in opendj resource schema
    
    
    💥💥💥💥💥💥 Het is nu tijd om te kijken wat het gedrag is van verwijderen van rollen in de posix-group branch 💥💥💥💥💥💥 
        => blijkbaar zijn we niet de enigste: http://lists.evolveum.com/pipermail/midpoint/2016-April/001720.html
        en ook: http://lists.evolveum.com/pipermail/midpoint/2016-November/002807.html
        en: http://lists.evolveum.com/pipermail/midpoint/2016-November/002843.html

- Split up the Account in multiple intents according to CUG (Closed User Groups)

        schema handling configuration must define intents for them and synchronization configuration must define conditions which would then identify the intent and the correlation rule.
        
        AR|EA|GID|LB|OV

- Ook interessant om uit te zoeken:
    Indien een gebruiker een technische role induced krijgt of je een attribuut aan die gebruiker kan toekennen 
    mogelijkse oplossing: focusMappings in inducement?

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

- Dit al gezien?
       
       https://wiki.evolveum.com/display/midPoint/Self+Registration+Configuration
       
- Er is nog da probleemke met single run en non association tussen account en entitlements
    -> met tolerant setting op account.entitlements attribuut spelen
    
- Bij herhangen entitlement in db krijgen we lingering uniqueMember attributen in LDAP
    Echter de use-case is niet echt real-world denk ik aangezien er enkel entitlements deleted/created worden en nooit herhangen
    Maar desalwelteplus die non real-world use-case lingering uniqueMembers sounds a lot like this thread:
    
    Sounds a lot like this: http://lists.evolveum.com/pipermail/midpoint/2018-September/004954.html
    or this: http://lists.evolveum.com/pipermail/midpoint/2016-November/002807.html
    
- Probleem met de juiste technische rol namen in LDAP te krijgen en (ander probleem) deletes in orde te krijgen:
    Is het een idee om ook voor de technische rollen assignmentTargetSearch te gebruiken
    (en ze dus effectief in midPoint te definiëren en ze dan pas te syncen naar LDAP zonder inducements)
    vooral om die zottigheid met 
       
       What the fuck is roleMembershipRef onder role-config? zie attachments http://lists.evolveum.com/pipermail/midpoint/2016-November/002849.html
       
       Weg naar oplossing: com.evolveum.midpoint.provisioning op debug zetten