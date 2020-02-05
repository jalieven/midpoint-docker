# Todo's

## Entitlements to roles

-   Map basic WebIDM role to LDAP
-   Split up the entitlements name to (meta) role
-   Assign the entitlement to the effective account via accountId
-   Map the organisation info in entitlement to Org structure (assignmentTargetSearch?)
-   Privileges in entitlement to effective role via mapping somewhere (dynamic and GUI editable)
-   Contact info stays on entitlement (find a way to map it to LDAP)
-   Map everything (orgs, roles, contactinfo) into LDAP structure

- Scenario checken voor X aantal users (hoe lang loopt zo een import task)
        
    Accounts import task:
    First run: Processed 500 objects in 89 seconds
    Second run: Processed 500 objects in 46 seconds
    
    Entitlements import task:
    First run: Processed 1500 objects in 62 seconds
    Second run: Processed 1500 objects in 36 seconds
    
    Reconcile accounts task:
    First run: Processed 500 account(s), got 0 error(s). Average time for one object: 77.67 ms (wall clock time average: 84.246 ms).
    Second run: Processed 500 account(s), got 0 error(s). Average time for one object: 171.814 ms 
   
- Daarna wat scenarios runnen

    SCENARIOS:
    
        ⛔️ Aanpassen van de entitlement privileges:
        
            - Import van 1 account en 1 entitlement (met privileges A, B en C) dan reconciliation
            - Update van entitlement privileges naar B, D en E dan reconciliation
            - Zijn dan alle technische rollen ok?
                => momenteel heeft die entitlement privileges A, B, C, D en E (union dus) => 🚨🚨🚨🚨
        
                make restart_local_midpoint
                make insert_account
                make insert_entitlement
                make resume_import_task
                make update_entitlement_privileges
                make run_import_entitlements_task
                make run_reconciliation_accounts_task
                
                
        ⛔ Verwijderen van een account (en dus by definition ook zijn entitlements ref-integrity gewijs):
        
            - Import van 1 account en 1 entitlement dan reconciliation
            
                make restart_local_midpoint
                make insert_account
                make insert_entitlement
                make resume_import_task
                make delete_entitlement
                make delete_account
                make run_import_entitlements_task
                make run_import_accounts_task
                make run_reconciliation_accounts_task
                
                => LDAP ok
                🚨🚨🚨🚨 maar midPoint ziet nog de entitlement alsof die bestaat in PostgreSQL (projection is er nog steeds)
                   SHADOW (is not tombstoned):
                        <synchronizationSituation>linked</synchronizationSituation> 
                        <exists>true</exists>
                 en wanneer we die ene entitlement reconcilen:
                 
                    com.evolveum.midpoint.util.exception.ObjectNotFoundException: Object of type 'RoleType' with oid '6e2e9360-96b2-43b1-8bee-ae37ec68a286' was not found.     
                   
                !!! Hetzelfde gebeurt eigenlijk wanneer er gewoon een entitlement wordt verwijderd en een reconcile wordt getriggered op die entitlement
                dus die run_import_entitlements_task doet eigenlijk niets uit. midPoint gaat dan foobar bij insert en import van die entitlement komt het niet meer goed
                java.lang.IllegalStateException: More than one shadows found in repository for RAC(identifiers):[PCV(null):[RA({.../connector/icf-1/resource-schema-3}name):[PPV(String:20001-Milieumedewerker-01)]]]

                Gerelateerd daarmee is het volgende (zonder de import tasks te runnen):
                    make restart_local_midpoint
                    make insert_account
                    make insert_entitlement
                    make run_reconciliation_accounts_task

            - Oplossing:
            
                make restart_local_midpoint
                make insert_account
                make insert_entitlement
                make run_reconciliation_entitlements_task
                make run_reconciliation_accounts_task
                make delete_entitlement
                make delete_account
                make run_reconciliation_entitlements_task  (hier is de entitlement wel weg maar de assignment in account->entitlement is "Not Found")
                make run_reconciliation_accounts_task      (hier wordt dan de assignment opgekuisd maar wel dus die ObjectNotFoundException in de logs)

            HET IS DUS ZAAK OM EERST ALTIJD (ipv de import tasks) DE RECONCILIATION TASKS UIT TE VOEREN EN DAN BEST EERST DE ENTITLEMENTS EN DAN PAS ACCOUNTS
        
        
        ✅ Verwijderen van entitlement:
        
            - Import van 1 account en 1 entitlement (met privileges A, B en C) dan reconciliation
            - Delete van entitlement dan reconciliation
            - Zijn dan alle rollen ok.
                => idd de account heeft geen rol meer in LDAP en midPoint 
        
                make restart_local_midpoint
                make insert_account
                make insert_entitlement
                make resume_import_task
                make delete_entitlement
                make run_import_entitlements_task
                make run_reconciliation_accounts_task
               
        ✅ Aanpassen van account gegevens:
        
            - Import van 1 account en 1 entitlement (met privileges A, B en C) dan reconciliation
            - Aanpassen van account dan reconciliation
            - Is dan de account in orde? LDAP en midPoint in orde
            
                make restart_local_midpoint
                make insert_account
                make insert_entitlement
                make resume_import_task
                make update_account
                make run_import_accounts_task
                make run_reconciliation_accounts_task
                
         TO TEST Aanpassen van attributen (met uitzondering van privileges) van de entitlement:
         
         
         
         In de WebIDM provisioning SOAP service wordt er zo iets als renameAccount gedaan (en dan worden die accountIds old en new meegegeven)
            => Ik denk dus dat we het RRN als primary key best nemen?
            => Wat is het doel eigenlijk van deze operatie?
         
            


- REST connector fabriceren: https://wiki.evolveum.com/display/midPoint/REST+Connector+Superclass

- Outbound association leggen interessante thread: http://lists.evolveum.com/pipermail/midpoint/2016-September/002491.html

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
                        
                    ANOTHER PRELIMINARY CONCLUSION: Waarom zit association niet in de items (private member) van com.evolveum.midpoint.prism.impl.PrismContainerValueImpl ??? (user shadow of Scripted SQL resource)
                        Wat is de connectie met de vorige conclusie? Op een shadow object (account) in findContainer daarop wordt het item "association" gezocht maar niet gevonden
                        omdat de PrismContainerValueImpl een item mist zijnde "association" wat blijkbaar overeen komt met het feit dat de scripted-sql resource schemaHandling geen association gedefinieerd heeft.
                        
         🚨🚨🚨🚨🚨 WTF: you tellin' me this in a mailing list post: http://lists.evolveum.com/pipermail/midpoint/2016-May/001880.html   🚨🚨🚨🚨
        
            So what I see is that the shadow that gets written (xml in fullObject column) in the repo contains the association. 
                
                <shadow xmlns="http://midpoint.evolveum.com/xml/ns/public/common/common-3" xmlns:c="http://midpoint.evolveum.com/xml/ns/public/common/common-3" xmlns:icfs="http://midpoint.evolveum.com/xml/ns/public/connector/icf-1/resource-schema-3" xmlns:org="http://midpoint.evolveum.com/xml/ns/public/common/org-3" xmlns:q="http://prism.evolveum.com/xml/ns/public/query-3" xmlns:ri="http://midpoint.evolveum.com/xml/ns/public/resource/instance-3" xmlns:t="http://prism.evolveum.com/xml/ns/public/types-3" oid="954b8cfe-5c84-4f82-946f-0196dcec92c0" version="0">
                    <name>81071040575</name>
                    <resourceRef oid="dafe0482-faef-47b7-ae08-66b150929bb7" relation="org:default" type="c:ResourceType">
                        <!-- Scripted SQL -->
                    </resourceRef>
                    <objectClass>ri:AccountObjectClass</objectClass>
                    <primaryIdentifierValue>3fd83cd4-d1bb-4d7f-9a1a-12a02ed85a95</primaryIdentifierValue>
                    <kind>account</kind>
                    <exists>true</exists>
                    <attributes>
                        <icfs:name>81071040575</icfs:name>
                        <icfs:uid>3fd83cd4-d1bb-4d7f-9a1a-12a02ed85a95</icfs:uid>
                    </attributes>
                    <association id="1">
                        <name>ri:ents</name>
                        <!-- 🚨🚨🚨🚨 Take note that shadowRef is not saved 🚨🚨🚨🚨 -->
                        <identifiers>
                            <icfs:name>20001-Milieumedewerker-01</icfs:name>
                        </identifiers>
                    </association>
                    <activation/>
                </shadow>
                
            It is afterwards that this gets overwritten with a version that has no associations. Poof gone...
                
                <shadow xmlns="http://midpoint.evolveum.com/xml/ns/public/common/common-3" xmlns:c="http://midpoint.evolveum.com/xml/ns/public/common/common-3" xmlns:icfs="http://midpoint.evolveum.com/xml/ns/public/connector/icf-1/resource-schema-3" xmlns:org="http://midpoint.evolveum.com/xml/ns/public/common/org-3" xmlns:q="http://prism.evolveum.com/xml/ns/public/query-3" xmlns:ri="http://midpoint.evolveum.com/xml/ns/public/resource/instance-3" xmlns:t="http://prism.evolveum.com/xml/ns/public/types-3" oid="954b8cfe-5c84-4f82-946f-0196dcec92c0" version="1">
                    <name>81071040575</name>
                    <resourceRef oid="dafe0482-faef-47b7-ae08-66b150929bb7" relation="org:default" type="c:ResourceType"/>
                    <synchronizationTimestamp>2020-01-24T13:30:38.108Z</synchronizationTimestamp>
                    <fullSynchronizationTimestamp>2020-01-24T13:30:38.108Z</fullSynchronizationTimestamp>
                    <objectClass>ri:AccountObjectClass</objectClass>
                    <primaryIdentifierValue>3fd83cd4-d1bb-4d7f-9a1a-12a02ed85a95</primaryIdentifierValue>
                    <kind>account</kind>
                    <intent>webidm</intent>
                    <exists>true</exists>
                    <attributes xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:c="http://midpoint.evolveum.com/xml/ns/public/common/common-3" xsi:type="c:ShadowAttributesType">
                        <icfs:name>81071040575</icfs:name>
                        <icfs:uid>3fd83cd4-d1bb-4d7f-9a1a-12a02ed85a95</icfs:uid>
                    </attributes>
                    <activation/>
                </shadow>
               
            So remember that afterwards this shadow is used to calculate the delta's that needs to be performed (cfr the ReconciliationProcessor.decideIfTolerateAssociation method)
 
            Immediately we see that the following modifications made to the just created shadow:
                - relace intent, kind, synchronizationTimestamp and fullSynchronizationTimestamp
                    => it is in this replace that the association-part is removed and written into the repo
                        so the culprit resides in the com.evolveum.midpoint.repo.sql.helpers.ObjectUpdater#modifyObjectAttempt method call from this site:
                            com/evolveum/midpoint/repo/sql/SqlRepositoryServiceImpl.java:629
                         it is com.evolveum.midpoint.repo.sql.helpers.ObjectRetriever#getObjectInternal that retrieves the shadow but forgets to provide the association in its items
                         and finally it is com/evolveum/midpoint/repo/sql/helpers/ObjectRetriever.java:557 that removes the association from the shadow
                         with the ominous comment: "we store it because provisioning now sends it to repo, but it should be transient"
                         well I think otherwise, this looks like a bug. Also this code was not touched since 08/12/2015
                         
             When we remove this line that removes the association we get a fat NPE in the admin tool when opening the user, but our bug is fixed 😎
             But somehow I can't shake the feeling that the association should get fetched from the inbound source (i.e. PostgreSQL) at the right moment.
             Something like <searchStrategy>onResourceIfNeeded</searchStrategy>
             
             
             So now my conclusion is that the NPE in the admin points to the fact that together with name and identifiers there should also be a shadowRef saved in the shadow (PostgreSQL account) repo.
             We see that only the name and identifiers are saved in the beginning (first import task). I suppose the shadowRef is at that point not resolvable (perhaps the entitlements are not imported yet so the shadow of an entitlement is not yet available).
             Now we also see that if we perform a null safe check on the NPE site our use-case works (uniqueMember is cleared of the LDAP group when we remove entitlement from PostgreSQL) but we see that the
             shadow of the entitlement is lingering in the repo (and indeed when we again add the entitlement again in PostgreSQL an exception is thrown saying that there are conflicting shadows in the import task run).
             So my hunch is that the shadowRef should be saved on the association when possible. I see that during the reconciliation the shardowRef is correctly added beside the name and identifiers tags (but alas never saved to the repo).
             I will persue this line of thinking. My best guess is to save this shadowRef in the association someplace in the ShadowManager.
             
             
                    2020-01-24 15:49:48,759 [] [http-nio-8080-exec-6] ERROR (com.evolveum.midpoint.gui.impl.factory.ShadowAssociationWrapperFactoryImpl): Couldn't create container for associations. 
                    java.lang.NullPointerException: null
                    	at com.evolveum.midpoint.gui.impl.factory.ShadowAssociationWrapperFactoryImpl.createWrapper(ShadowAssociationWrapperFactoryImpl.java:193)
                    	at com.evolveum.midpoint.gui.impl.factory.ShadowAssociationWrapperFactoryImpl.createWrapper(ShadowAssociationWrapperFactoryImpl.java:102)
                    	at com.evolveum.midpoint.gui.impl.factory.ShadowAssociationWrapperFactoryImpl.createWrapper(ShadowAssociationWrapperFactoryImpl.java:66)
                    	at com.evolveum.midpoint.gui.impl.factory.PrismContainerWrapperFactoryImpl.addItemWrapper(PrismContainerWrapperFactoryImpl.java:111)
                    	at com.evolveum.midpoint.gui.impl.factory.PrismContainerWrapperFactoryImpl.createValueWrapper(PrismContainerWrapperFactoryImpl.java:84)
                    	at com.evolveum.midpoint.gui.impl.factory.PrismObjectWrapperFactoryImpl.createValueWrapper(PrismObjectWrapperFactoryImpl.java:108)
                    	at com.evolveum.midpoint.gui.impl.factory.PrismObjectWrapperFactoryImpl.createObjectWrapper(PrismObjectWrapperFactoryImpl.java:89)
                    	at com.evolveum.midpoint.web.page.admin.PageAdminFocus.loadShadowWrapper(PageAdminFocus.java:293)
                    	at com.evolveum.midpoint.web.page.admin.PageAdminFocus.loadShadowWrappers(PageAdminFocus.java:264)
                    	

        Al het bovenstaand gezever is niet nodig gewoon in de opendj resource file de native capabilities verwijderen does the trick!
            
        
- Vragen voor DAASI:

    - Vragen wat dat zinneke betekent: Using the normal (non-action) methods is also generally a preferred way because such definition is applied to all changes resulting in a consistent policy in Confluence (https://wiki.evolveum.com/display/midPoint/Synchronization+Configuration#SynchronizationConfiguration-Reactions)
    - Ook daarbij wat meer uitleg vragen wat die zaken zoals unmatched/unlinked nu juist betekenen. Reactions uitleggen
        En dan ook samen daarmee wat als activation overschreven wordt in de GUI => worden outbound overschreven?
    - Waarom die scripting task recompute voor die technische rollen om ze in LDAP te krijgen?
    - Die verschillende soorten tasks (reconcile/live/recompute/import), wat zijn de verschillen hier tussen en wanneer welke te runnen? Is het in onze use-case enkel nodig om een import task te runnen van tijd tot tijd?
    - Wanneer een entitlement bijkomt in DB dan hangt het af van of de entitlement import task eerst loopt en dan accounts pas om effectief in die run een ldap uniqueMember te genereren. Als de volgorde omgekeerd voordoet dan duurt het 2 import runs van die tasks om alles in orde te krijgen.
    - Kunnen wij associationTargetSearch gebruiken bij inbound assigment? Hoe doe je dat?
    - Crisis scenario indien er assignments zijn gemaakt van rollen aan users (manueel in midPoint), alle accounts in DB zijn leeg, import gebeurt, nadien zijn de users terug => zijn de assignments dan nog gelegd?
            Second storage (enriched role terug in PostgreSQL database)
    - Wat kunnen we doen om de import sneller te laten verlopen?
    - Is er een betere manier om de tasks te chainen? Partitioned Tasks... kleine introductie
    - Wanneer we 2 outbound resources (A en B) hebben en we willen adhv een attribuut die laten terecht komen in A of B. Hoe doe je dat?
    - Kan self-servicing werken als we niet per se email hebben en we geen passwords hebben?
    - Soms zien we in de logs een "time moved back" exception: what is that all about? En dan werken sommige tasks niet meer naar behoren. (is enkel in docker containers op Mac OS X)
    - Probleem waarbij de privileges in entitlements upgedate worden (verwijderen bvb) dan blijft de union zitten in midPoint ook als is de mapping authoritative. <set><predefined>all</predefined> werkt niet
        Dus er worden geen entitlements verwijderd...
    - Kunnen we gericht de accounts en entitlements reconciliaten (via REST api calls) wanneer we die aangepast zien vanuit de provisioning? (en zo live syncing doen)

    
    - Stacktrace van docker time float
    
            org.apache.wicket.WicketRuntimeException: Error attaching this container for rendering: [WebMarkupContainer [Component id = body]]
            2020-01-28 14:21:12,132 [] [http-nio-8080-exec-9] ERROR (com.evolveum.midpoint.web.security.LoggingRequestCycleListener): Error occurred during page rendering.
            	
            Caused by: java.lang.IllegalStateException: The time has moved back, possible consistency violation
            	at com.evolveum.midpoint.task.quartzimpl.LightweightIdentifierGeneratorImpl.generate(LightweightIdentifierGeneratorImpl.java:45)
            	at com.evolveum.midpoint.task.quartzimpl.TaskManagerQuartzImpl.generateTaskIdentifier(TaskManagerQuartzImpl.java:827)
            	at com.evolveum.midpoint.task.quartzimpl.TaskManagerQuartzImpl.createTaskInstance(TaskManagerQuartzImpl.java:822)
            	at com.evolveum.midpoint.task.quartzimpl.TaskManagerQuartzImpl.createTaskInstance(TaskManagerQuartzImpl.java:132)
            	at jdk.internal.reflect.GeneratedMethodAccessor962.invoke(Unknown Source)
            	at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
            	at java.base/java.lang.reflect.Method.invoke(Method.java:566)
            	at org.apache.wicket.proxy.LazyInitProxyFactory$JdkHandler.invoke(LazyInitProxyFactory.java:521)
            	at com.sun.proxy.$Proxy244.createTaskInstance(Unknown Source)
            	at com.evolveum.midpoint.gui.api.util.WebModelServiceUtils.createSimpleTask(WebModelServiceUtils.java:640)
            	at com.evolveum.midpoint.gui.api.page.PageBase.createSimpleTask(PageBase.java:752)
            	at com.evolveum.midpoint.gui.api.page.PageBase.createSimpleTask(PageBase.java:744)
            	at com.evolveum.midpoint.gui.api.page.PageBase.getCompiledUserProfile(PageBase.java:623)
            	at com.evolveum.midpoint.gui.api.page.PageBase.executeResultScriptHook(PageBase.java:1407)
            	at com.evolveum.midpoint.gui.api.page.PageBase.showResult(PageBase.java:1371)
            	at com.evolveum.midpoint.gui.api.page.PageBase.showResult(PageBase.java:1340)
            	at com.evolveum.midpoint.web.component.data.RepositoryObjectDataProvider.internalSize(RepositoryObjectDataProvider.java:175)
            	at com.evolveum.midpoint.web.component.data.BaseSortableDataProvider.size(BaseSortableDataProvider.java:315)
            	at org.apache.wicket.markup.repeater.data.DataViewBase.internalGetItemCount(DataViewBase.java:142)
            	at org.apache.wicket.markup.repeater.AbstractPageableView.getItemCount(AbstractPageableView.java:214)
            	at org.apache.wicket.markup.repeater.AbstractPageableView.getRowCount(AbstractPageableView.java:195)
            	at org.apache.wicket.markup.repeater.AbstractPageableView.getViewSize(AbstractPageableView.java:293)
            	at org.apache.wicket.markup.repeater.AbstractPageableView.getItemModels(AbstractPageableView.java:97)
            	at org.apache.wicket.markup.repeater.RefreshingView.onPopulate(RefreshingView.java:93)
            	at org.apache.wicket.markup.repeater.AbstractRepeater.onBeforeRender(AbstractRepeater.java:124)
            	at org.apache.wicket.markup.repeater.AbstractPageableView.onBeforeRender(AbstractPageableView.java:113)
            	at org.apache.wicket.Component.beforeRender(Component.java:939)
            	at org.apache.wicket.MarkupContainer.onBeforeRenderChildren(MarkupContainer.java:1754)
            	... 115 common frames omitted
    