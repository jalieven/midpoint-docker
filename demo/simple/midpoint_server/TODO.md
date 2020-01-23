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
       
- On to the debugging round om probleem te onderzoeken waarom uniqueMembers niet worden uitgekuisd nadat Entitlements wegvallen:

    Interesting sites to start debugging:
    
        * com.evolveum.midpoint.model.common.mapping.MappingImpl ("MAPPING  in for association") -> trace van de associationFromLink setting in inducement van onze Meta-role
        
                In de call stack interessante sites:
                    - com.evolveum.midpoint.model.impl.lens.projector.focus.AssignmentProcessor#evaluateConstructions
                        Wnn we de entitlement deleten: waarom returned com.evolveum.midpoint.model.impl.lens.projector.focus.AssignmentProcessor#processAssignmentsProjectionsWithFocus early (door focusDelta.isDelete())?
                        en komen we nooit verder in die method waar dan de Projections worden evaluated (onze fameuze associationFromLink in de meta-role)
                        of is het processFocusDelete waar de magic happens?
                        
                            Vragen/bedenkingen die ik mij hierbij stel:
                                * Ik krijg het gevoel dat de afhandeling van zo een delete en zijn weerslag in LDAP niet in deze life-cycle gebeurt (eerder in een reconcile task ofzo)
                                    Indien dit niet het geval is dan valt de AssignmentProcessor steeds eruit en worden er geen assigments processed bij een delete van entitlement => dan is mP wel degelijk brol.
                                    {UNEXPLORED}
                                * Mss moeten we eens beginnen zonder entitlements en dan com/evolveum/midpoint/model/impl/lens/projector/focus/AssignmentProcessor.java:215 debuggen
                                    
                                    💪 Uitgevoerd en niets bijgeleerd. Het aanmaken van de uniqueMember wordt niet in dat pad afgehandeld.
                                        Misschien is het interessanter om te vertrekken van de call naar de connector wnn die wordt aangemaakt om dan te kijken of er in die buurt ook een delete bestaat
                                        om dan de usage sites daarvan te onderzoeken
                                    
                                * Is het juist dat we een focus (entitlement) delete krijgen hier (niet eerste een unlink ofzo) {reactions}
                               
                                    💪 Het op unlink reaction zetten in een deleted situation doet ons over de focusDelta.isDelete() vliegen waardoor onderliggende code wordt uitgevoerd.
                                        Echter zonder success (nu wordt zelfs de entitlement niet verwijderd in midPoint zelf.
                                        Plots zie ik volgend interessant zinnetje in mP Confluence ivm die reaction config:
                                            'Using the normal (non-action) methods is also generally a preferred way because such definition is applied to all changes resulting in a consistent policy'
                                            Wat er hiermee juist wordt bedoelt is mij niet duidelijk: verwijderen van die expliciete config of ergens anders een config doen.
                                            (https://wiki.evolveum.com/display/midPoint/Synchronization+Configuration#SynchronizationConfiguration-Reactions)
                                            
                                * Zeer interessante comment op com/evolveum/midpoint/model/impl/lens/projector/focus/AssignmentProcessor.java:215
                                    => This is where most of the assignment-level action happens.
                                    {DEFINITELY_TO_EXPLORE} 
                               
                    - ook een tof hubke: com/evolveum/midpoint/model/impl/lens/projector/focus/AssignmentHolderProcessor.java:249
                        {UNEXPLORED}
                    - ook zeer wijze method om te onderzoeken (ook op andere usage sites):
                        com.evolveum.midpoint.model.impl.lens.projector.focus.AssignmentTripleEvaluator#isMemberOfInvocationResultChanged
                    - en ook AssignmentProcessor.processMembershipAndDelegatedRef
                
        * package org.identityconnectors.framework.spi.operations -> site waar alle acties naar de connectoren van vertrekken
            {UNEXPLORED}
        * package com.evolveum.midpoint.provisioning -> mss interessant om te zien wat er daar gebeurt wnn er een entitlement die er voorheen was niet meer zichtbaar is
            
            Tijdens provisioning (aanmaak uniqueMember) zie ik dit in de logs:
                2020-01-23 08:58:22,737 [PROVISIONING] [midPointScheduler_Worker-2] DEBUG (com.evolveum.midpoint.provisioning.impl.ResourceObjectConverter): PROVISIONING MODIFY operation on resource:d0811790-6420-11e4-86b2-3c9755567874(OpenDJ)
                 MODIFY object, object class Technical Role, identified by:
                  {
                    entryUUID: 88972d7b-6deb-4d94-b343-d039099d23c4
                    dn: cn=jirauser,ou=groups,dc=didm,dc=be
                  }
                 changes:
                  [
                    PropertyModificationOperation:
                      delta:
                        attributes/uniqueMember
                          ADD: uid=81071040575,ou=people,dc=didm,dc=be
                      matchingRule: {http://prism.evolveum.com/xml/ns/public/matching-rule-3}stringIgnoreCase
                  ]
                  
             Op plaats com/evolveum/midpoint/model/impl/lens/ChangeExecutor.java:936 zouden we moeten objectDelta's moeten krijgen met volgende gegevens als we entitlement verwijderen:
                {
                    - changeType: "MODIFY"
                    - modifications: [
                        / assignment, REMOVE (user-assigment)
                        / roleMembershipRef, REMOVE (om 20001-Milieumedewerker-01 te unassignen)
                    ]
                 }
                 en {
                    - changeType: "MODIFY"
                    - modifications: [
                        association, REMOVE -> valuesToDelete (name, shadowRef, identifiers) => ref naar JiraUser
                    ]
                    Bij het toevoegen van de entitlement komt deze laatste objectDelta met ADD van sn,cn, givenName, uid op account
                    
                De focusDelta opbouwen in com/evolveum/midpoint/model/impl/lens/ChangeExecutor.java:155 is wel belangrijk precies

             Bij delete van entitlement krijgen we volgende objectDelta's:
             
                {
                    - verwijderen van assignment (user-assigment van in scripted sql resource)
                    - replace roleMembershipRef (om 20001-Milieumedewerker-01 te unassignen)
                 } en {
                    - changeType: "MODIFY"
                    - modifications: [
                         - ADD van sn,cn, givenName, uid op account
                         🚨🚨🚨🚨 MAAR HIER DUS NIET DIE VERWACHTE MODIFICATION ZOALS WE DIE ZIEN BIJ CREATIE ENTITLEMENT 🚨🚨🚨🚨 
                           !!!! association, REMOVE -> valuesToDelete (name, shadowRef, identifiers) => ref naar JiraUser !!!!
                    ]
                 }
                 
              OK one step closer: In LensProjectionContext wordt er een secondaryDelta opgebouwd => usages vinden
                    => op het moment van verwijderen is het de direct de ReconciliationProcessor die de secondaryDelta opvult
                            ReconciliationProcessor.recordDelta -> ".reconcileProjectionAttribute => ".reconcileProjectionAttributes
                                !!! iets verderop de call  naar reconcileProjectionAttributes wordt reconcileProjectionAssociations (?? {UNEXPLORED} ??) uitgevoerd
            
                Just a thought: waarom deze logger eens niet op trace zetten en vanalles bijleren bij aanmaken:
                    {UNEXPLORED} com.evolveum.midpoint.model.impl.lens.projector.ReconciliationProcessor => TRACE     (DEES MOETEN WE ZEKER DOEN)
                        zeer belovende javadoc van die class:
                             * Processor that reconciles the computed account and the real account. There
                             * will be some deltas already computed from the other processors. This
                             * processor will compare the "projected" state of the account after application
                             * of the deltas to the actual (real) account with the result of the mappings.
                             * The differences will be expressed as additional "reconciliation" deltas.
        
                Nog warmer: ReconciliationProcessor.decideIfTolerateAssociation (zal tolerant er toch iets mee te maken hebben?)
                        Uit deze call komt de kennis dat areCValues mss wel moet opgevuld zijn?
                        !!! De areCValues zijn empty omdat de tweede call in ReconciliationProcessor.reconcileProjectionAssociations een lege associationsContainer terug geeft !!!!
                         
                    PRELIMINARY CONCLUSION SO FAR: wanneer de association op tolerant = false staat dan gaat de
                        ReconciliationProcessor.reconcileProjectionAssociations een association-delta toevoegen aan de objectDelta (diegene die we zien bij aanmaken en dus missen bij delete)
                        Gegeven dus dat de tweede call in ReconciliationProcessor.reconcileProjectionAssociations geen lege associationsContainer terug geeft
                        waardoor de areCValues worden opgevuld en er effectief die association-delta wordt toegevoegd.
                        
                    ANOTHER PRELIMINARY CONCLUSION: Waarom zit association niet in de items (private member) van com.evolveum.midpoint.prism.impl.PrismContainerValueImpl ???
                        Wat is de connectie met de vorige conclusie? Op een shadow object (account) in findContainer daarop wordt het item "association" gezocht maar niet gevonden
                        omdat de PrismContainerValueImpl een item mist zijnde "association" wat blijkbaar overeen komt met het feit dat de scripted-sql resource schemaHandling geen association gedefinieerd heeft.
                        Komen die items in PrismContainerValueImpl overeen met schema extension? want een association bij configureren resulteert niet in "association" toevoeging in die items...
                     
        
- Vragen voor DAASI:

    - Vragen wat dat zinneke betekent: Using the normal (non-action) methods is also generally a preferred way because such definition is applied to all changes resulting in a consistent policy in Confluence (https://wiki.evolveum.com/display/midPoint/Synchronization+Configuration#SynchronizationConfiguration-Reactions)
    - Ook daarbij wat meer uitleg vragen wat die zaken zoals unmatched/unlinked nu juist betekenen. Reactions uitleggen
    - Waarom die scripting task recompute voor die technische rollen om ze in LDAP te krijgen?
    - Die verschillende soorten tasks (reconcile/live/recompute/import), wat zijn de verschillen hier tussen en wanneer welke te runnen? Is het in onze use-case enkel nodig om een import task te runnen van tijd tot tijd?
    - Wanneer een entitlement bijkomt in DB dan hangt het af van of de entitlement import task eerst loopt en dan accounts pas om effectief in die run een ldap uniqueMember te genereren. Als de volgorde omgekeerd voordoet dan duurt het 2 runs van die tasks om alles in orde te krijgen.