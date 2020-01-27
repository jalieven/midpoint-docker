# Todo's

## Entitlements to roles

-   Map basic WebIDM role to LDAP
-   Split up the entitlements name to (meta) role
-   Assign the entitlement to the effective account via accountId
-   Map the organisation info in entitlement to Org structure (assignmentTargetSearch?)
-   Privileges in entitlement to effective role via mapping somewhere (dynamic and GUI editable)
-   Contact info stays on entitlement (find a way to map it to LDAP)
-   Map everything (orgs, roles, contactinfo) into LDAP structure

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
                    	at com.evolveum.midpoint.web.page.admin.PageAdminFocus$1.load(PageAdminFocus.java:123)
                    	at com.evolveum.midpoint.web.page.admin.PageAdminFocus$1.load(PageAdminFocus.java:118)
                    	at com.evolveum.midpoint.gui.api.model.LoadableModel.getObject(LoadableModel.java:59)
                    	at com.evolveum.midpoint.web.component.objectdetails.FocusMainPanel$4.getCount(FocusMainPanel.java:231)
                    	at com.evolveum.midpoint.gui.api.component.tabs.CountablePanelTab$1.getObject(CountablePanelTab.java:42)
                    	at com.evolveum.midpoint.gui.api.component.tabs.CountablePanelTab$1.getObject(CountablePanelTab.java:36)
                    	at com.evolveum.midpoint.web.component.TabbedPanel$2$1.getObject(TabbedPanel.java:141)
                    	at com.evolveum.midpoint.web.component.TabbedPanel$2$1.getObject(TabbedPanel.java:133)
                    	at org.apache.wicket.AttributeModifier.getReplacementOrNull(AttributeModifier.java:218)
                    	at org.apache.wicket.AttributeModifier.replaceAttributeValue(AttributeModifier.java:173)
                    	at org.apache.wicket.AttributeModifier.onComponentTag(AttributeModifier.java:155)
                    	at org.apache.wicket.Component.renderComponentTag(Component.java:3929)
                    	at org.apache.wicket.Component.internalRenderComponent(Component.java:2482)
                    	at org.apache.wicket.markup.html.WebComponent.onRender(WebComponent.java:60)
                    	at org.apache.wicket.Component.internalRender(Component.java:2296)
                    	at org.apache.wicket.Component.render(Component.java:2227)
                    	at org.apache.wicket.MarkupContainer.renderNext(MarkupContainer.java:1454)
                    	at org.apache.wicket.MarkupContainer.renderAll(MarkupContainer.java:1654)
                    	at org.apache.wicket.MarkupContainer.renderComponentTagBody(MarkupContainer.java:1629)
                    	at org.apache.wicket.MarkupContainer.onComponentTagBody(MarkupContainer.java:1587)
                    	at org.apache.wicket.markup.html.link.AbstractLink.onComponentTagBody(AbstractLink.java:82)
                    	at org.apache.wicket.markup.html.panel.DefaultMarkupSourcingStrategy.onComponentTagBody(DefaultMarkupSourcingStrategy.java:70)
                    	at org.apache.wicket.Component.internalRenderComponent(Component.java:2491)
                    	at org.apache.wicket.MarkupContainer.onRender(MarkupContainer.java:1593)
                    	at org.apache.wicket.Component.internalRender(Component.java:2296)
                    	at org.apache.wicket.Component.render(Component.java:2227)
                    	at org.apache.wicket.MarkupContainer.renderNext(MarkupContainer.java:1454)
                    	at org.apache.wicket.MarkupContainer.renderAll(MarkupContainer.java:1654)
                    	at org.apache.wicket.MarkupContainer.renderComponentTagBody(MarkupContainer.java:1629)
                    	at org.apache.wicket.MarkupContainer.onComponentTagBody(MarkupContainer.java:1587)
                    	at org.apache.wicket.markup.html.panel.DefaultMarkupSourcingStrategy.onComponentTagBody(DefaultMarkupSourcingStrategy.java:70)
                    	at org.apache.wicket.Component.internalRenderComponent(Component.java:2491)
                    	at org.apache.wicket.MarkupContainer.onRender(MarkupContainer.java:1593)
                    	at org.apache.wicket.Component.internalRender(Component.java:2296)
                    	at org.apache.wicket.Component.render(Component.java:2227)
                    	at org.apache.wicket.markup.html.list.Loop.renderItem(Loop.java:167)
                    	at org.apache.wicket.markup.html.list.Loop.renderChild(Loop.java:156)
                    	at org.apache.wicket.markup.repeater.AbstractRepeater.onRender(AbstractRepeater.java:102)
                    	at org.apache.wicket.Component.internalRender(Component.java:2296)
                    	at org.apache.wicket.Component.render(Component.java:2227)
                    	at org.apache.wicket.MarkupContainer.renderNext(MarkupContainer.java:1454)
                    	at org.apache.wicket.MarkupContainer.renderAll(MarkupContainer.java:1654)
                    	at org.apache.wicket.MarkupContainer.renderComponentTagBody(MarkupContainer.java:1629)
                    	at org.apache.wicket.MarkupContainer.onComponentTagBody(MarkupContainer.java:1587)
                    	at org.apache.wicket.markup.html.panel.DefaultMarkupSourcingStrategy.onComponentTagBody(DefaultMarkupSourcingStrategy.java:70)
                    	at org.apache.wicket.Component.internalRenderComponent(Component.java:2491)
                    	at org.apache.wicket.MarkupContainer.onRender(MarkupContainer.java:1593)
                    	at org.apache.wicket.Component.internalRender(Component.java:2296)
                    	at org.apache.wicket.Component.render(Component.java:2227)
                    	at org.apache.wicket.MarkupContainer.renderNext(MarkupContainer.java:1454)
                    	at org.apache.wicket.MarkupContainer.renderAll(MarkupContainer.java:1654)
                    	at org.apache.wicket.MarkupContainer.renderComponentTagBody(MarkupContainer.java:1629)
                    	at org.apache.wicket.MarkupContainer.renderAssociatedMarkup(MarkupContainer.java:798)
                    	at org.apache.wicket.markup.html.panel.AssociatedMarkupSourcingStrategy.renderAssociatedMarkup(AssociatedMarkupSourcingStrategy.java:77)
                    	at org.apache.wicket.markup.html.panel.PanelMarkupSourcingStrategy.onComponentTagBody(PanelMarkupSourcingStrategy.java:112)
                    	at org.apache.wicket.Component.internalRenderComponent(Component.java:2491)
                    	at org.apache.wicket.MarkupContainer.onRender(MarkupContainer.java:1593)
                    	at org.apache.wicket.Component.internalRender(Component.java:2296)
                    	at org.apache.wicket.Component.render(Component.java:2227)
                    	at org.apache.wicket.MarkupContainer.renderNext(MarkupContainer.java:1454)
                    	at org.apache.wicket.MarkupContainer.renderAll(MarkupContainer.java:1654)
                    	at org.apache.wicket.MarkupContainer.renderComponentTagBody(MarkupContainer.java:1629)
                    	at org.apache.wicket.MarkupContainer.onComponentTagBody(MarkupContainer.java:1587)
                    	at org.apache.wicket.markup.html.form.Form.onComponentTagBody(Form.java:1708)
                    	at com.evolveum.midpoint.web.component.form.Form.onComponentTagBody(Form.java:43)
                    	at org.apache.wicket.markup.html.panel.DefaultMarkupSourcingStrategy.onComponentTagBody(DefaultMarkupSourcingStrategy.java:70)
                    	at org.apache.wicket.Component.internalRenderComponent(Component.java:2491)
                    	at org.apache.wicket.MarkupContainer.onRender(MarkupContainer.java:1593)
                    	at org.apache.wicket.Component.internalRender(Component.java:2296)
                    	at org.apache.wicket.Component.render(Component.java:2227)
                    	at org.apache.wicket.MarkupContainer.renderNext(MarkupContainer.java:1454)
                    	at org.apache.wicket.MarkupContainer.renderAll(MarkupContainer.java:1654)
                    	at org.apache.wicket.MarkupContainer.renderComponentTagBody(MarkupContainer.java:1629)
                    	at org.apache.wicket.MarkupContainer.renderAssociatedMarkup(MarkupContainer.java:798)
                    	at org.apache.wicket.markup.html.panel.AssociatedMarkupSourcingStrategy.renderAssociatedMarkup(AssociatedMarkupSourcingStrategy.java:77)
                    	at org.apache.wicket.markup.html.panel.PanelMarkupSourcingStrategy.onComponentTagBody(PanelMarkupSourcingStrategy.java:112)
                    	at org.apache.wicket.Component.internalRenderComponent(Component.java:2491)
                    	at org.apache.wicket.MarkupContainer.onRender(MarkupContainer.java:1593)
                    	at org.apache.wicket.Component.internalRender(Component.java:2296)
                    	at org.apache.wicket.Component.render(Component.java:2227)
                    	at org.apache.wicket.MarkupContainer.renderNext(MarkupContainer.java:1454)
                    	at org.apache.wicket.MarkupContainer.renderAll(MarkupContainer.java:1654)
                    	at org.apache.wicket.MarkupContainer.renderComponentTagBody(MarkupContainer.java:1629)
                    	at org.apache.wicket.MarkupContainer.onComponentTagBody(MarkupContainer.java:1587)
                    	at org.apache.wicket.markup.html.panel.DefaultMarkupSourcingStrategy.onComponentTagBody(DefaultMarkupSourcingStrategy.java:70)
                    	at org.apache.wicket.Component.internalRenderComponent(Component.java:2491)
                    	at org.apache.wicket.MarkupContainer.onRender(MarkupContainer.java:1593)
                    	at org.apache.wicket.Component.internalRender(Component.java:2296)
                    	at org.apache.wicket.Component.render(Component.java:2227)
                    	at org.apache.wicket.MarkupContainer.renderNext(MarkupContainer.java:1454)
                    	at org.apache.wicket.MarkupContainer.renderAll(MarkupContainer.java:1654)
                    	at org.apache.wicket.Page.onRender(Page.java:858)
                    	at org.apache.wicket.markup.html.WebPage.onRender(WebPage.java:126)
                    	at org.apache.wicket.Component.internalRender(Component.java:2296)
                    	at org.apache.wicket.Component.render(Component.java:2227)
                    	at org.apache.wicket.Page.renderPage(Page.java:998)
                    	at org.apache.wicket.request.handler.render.WebPageRenderer.renderPage(WebPageRenderer.java:124)
                    	at org.apache.wicket.request.handler.render.WebPageRenderer.respond(WebPageRenderer.java:236)
                    	at org.apache.wicket.core.request.handler.RenderPageRequestHandler.respond(RenderPageRequestHandler.java:202)
                    	at org.apache.wicket.request.cycle.RequestCycle$HandlerExecutor.respond(RequestCycle.java:914)
                    	at org.apache.wicket.request.RequestHandlerExecutor.execute(RequestHandlerExecutor.java:65)
                    	at org.apache.wicket.request.cycle.RequestCycle.execute(RequestCycle.java:282)
                    	at org.apache.wicket.request.cycle.RequestCycle.processRequest(RequestCycle.java:253)
                    	at org.apache.wicket.request.cycle.RequestCycle.processRequestAndDetach(RequestCycle.java:221)
                    	at org.apache.wicket.protocol.http.WicketFilter.processRequestCycle(WicketFilter.java:275)
                    	at org.apache.wicket.protocol.http.WicketFilter.processRequest(WicketFilter.java:206)
                    	at org.apache.wicket.protocol.http.WicketFilter.doFilter(WicketFilter.java:299)
                    	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:193)
                    	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:166)
                    	at com.evolveum.midpoint.web.util.MidPointProfilingServletFilter.doFilter(MidPointProfilingServletFilter.java:85)
                    	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:193)
                    	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:166)
                    	at org.springframework.security.web.FilterChainProxy$VirtualFilterChain.doFilter(FilterChainProxy.java:320)
                    	at org.springframework.security.web.session.SessionManagementFilter.doFilter(SessionManagementFilter.java:84)
                    	at org.springframework.security.web.FilterChainProxy$VirtualFilterChain.doFilter(FilterChainProxy.java:334)
                    	at com.evolveum.midpoint.web.security.filter.MidpointAuthFilter$VirtualFilterChain.doFilter(MidpointAuthFilter.java:240)
                    	at org.springframework.security.web.access.intercept.FilterSecurityInterceptor.invoke(FilterSecurityInterceptor.java:127)
                    	at org.springframework.security.web.access.intercept.FilterSecurityInterceptor.doFilter(FilterSecurityInterceptor.java:91)
                    	at com.evolveum.midpoint.web.security.filter.MidpointAuthFilter$VirtualFilterChain.doFilter(MidpointAuthFilter.java:254)
                    	at org.springframework.security.web.session.SessionManagementFilter.doFilter(SessionManagementFilter.java:137)
                    	at com.evolveum.midpoint.web.security.filter.MidpointAuthFilter$VirtualFilterChain.doFilter(MidpointAuthFilter.java:254)
                    	at org.springframework.security.web.access.ExceptionTranslationFilter.doFilter(ExceptionTranslationFilter.java:119)
                    	at com.evolveum.midpoint.web.security.filter.MidpointAuthFilter$VirtualFilterChain.doFilter(MidpointAuthFilter.java:254)
                    	at com.evolveum.midpoint.web.security.filter.MidpointAnonymousAuthenticationFilter.doFilter(MidpointAnonymousAuthenticationFilter.java:66)
                    	at com.evolveum.midpoint.web.security.filter.MidpointAuthFilter$VirtualFilterChain.doFilter(MidpointAuthFilter.java:254)
                    	at org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter.doFilter(SecurityContextHolderAwareRequestFilter.java:170)
                    	at com.evolveum.midpoint.web.security.filter.MidpointAuthFilter$VirtualFilterChain.doFilter(MidpointAuthFilter.java:254)
                    	at org.springframework.security.web.savedrequest.RequestCacheAwareFilter.doFilter(RequestCacheAwareFilter.java:63)
                    	at com.evolveum.midpoint.web.security.filter.MidpointAuthFilter$VirtualFilterChain.doFilter(MidpointAuthFilter.java:254)
                    	at org.springframework.security.web.session.ConcurrentSessionFilter.doFilter(ConcurrentSessionFilter.java:155)
                    	at com.evolveum.midpoint.web.security.filter.MidpointAuthFilter$VirtualFilterChain.doFilter(MidpointAuthFilter.java:254)
                    	at org.springframework.security.web.csrf.CsrfFilter.doFilterInternal(CsrfFilter.java:100)
                    	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:118)
                    	at com.evolveum.midpoint.web.security.filter.MidpointAuthFilter$VirtualFilterChain.doFilter(MidpointAuthFilter.java:254)
                    	at org.springframework.security.web.header.HeaderWriterFilter.doFilterInternal(HeaderWriterFilter.java:74)
                    	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:118)
                    	at com.evolveum.midpoint.web.security.filter.MidpointAuthFilter$VirtualFilterChain.doFilter(MidpointAuthFilter.java:254)
                    	at com.evolveum.midpoint.web.security.filter.MidpointAuthFilter.processingInMidpoint(MidpointAuthFilter.java:209)
                    	at com.evolveum.midpoint.web.security.filter.MidpointAuthFilter.doFilterInternal(MidpointAuthFilter.java:119)
                    	at com.evolveum.midpoint.web.security.filter.MidpointAuthFilter.doFilter(MidpointAuthFilter.java:105)
                    	at org.springframework.security.web.FilterChainProxy$VirtualFilterChain.doFilter(FilterChainProxy.java:334)
                    	at org.springframework.security.web.session.ConcurrentSessionFilter.doFilter(ConcurrentSessionFilter.java:155)
                    	at org.springframework.security.web.FilterChainProxy$VirtualFilterChain.doFilter(FilterChainProxy.java:334)
                    	at org.springframework.security.web.context.SecurityContextPersistenceFilter.doFilter(SecurityContextPersistenceFilter.java:105)
                    	at org.springframework.security.web.FilterChainProxy$VirtualFilterChain.doFilter(FilterChainProxy.java:334)
                    	at org.springframework.security.web.context.request.async.WebAsyncManagerIntegrationFilter.doFilterInternal(WebAsyncManagerIntegrationFilter.java:56)
                    	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:118)
                    	at org.springframework.security.web.FilterChainProxy$VirtualFilterChain.doFilter(FilterChainProxy.java:334)
                    	at org.springframework.security.web.FilterChainProxy.doFilterInternal(FilterChainProxy.java:215)
                    	at org.springframework.security.web.FilterChainProxy.doFilter(FilterChainProxy.java:178)
                    	at org.springframework.web.filter.DelegatingFilterProxy.invokeDelegate(DelegatingFilterProxy.java:357)
                    	at org.springframework.web.filter.DelegatingFilterProxy.doFilter(DelegatingFilterProxy.java:270)
                    	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:193)
                    	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:166)
                    	at org.springframework.web.filter.RequestContextFilter.doFilterInternal(RequestContextFilter.java:99)
                    	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:118)
                    	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:193)
                    	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:166)
                    	at org.springframework.web.filter.FormContentFilter.doFilterInternal(FormContentFilter.java:92)
                    	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:118)
                    	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:193)
                    	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:166)
                    	at org.springframework.web.filter.HiddenHttpMethodFilter.doFilterInternal(HiddenHttpMethodFilter.java:93)
                    	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:118)
                    	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:193)
                    	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:166)
                    	at org.springframework.boot.actuate.metrics.web.servlet.WebMvcMetricsFilter.filterAndRecordMetrics(WebMvcMetricsFilter.java:114)
                    	at org.springframework.boot.actuate.metrics.web.servlet.WebMvcMetricsFilter.doFilterInternal(WebMvcMetricsFilter.java:104)
                    	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:118)
                    	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:193)
                    	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:166)
                    	at org.springframework.web.filter.CharacterEncodingFilter.doFilterInternal(CharacterEncodingFilter.java:200)
                    	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:118)
                    	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:193)
                    	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:166)
                    	at org.apache.catalina.core.StandardWrapperValve.invoke(StandardWrapperValve.java:202)
                    	at org.apache.catalina.core.StandardContextValve.invoke(StandardContextValve.java:96)
                    	at org.apache.catalina.authenticator.AuthenticatorBase.invoke(AuthenticatorBase.java:526)
                    	at org.apache.catalina.core.StandardHostValve.invoke(StandardHostValve.java:139)
                    	at org.apache.catalina.valves.ErrorReportValve.invoke(ErrorReportValve.java:92)
                    	at org.apache.catalina.core.StandardEngineValve.invoke(StandardEngineValve.java:74)
                    	at com.evolveum.midpoint.web.boot.TomcatRootValve.invoke(TomcatRootValve.java:60)
                    	at org.apache.catalina.connector.CoyoteAdapter.service(CoyoteAdapter.java:343)
                    	at org.apache.coyote.http11.Http11Processor.service(Http11Processor.java:408)
                    	at org.apache.coyote.AbstractProcessorLight.process(AbstractProcessorLight.java:66)
                    	at org.apache.coyote.AbstractProtocol$ConnectionHandler.process(AbstractProtocol.java:860)
                    	at org.apache.tomcat.util.net.NioEndpoint$SocketProcessor.doRun(NioEndpoint.java:1587)
                    	at org.apache.tomcat.util.net.SocketProcessorBase.run(SocketProcessorBase.java:49)
                    	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1128)
                    	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:628)
                    	at org.apache.tomcat.util.threads.TaskThread$WrappingRunnable.run(TaskThread.java:61)
                    	at java.base/java.lang.Thread.run(Thread.java:834)

            
        
- Vragen voor DAASI:

    - Vragen wat dat zinneke betekent: Using the normal (non-action) methods is also generally a preferred way because such definition is applied to all changes resulting in a consistent policy in Confluence (https://wiki.evolveum.com/display/midPoint/Synchronization+Configuration#SynchronizationConfiguration-Reactions)
    - Ook daarbij wat meer uitleg vragen wat die zaken zoals unmatched/unlinked nu juist betekenen. Reactions uitleggen
    - Waarom die scripting task recompute voor die technische rollen om ze in LDAP te krijgen?
    - Die verschillende soorten tasks (reconcile/live/recompute/import), wat zijn de verschillen hier tussen en wanneer welke te runnen? Is het in onze use-case enkel nodig om een import task te runnen van tijd tot tijd?
    - Wanneer een entitlement bijkomt in DB dan hangt het af van of de entitlement import task eerst loopt en dan accounts pas om effectief in die run een ldap uniqueMember te genereren. Als de volgorde omgekeerd voordoet dan duurt het 2 runs van die tasks om alles in orde te krijgen.